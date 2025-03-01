package com.cpen321.study_wimme

import java.io.Serializable

data class User(
    val googleId: String,
    val email: String,
    val displayName: String,
    val userName: String,
    val firstName: String,
    val lastName: String,
    val year: Int,
    val faculty: String,
    val interests: String = "",
    val friends: List<String> = emptyList(),
    val friendRequests: List<String> = emptyList()
) : Serializable