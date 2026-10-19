
import {setGlobalOptions} from "firebase-functions/v2";
import {onCall, HttpsError} from "firebase-functions/v2/https";
import {initializeApp} from "firebase-admin/app";
import {getFirestore} from "firebase-admin/firestore";

setGlobalOptions({
  region: "us-central1",
  maxInstances: 10,
});

initializeApp();

const db = getFirestore();

interface OrderItemInput {
  sku: string;
  quantity: number;
}

interface CreateOrderInput {
  requestId: string;
  items: OrderItemInput[];
  couponCode?: string | null;
  deliveryMethod: "DELIVERY" | "PICKUP";
  deliveryAddress?: string | null;
  pickupStoreId?: string | null;
  paymentMethod: string;
  receiptType: string;
  invoiceRuc?: string | null;
  invoiceBusinessName?: string | null;
}

export const createOrder = onCall(
  {enforceAppCheck: false},
  async (request) => {
    // Protección temporal: no permitir escrituras en producción.
    if (
      process.env.FUNCTIONS_EMULATOR !== "true" ||
      !process.env.FIRESTORE_EMULATOR_HOST
    ) {
      throw new HttpsError(
        "failed-precondition",
        "El registro de pedidos solo está habilitado en emuladores."
      );
    }

    const uid = request.auth?.uid;

    if (!uid) {
      throw new HttpsError(
        "unauthenticated",
        "Debes iniciar sesión para registrar un pedido."
      );
    }

    const data = request.data as CreateOrderInput | null;

    if (
      !data ||
      typeof data !== "object" ||
      Array.isArray(data)
    ) {
      throw new HttpsError(
        "invalid-argument",
        "La solicitud del pedido no es válida."
      );
    }

    if (
      typeof data.requestId !== "string" ||
      !/^[a-zA-Z0-9_-]{8,80}$/.test(data.requestId)
    ) {
      throw new HttpsError(
        "invalid-argument",
        "El identificador del pedido no es válido."
      );
    }

    if (
      !Array.isArray(data.items) ||
      data.items.length < 1 ||
      data.items.length > 50
    ) {
      throw new HttpsError(
        "invalid-argument",
        "El pedido debe contener entre 1 y 50 productos."
      );
    }

    const skus = new Set<string>();

    for (const item of data.items) {
      if (
        !item ||
        typeof item.sku !== "string" ||
        !/^FA-[0-9]{3,}$/.test(item.sku) ||
        !Number.isSafeInteger(item.quantity) ||
        item.quantity < 1 ||
        item.quantity > 100
      ) {
        throw new HttpsError(
          "invalid-argument",
          "Hay productos o cantidades inválidas."
        );
      }

      if (skus.has(item.sku)) {
        throw new HttpsError(
          "invalid-argument",
          "El pedido contiene productos duplicados."
        );
      }

      skus.add(item.sku);
    }

    if (data.deliveryMethod === "DELIVERY") {
      if (
        typeof data.deliveryAddress !== "string" ||
        data.deliveryAddress.trim().length < 5 ||
        data.deliveryAddress.length > 500
      ) {
        throw new HttpsError(
          "invalid-argument",
          "La dirección de entrega no es válida."
        );
      }
    } else if (data.deliveryMethod === "PICKUP") {
      if (
        typeof data.pickupStoreId !== "string" ||
        !/^[a-zA-Z0-9_-]{1,80}$/.test(data.pickupStoreId)
      ) {
        throw new HttpsError(
          "invalid-argument",
          "Selecciona una botica válida."
        );
      }
    } else {
      throw new HttpsError(
        "invalid-argument",
        "Método de entrega no válido."
      );
    }

    if (
      typeof data.paymentMethod !== "string" ||
      !["EFECTIVO", "YAPE", "PLIN", "TARJETA_DEMO"]
        .includes(data.paymentMethod)
    ) {
      throw new HttpsError(
        "invalid-argument",
        "Método de pago no reconocido."
      );
    }

    if (
      typeof data.receiptType !== "string" ||
      !["Boleta simple", "Factura"].includes(data.receiptType)
    ) {
      throw new HttpsError(
        "invalid-argument",
        "Comprobante no reconocido."
      );
    }

    if (data.receiptType === "Factura") {
      if (
        typeof data.invoiceRuc !== "string" ||
        !/^\d{11}$/.test(data.invoiceRuc) ||
        typeof data.invoiceBusinessName !== "string" ||
        data.invoiceBusinessName.trim().length < 3 ||
        data.invoiceBusinessName.length > 200
      ) {
        throw new HttpsError(
          "invalid-argument",
          "Los datos de facturación no son válidos."
        );
      }
    }

    const couponCode =
      typeof data.couponCode === "string" ?
        data.couponCode.trim().toUpperCase() : null;

    if (couponCode && couponCode !== "FARMAPP10") {
      throw new HttpsError(
        "invalid-argument",
        "Cupón no válido."
      );
    }

    // Normalizar los datos para identificar reintentos idénticos.
    const normalizedItems = data.items
      .map((item) => ({
        sku: item.sku,
        quantity: item.quantity,
      }))
      .sort((a, b) => a.sku.localeCompare(b.sku));

    const orderRequest = {
      items: normalizedItems,
      couponCode: couponCode || null,
      deliveryMethod: data.deliveryMethod,
      deliveryAddress: data.deliveryMethod === "DELIVERY" ?
        data.deliveryAddress!.trim() : null,
      pickupStoreId: data.deliveryMethod === "PICKUP" ?
        data.pickupStoreId! : null,
      paymentMethod: data.paymentMethod,
      receiptType: data.receiptType,
      invoiceRuc: data.receiptType === "Factura" ?
        data.invoiceRuc! : null,
      invoiceBusinessName: data.receiptType === "Factura" ?
        data.invoiceBusinessName!.trim() : null,
    };

    const requestFingerprint = JSON.stringify(orderRequest);

    const orderRef = db
      .collection("users")
      .doc(uid)
      .collection("orders")
      .doc(data.requestId);

    const productRefs = normalizedItems.map((item) =>
      db.collection("products").doc(item.sku)
    );

    return db.runTransaction(async (transaction) => {
      // Primero se realizan TODAS las lecturas.
      const existingOrder = await transaction.get(orderRef);

      // Idempotencia: un reintento idéntico no modifica el stock.
      if (existingOrder.exists) {
        const previous = existingOrder.data();

        if (previous?.requestFingerprint !== requestFingerprint) {
          throw new HttpsError(
            "already-exists",
            "Este identificador pertenece a otro pedido."
          );
        }

        return {
          validated: true,
          persisted: true,
          reused: true,
          orderId: data.requestId,
          subtotalCents: previous.subtotalCents,
          discountCents: previous.discountCents,
          totalCents: previous.totalCents,
          message: "Pedido recuperado sin descontar stock nuevamente.",
        };
      }

      const productSnapshots = [];

      for (const productRef of productRefs) {
        productSnapshots.push(await transaction.get(productRef));
      }

      // Todas las lecturas terminaron; ahora validamos.
      const lines = [];
      let subtotalCents = 0;

      for (let i = 0; i < normalizedItems.length; i++) {
        const item = normalizedItems[i];
        const snapshot = productSnapshots[i];

        if (!snapshot.exists) {
          throw new HttpsError(
            "not-found",
            `Producto no encontrado: ${item.sku}`
          );
        }

        const product = snapshot.data();

        if (
          (product?.sku !== undefined && product.sku !== item.sku) ||
          typeof product?.name !== "string" ||
          product.name.trim() === "" ||
          product.active !== true
        ) {
          throw new HttpsError(
            "failed-precondition",
            `Producto no disponible: ${item.sku}`
          );
        }

        const price = product.priceCents;
        const stock = product.stock;

        if (
          !Number.isSafeInteger(price) ||
          price <= 0 ||
          !Number.isSafeInteger(stock) ||
          stock < 0
        ) {
          throw new HttpsError(
            "failed-precondition",
            `Datos de catálogo inválidos: ${item.sku}`
          );
        }

        if (stock < item.quantity) {
          throw new HttpsError(
            "failed-precondition",
            `Stock insuficiente para ${item.sku}`
          );
        }

        const lineTotal = price * item.quantity;

        if (
          !Number.isSafeInteger(lineTotal) ||
          !Number.isSafeInteger(subtotalCents + lineTotal)
        ) {
          throw new HttpsError(
            "invalid-argument",
            "El importe del pedido excede el límite permitido."
          );
        }

        subtotalCents += lineTotal;

        lines.push({
          sku: item.sku,
          name: product.name,
          unitPriceCents: price,
          quantity: item.quantity,
        });
      }

      const discountCents = couponCode === "FARMAPP10" ?
        Math.floor(subtotalCents / 10) : 0;

      const totalCents = subtotalCents - discountCents;
      const createdAtMillis = Date.now();

      // Escrituras atómicas: inventario y pedido.
      for (let i = 0; i < normalizedItems.length; i++) {
        const currentStock = productSnapshots[i].data()!.stock;
        transaction.update(productRefs[i], {
          stock: currentStock - normalizedItems[i].quantity,
        });
      }

      transaction.create(orderRef, {
        id: data.requestId,
        userId: uid,
        createdAtMillis,
        items: lines,
        subtotalCents,
        discountCents,
        totalCents,
        deliveryMethod: orderRequest.deliveryMethod,
        deliveryAddress: orderRequest.deliveryAddress,
        pickupStoreId: orderRequest.pickupStoreId,
        paymentMethod: orderRequest.paymentMethod,
        receiptType: orderRequest.receiptType,
        invoiceRuc: orderRequest.invoiceRuc,
        invoiceBusinessName: orderRequest.invoiceBusinessName,
        couponCode: orderRequest.couponCode,
        status: "DEMO_PENDING",
        requestFingerprint,
      });

      return {
        validated: true,
        persisted: true,
        reused: false,
        orderId: data.requestId,
        subtotalCents,
        discountCents,
        totalCents,
        message: "Pedido de demostración registrado correctamente.",
      };
    });
  }
);
