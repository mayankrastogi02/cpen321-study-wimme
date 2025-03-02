package com.cpen321.study_wimme

import java.io.Serializable

data class Group(
    val id: String,
    val name: String,
    val members: ArrayList<GroupMember>
) : Serializable

data class GroupMember(
    val id: String,
    val userName: String
) : Serializable