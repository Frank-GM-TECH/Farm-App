package com.example.farmapp.profile

interface ProfileRepository {

    suspend fun getProfile(userId: String): UserProfile?

    suspend fun createProfileIfMissing(
        userId: String,
        displayName: String = ""
    ): UserProfile

    suspend fun updateProfile(
        userId: String,
        displayName: String,
        phone: String,
        address: String
    )

    suspend fun updatePhotoUrl(
        userId: String,
        photoUrl: String
    )
}