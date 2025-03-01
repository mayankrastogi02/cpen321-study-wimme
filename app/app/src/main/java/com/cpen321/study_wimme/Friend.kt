package com.cpen321.study_wimme

import java.io.Serializable

data class Friend(
    val id: String,
    val username: String,
    val firstName: String,
    val lastName: String,
    val year: String,
    val program: String,
    val interests: String,
) : Serializable