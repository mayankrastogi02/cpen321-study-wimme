package com.cpen321.study_wimme

enum class SessionVisibility {
    PRIVATE, PUBLIC
}

data class Session(
    val name: String,
    val time: String,
    val location: String,
    val description: String,
    val visibility: SessionVisibility
)
