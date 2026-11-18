package com.example.farmapp.data.catalog

/**
 * HU03 - Productos de demostración de FarmApp.
 *
 * Incluye:
 * - Código SKU.
 * - Código de barras.
 * - Nombre y descripción.
 * - Categoría.
 * - Precio en céntimos.
 * - Stock.
 * - Fotografía de referencia.
 *
 * Las imágenes proceden de Wikimedia Commons.
 * Los precios, códigos y existencias son ficticios.
 */
object CatalogSeedData {

    val sampleProducts: List<CatalogProductEntity> = listOf(

        // =========================================
        // PRODUCTO 1 - ALCOHOL EN GEL
        // =========================================

        CatalogProductEntity(
            sku = "FA-001",
            barcode = "2000000000015",
            name = "Alcohol en gel",
            description = "Producto de higiene personal.",
            category = "HIGIENE",
            priceCents = 890L,
            stock = 12,
            imageUrl =
                "https://upload.wikimedia.org/wikipedia/commons/" +
                        "thumb/6/6b/Hand_sanitizer_bottle.jpg/" +
                        "500px-Hand_sanitizer_bottle.jpg"
        ),

        // =========================================
        // PRODUCTO 2 - JABÓN NEUTRO
        // =========================================

        CatalogProductEntity(
            sku = "FA-002",
            barcode = "2000000000022",
            name = "Jabón neutro",
            description = "Jabón para higiene personal.",
            category = "HIGIENE",
            priceCents = 650L,
            stock = 7,
            imageUrl =
                "https://thumb.wikimedia.org/wikipedia/commons/" +
                        "thumb/5/53/A_bar_of_soap.jpg/" +
                        "960px-A_bar_of_soap.jpg"
        ),

        // =========================================
        // PRODUCTO 3 - MASCARILLAS
        // =========================================

        CatalogProductEntity(
            sku = "FA-003",
            barcode = "2000000000039",
            name = "Mascarillas",
            description = "Mascarillas de protección.",
            category = "HIGIENE",
            priceCents = 480L,
            stock = 0,
            imageUrl =
                "https://thumb.wikimedia.org/wikipedia/commons/" +
                        "thumb/3/34/Surgical_face_mask.jpg/" +
                        "960px-Surgical_face_mask.jpg"
        ),

        // =========================================
        // PRODUCTO 4 - GASAS ESTÉRILES
        // =========================================

        CatalogProductEntity(
            sku = "FA-004",
            barcode = "2000000000046",
            name = "Gasas estériles",
            description = "Material de primeros auxilios.",
            category = "PRIMEROS_AUXILIOS",
            priceCents = 550L,
            stock = 4,
            imageUrl =
                "https://thumb.wikimedia.org/wikipedia/commons/" +
                        "thumb/3/3a/Gauze_bandage.png/" +
                        "960px-Gauze_bandage.png"
        ),

        // =========================================
        // PRODUCTO 5 - VENDA ELÁSTICA
        // =========================================

        CatalogProductEntity(
            sku = "FA-005",
            barcode = "2000000000053",
            name = "Venda elástica",
            description = "Venda para primeros auxilios.",
            category = "PRIMEROS_AUXILIOS",
            priceCents = 990L,
            stock = 3,
            imageUrl =
                "https://thumb.wikimedia.org/wikipedia/commons/" +
                        "thumb/6/6f/" +
                        "2023_Opaski_Hartmann_P%C3%BCtterbinde.jpg/" +
                        "960px-2023_Opaski_Hartmann_P%C3%BCtterbinde.jpg"
        ),

        // =========================================
        // PRODUCTO 6 - TERMÓMETRO DIGITAL
        // =========================================

        CatalogProductEntity(
            sku = "FA-006",
            barcode = "2000000000060",
            name = "Termómetro digital",
            description = "Equipo para medir temperatura.",
            category = "EQUIPOS",
            priceCents = 2550L,
            stock = 8,
            imageUrl =
                "https://thumb.wikimedia.org/wikipedia/commons/" +
                        "thumb/d/d0/Digital_Thermometer.jpg/" +
                        "960px-Digital_Thermometer.jpg"
        )
    )
}