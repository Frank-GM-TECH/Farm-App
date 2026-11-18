package com.example.farmapp.data.catalog

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Escucha los productos activos de Firestore y actualiza la caché Room.
 * Usa una única instancia a nivel de Application.
 * No confirma compras ni modifica el stock remoto.
 */
class CatalogRealtimeSync(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val dao: CatalogProductDao
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val writeMutex = Mutex()
    private var registration: ListenerRegistration? = null
    private var listenerUserId: String? = null
    private var generation = 0L
    private var started = false

    private val authListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
        listenFor(firebaseAuth.currentUser?.uid)
    }

    fun start() {
        if (started) return
        started = true
        auth.addAuthStateListener(authListener)
    }

    fun stop() {
        if (!started) return
        started = false
        auth.removeAuthStateListener(authListener)
        registration?.remove()
        registration = null
        listenerUserId = null
        generation++
        scope.cancel()
    }

    private fun listenFor(uid: String?) {
        if (uid == listenerUserId && registration != null) return
        registration?.remove()
        registration = null
        listenerUserId = uid
        generation++
        val currentGeneration = generation
        if (uid == null) return

        registration = firestore.collection("products")
            .whereEqualTo("active", true)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.w("FarmAppCatalogSync", "Sincronización interrumpida; se conserva Room", error)
                    return@addSnapshotListener
                }
                // Una instantánea en caché no demuestra el catálogo completo del servidor.
                if (snapshot == null || snapshot.metadata.isFromCache) return@addSnapshotListener

                val parsed = try {
                    val products = snapshot.documents.map { doc ->
                        val sku = doc.getString("sku") ?: error("SKU ausente")
                        val name = doc.getString("name") ?: error("Nombre ausente")
                        val category = doc.getString("category") ?: error("Categoría ausente")
                        val price = doc.getLong("priceCents") ?: error("Precio ausente")
                        val stockLong = doc.getLong("stock") ?: error("Stock ausente")
                        require(doc.id == sku && sku.isNotBlank() && name.isNotBlank() && category.isNotBlank())
                        require(price >= 0 && stockLong in 0..Int.MAX_VALUE.toLong())
                        require(doc.getBoolean("active") == true)
                        CatalogProductEntity(
                            sku = sku,
                            barcode = doc.getString("barcode"),
                            name = name,
                            description = doc.getString("description") ?: "",
                            category = category,
                            priceCents = price,
                            stock = stockLong.toInt(),
                            imageUrl = doc.getString("imageUrl"),
                            active = true
                        )
                    }
                    require(products.map { it.sku }.distinct().size == products.size)
                    val barcodes = products.mapNotNull { it.barcode?.takeIf(String::isNotBlank) }
                    require(barcodes.distinct().size == barcodes.size)
                    products
                } catch (e: Exception) {
                    Log.e("FarmAppCatalogSync", "Instantánea inválida; no se modifica Room", e)
                    return@addSnapshotListener
                }

                scope.launch {
                    writeMutex.withLock {
                        if (started && generation == currentGeneration && auth.currentUser?.uid == uid) {
                            try {
                                dao.replaceActiveSnapshot(parsed)
                            } catch (e: Exception) {
                                Log.e("FarmAppCatalogSync", "Error al actualizar Room", e)
                            }
                        }
                    }
                }
            }
    }
}