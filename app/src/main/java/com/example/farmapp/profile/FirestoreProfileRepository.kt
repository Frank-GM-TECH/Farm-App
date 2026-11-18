package com.example.farmapp.profile

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class FirestoreProfileRepository(
    private val firestore: FirebaseFirestore
) : ProfileRepository {

    private val usersCollection
        get() = firestore.collection("users")

    override suspend fun getProfile(
        userId: String
    ): UserProfile? {
        val document = usersCollection
            .document(userId)
            .get()
            .await()

        return document.toObject(UserProfile::class.java)
    }

    override suspend fun createProfileIfMissing(
        userId: String,
        displayName: String
    ): UserProfile {
        val reference = usersCollection.document(userId)

        return firestore.runTransaction { transaction ->
            val snapshot = transaction.get(reference)

            if (snapshot.exists()) {
                snapshot.toObject(UserProfile::class.java)
                    ?: error("No se pudo leer el perfil")
            } else {
                val newProfile = UserProfile(
                    displayName = displayName.trim(),
                    role = "cliente"
                )

                transaction.set(reference, newProfile)
                newProfile
            }
        }.await()
    }

    override suspend fun updateProfile(
        userId: String,
        displayName: String,
        phone: String,
        address: String
    ) {
        usersCollection.document(userId)
            .update(
                mapOf(
                    "displayName" to displayName.trim(),
                    "phone" to phone.trim(),
                    "address" to address.trim()
                )
            )
            .await()
    }

    override suspend fun updatePhotoUrl(
        userId: String,
        photoUrl: String
    ) {
        usersCollection.document(userId)
            .update("photoUrl", photoUrl)
            .await()
    }
}
