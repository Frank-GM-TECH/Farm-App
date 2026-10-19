package com.example.farmapp.data.orders

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Source
import kotlinx.coroutines.tasks.await

/** Interfaz simple para sustituir Firestore por un fake en pruebas. */
interface OrderRepository {
    suspend fun getOrdersFromServer(): List<FarmOrder>
    suspend fun getOrderFromServer(orderId: String): FarmOrder?
}

/**
 * HU04 13A: lectura de pedidos de demostración desde Firestore.
 * No implementa aún escrituras ni confirma pedidos; eso corresponde a 13C.
 */
class FirestoreOrderRepository(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : OrderRepository {
    private fun currentUid(): String =
        requireNotNull(auth.currentUser?.uid) { "Inicia sesión para consultar pedidos." }

    override suspend fun getOrdersFromServer(): List<FarmOrder> {
        val uid = currentUid()
        val result = firestore.collection("users")
            .document(uid)
            .collection("orders")
            .get(Source.SERVER)
            .await()
        check(currentUid() == uid) { "La sesión cambió durante la consulta." }
        return result.documents.mapNotNull { doc ->
            doc.toObject(FarmOrder::class.java)?.takeIf {
                it.id == doc.id && it.userId == uid
            }
        }
    }

    override suspend fun getOrderFromServer(orderId: String): FarmOrder? {
        require(orderId.isNotBlank() && '/' !in orderId) { "ID de pedido inválido." }
        val uid = currentUid()
        val doc = firestore.collection("users")
            .document(uid)
            .collection("orders")
            .document(orderId)
            .get(Source.SERVER)
            .await()
        check(currentUid() == uid) { "La sesión cambió durante la consulta." }
        return doc.toObject(FarmOrder::class.java)?.takeIf {
            it.id == doc.id && it.userId == uid
        }
    }
}