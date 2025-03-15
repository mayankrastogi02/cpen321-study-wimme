package com.cpen321.study_wimme.data

import com.cpen321.study_wimme.data.model.LoggedInUser
import java.io.IOException

/**
 * Class that handles authentication w/ login credentials and retrieves user information.
 */
class LoginDataSource {

    fun login(username: String, password: String): Result<LoggedInUser> {
        return try {
            // TODO: handle loggedInUser authentication
            val fakeUser = LoggedInUser(java.util.UUID.randomUUID().toString(), "Jane Doe")
            Result.Success(fakeUser)
        } catch (e: IOException) {
            Result.Error(IOException("Network error logging in", e))
        }
    }

    fun logout() {
        // TODO: revoke authentication
    }
}