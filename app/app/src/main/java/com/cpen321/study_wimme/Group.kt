package com.cpen321.study_wimme

import java.io.Serializable

data class Group(
    val id: String,
    val name: String,
    val description: String
) : Serializable