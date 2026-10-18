package com.example.farmapp.profile

data class UserProfile(
    val displayName: String = "",
    val phone: String = "",
    val address: String = "",
    val photoUrl: String = "",
    val role: String = "cliente"
)