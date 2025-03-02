package com.cpen321.study_wimme

import java.io.Serializable

enum class SessionVisibility : Serializable {
    PRIVATE, PUBLIC
}

enum class SessionFilter : Serializable {
    FIND, JOINED, HOSTED
}

data class Session(
    val id: String = "", // MongoDB document ID
    val name: String,
    val time: String,
    val location: String,
    val description: String = "",
    val visibility: SessionVisibility = SessionVisibility.PRIVATE,
    // Additional fields for details screen
    val subject: String = "",
    val faculty: String = "",
    val year: String = "",
    val hostName: String = "Unknown Host",
    val hostId: String = "",
    val participants: List<String> = emptyList()
) : Serializable