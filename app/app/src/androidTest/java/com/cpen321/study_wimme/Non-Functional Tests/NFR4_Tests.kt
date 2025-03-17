package com.cpen321.study_wimme

import androidx.test.ext.junit.runners.AndroidJUnit4
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException
import java.net.URL

@RunWith(AndroidJUnit4::class)
class ErrorRecoveryTests {

    private val client = OkHttpClient()
    private val baseURL = URL("${BuildConfig.SERVER_URL}")

    private fun simulateError(request: Request): Int {
        return try {
            val response = client.newCall(request).execute()
            val statusCode = response.code
            response.close()
            statusCode
        } catch (e: IOException) {
            e.printStackTrace()
            -1
        }
    }

    @Test
    fun testVerifyUserMissingGoogleId() {
        val url = baseURL.toString() + "/auth/verify"
        val request = Request.Builder().url(url).build()
        val statusCode = simulateError(request)
        assertTrue("Expected 400 Bad Request, but got status code: $statusCode", statusCode == 400)
    }

    @Test
    fun testCreateOrUpdateUserMissingFields() {
        val url = baseURL.toString() + "/auth/google"
        val jsonBody = """
            {
                "email": "test@example.com",
                "displayName": "Test User"
            }
        """.trimIndent() // Missing googleId
        val requestBody = jsonBody.toRequestBody("application/json; charset=utf-8".toMediaType())
        val request = Request.Builder().url(url).post(requestBody).build()
        val statusCode = simulateError(request)
        assertTrue("Expected 400 Bad Request, but got status code: $statusCode", statusCode == 400)
    }

    @Test
    fun testUpdateUserProfileMissingGoogleId() {
        val url = baseURL.toString() + "/auth/profile/"
        val jsonBody = """
            {
                "firstName": "Test",
                "lastName": "User",
                "userName": "testuser",
                "year": 2,
                "faculty": "Engineering"
            }
        """.trimIndent()
        val requestBody = jsonBody.toRequestBody("application/json; charset=utf-8".toMediaType())
        val request = Request.Builder().url(url).put(requestBody).build()
        val statusCode = simulateError(request)
        assertTrue("Expected 404 Not Found, but got status code: $statusCode", statusCode == 404)
    }

    @Test
    fun testCreateGroupMissingFields() {
        val url = baseURL.toString() + "/group"
        val jsonBody = """
        {
            "description": "This is a test group"
        }
    """.trimIndent() // Missing name and members
        val requestBody = jsonBody.toRequestBody("application/json; charset=utf-8".toMediaType())
        val request = Request.Builder().url(url).post(requestBody).build()
        val statusCode = simulateError(request)
        assertTrue("Expected 400 Bad Request, but got status code: $statusCode", statusCode == 400)
    }

    @Test
    fun testGetGroupsMissingUserId() {
        val url = baseURL.toString() + "/group/"
        val request = Request.Builder().url(url).build()
        val statusCode = simulateError(request)
        assertTrue("Expected 404 Not Found, but got status code: $statusCode", statusCode == 404)
    }

    @Test
    fun testAssociateDeviceMissingFields() {
        val url = baseURL.toString() + "/notification/deviceToken"
        val jsonBody = """
        {
            "userId": "validUserId"
        }
    """.trimIndent() // Missing deviceToken
        val requestBody = jsonBody.toRequestBody("application/json; charset=utf-8".toMediaType())
        val request = Request.Builder().url(url).post(requestBody).build()
        val statusCode = simulateError(request)
        assertTrue("Expected 500 Bad Request, but got status code: $statusCode", statusCode == 500)
    }

    @Test
    fun testHostSessionMissingFields() {
        val url = baseURL.toString() + "/session"
        val jsonBody = """
        {
            "description": "This is a test session",
            "hostId": "validHostId",
            "location": {
                "type": "Point",
                "coordinates": [123.456, 78.910]
            },
            "dateRange": {
                "start": "2025-03-15T00:00:00Z",
                "end": "2025-03-15T01:00:00Z"
            },
            "isPublic": true,
            "subject": "Test Subject",
            "faculty": "Engineering",
            "year": 2
        }
    """.trimIndent() // Missing name
        val requestBody = jsonBody.toRequestBody("application/json; charset=utf-8".toMediaType())
        val request = Request.Builder().url(url).post(requestBody).build()
        val statusCode = simulateError(request)
        assertTrue("Expected 400 Bad Request, but got status code: $statusCode", statusCode == 400)
    }

    @Test
    fun testDeleteSessionMissingSessionId() {
        val url = baseURL.toString() + "/session/"
        val request = Request.Builder().url(url).delete().build()
        val statusCode = simulateError(request)
        assertTrue("Expected 404 Not Found, but got status code: $statusCode", statusCode == 404)
    }

}