package com.example.mediscan

import java.io.Serializable

data class Patient(
    val id: String = "",           // Firestore document ID
    val name: String? = "",
    val age: Int? = 0,
    val gender: String? = "",
    val condition: String? = "",
    val lastVisit: String? = "",
    val phone: String? = "",
    val address: String? = "",
    val priority: String? = "",
    val allergies: String? = "",
    val history: String? = "",
    val insurance: String? = "",
    val policy: String? = "",
    val notes: String? = ""
) : Serializable
