package com.example.farmapp.profile

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageException
import com.google.firebase.storage.StorageMetadata
import kotlinx.coroutines.tasks.await

class FirebaseProfilePhotoStorage(
    private val storage: FirebaseStorage =
        FirebaseStorage.getInstance(
            "gs://farm-app-c5291.firebasestorage.app"
        ),
    private val auth: FirebaseAuth =
        FirebaseAuth.getInstance()
) {

    private fun avatarPath(): String {
        val userId = auth.currentUser?.uid
            ?: error("Debes iniciar sesión.")

        return "profile_photos/$userId/avatar.jpg"
    }

    suspend fun uploadAvatar(
        jpegBytes: ByteArray
    ): String {

        require(jpegBytes.isNotEmpty()) {
            "La imagen está vacía."
        }

        require(jpegBytes.size <= 5 * 1024 * 1024) {
            "La imagen supera los 5 MiB."
        }

        val path = avatarPath()
        val reference = storage.reference.child(path)

        val metadata = StorageMetadata.Builder()
            .setContentType("image/jpeg")
            .build()

        reference.putBytes(
            jpegBytes,
            metadata
        ).await()

        return path
    }

    suspend fun readAvatar(): ByteArray? {

        val reference = storage.reference.child(
            avatarPath()
        )

        return try {
            reference.getBytes(
                5L * 1024 * 1024
            ).await()
        } catch (e: StorageException) {
            if (
                e.errorCode ==
                StorageException.ERROR_OBJECT_NOT_FOUND
            ) {
                null
            } else {
                throw e
            }
        }
    }
}
