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
class NonFunctionalTests {

    private val client = OkHttpClient()
    private val baseURL = URL("${BuildConfig.SERVER_URL}")
    private val baseUserId = BuildConfig.TEST_GOOGLE_ID
    private val baseGoogleId = BuildConfig.TEST_USER_ID

    private fun measureResponseTime(request: Request): Long {
        val startTime = System.currentTimeMillis()
        return try {
            val response = client.newCall(request).execute()
            val endTime = System.currentTimeMillis()
            val responseTime = endTime - startTime
            val statusCode = response.code
            println("Response time for ${request.url}: $responseTime ms, Status: $statusCode")
            response.close()
            responseTime
        } catch (e: IOException) {
            e.printStackTrace()
            Long.MAX_VALUE
        }
    }

    private fun assertResponseTime(url: String, maxResponseTime: Long) {
        val request = Request.Builder().url(url).build()
        val responseTime = measureResponseTime(request)
        assertTrue("Response time for $url is too high: $responseTime ms", responseTime < maxResponseTime)
    }

    // Test Cases for Auth endpoints

    @Test
    fun testNotificationDeviceTokenResponseTime() {
        val url = baseURL.toString() + "/notification/deviceToken"
        assertResponseTime(url, 500)
    }

    @Test
    fun testVerifyUserResponseTime() {
        val googleId = "validGoogleId" // Replace with a valid Google ID for testing
        val url = baseURL.toString() + "/auth/verify?googleId=$googleId"
        assertResponseTime(url, 200)
    }

    @Test
    fun testCreateOrUpdateUserResponseTime() {
        val url = baseURL.toString() + "/auth/google"
        val jsonBody = """
            {
                "googleId": "validGoogleId",
                "email": "test@example.com",
                "displayName": "Test User"
            }
        """.trimIndent()
        val requestBody = jsonBody.toRequestBody("application/json; charset=utf-8".toMediaType())
        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()
        val responseTime = measureResponseTime(request)
        assertResponseTime(url, 200)
    }

    @Test
    fun testUpdateUserProfileResponseTime() {
        val url = baseURL.toString() + "/auth/profile/$baseGoogleId"
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
        val request = Request.Builder()
            .url(url)
            .put(requestBody)
            .build()
        val responseTime = measureResponseTime(request)
        assertResponseTime(url, 200)
    }

    // Test Cases for Group endpoints

    @Test
    fun testCreateGroupResponseTime() {
        val url = baseURL.toString() + "/group"
        val jsonBody = """
            {
                "name": "Test Group",
                "description": "This is a test group",
                "members": ["member1", "member2"]
            }
        """.trimIndent()
        val requestBody = jsonBody.toRequestBody("application/json; charset=utf-8".toMediaType())
        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()
        val responseTime = measureResponseTime(request)
        assertResponseTime(url, 200)
    }

    @Test
    fun testGetGroupsResponseTime() {
        val userId = "validUserId" // Replace with a valid user ID for testing
        val url = baseURL.toString() + "/group/$userId"
        assertResponseTime(url, 200)
    }

    @Test
    fun testEditGroupResponseTime() {
        val groupId = "validGroupId" // Replace with a valid group ID for testing
        val url = baseURL.toString() + "/group/$groupId"
        val jsonBody = """
            {
                "name": "Updated Test Group",
                "description": "This is an updated test group",
                "members": ["member1", "member2", "member3"]
            }
        """.trimIndent()
        val requestBody = jsonBody.toRequestBody("application/json; charset=utf-8".toMediaType())
        val request = Request.Builder()
            .url(url)
            .put(requestBody)
            .build()
        val responseTime = measureResponseTime(request)
        assertResponseTime(url, 200)
    }

    @Test
    fun testDeleteGroupResponseTime() {
        val groupId = "validGroupId" // Replace with a valid group ID for testing
        val url = baseURL.toString() + "/group/$groupId"
        val request = Request.Builder()
            .url(url)
            .delete()
            .build()
        val responseTime = measureResponseTime(request)
        assertResponseTime(url, 200)
    }

    // Test Cases for User endpoints

    @Test
    fun testCreateUserResponseTime() {
        val url = baseURL.toString() + "/user"
        val jsonBody = """
            {
                "googleId": "validGoogleId",
                "email": "test@example.com",
                "displayName": "Test User"
            }
        """.trimIndent()
        val requestBody = jsonBody.toRequestBody("application/json; charset=utf-8".toMediaType())
        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()
        val responseTime = measureResponseTime(request)
        assertResponseTime(url, 200)
    }

    @Test
    fun testGetUserResponseTime() {
        val userId = "validUserId" // Replace with a valid user ID for testing
        val url = baseURL.toString() + "/user/$userId"
        assertResponseTime(url, 200)
    }

    @Test
    fun testEditUserResponseTime() {
        val userId = "validUserId" // Replace with a valid user ID for testing
        val url = baseURL.toString() + "/user/$userId"
        val jsonBody = """
            {
                "firstName": "Updated",
                "lastName": "User",
                "userName": "updateduser",
                "year": 3,
                "faculty": "Science"
            }
        """.trimIndent()
        val requestBody = jsonBody.toRequestBody("application/json; charset=utf-8".toMediaType())
        val request = Request.Builder()
            .url(url)
            .put(requestBody)
            .build()
        val responseTime = measureResponseTime(request)
        assertResponseTime(url, 200)
    }

    @Test
    fun testDeleteUserResponseTime() {
        val userId = "validUserId" // Replace with a valid user ID for testing
        val url = baseURL.toString() + "/user/$userId"
        val request = Request.Builder()
            .url(url)
            .delete()
            .build()
        val responseTime = measureResponseTime(request)
        assertResponseTime(url, 200)
    }

    // Test Cases for Notification endpoints

    @Test
    fun testAssociateDeviceResponseTime() {
        val url = baseURL.toString() + "/notification/deviceToken"
        val jsonBody = """
            {
                "deviceToken": "validDeviceToken",
                "userId": "validUserId"
            }
        """.trimIndent()
        val requestBody = jsonBody.toRequestBody("application/json; charset=utf-8".toMediaType())
        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()
        val responseTime = measureResponseTime(request)
        assertResponseTime(url, 200)
    }

    @Test
    fun testSendMessageTestResponseTime() {
        val url = baseURL.toString() + "/notification/sendMessageTest"
        val jsonBody = """
            {
                "message": "Test message",
                "userId": "validUserId"
            }
        """.trimIndent()
        val requestBody = jsonBody.toRequestBody("application/json; charset=utf-8".toMediaType())
        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()
        val responseTime = measureResponseTime(request)
        assertResponseTime(url, 200)
    }

    @Test
    fun testDeleteTokenResponseTime() {
        val url = baseURL.toString() + "/notification/deviceToken"
        val jsonBody = """
            {
                "deviceToken": "validDeviceToken",
                "userId": "validUserId"
            }
        """.trimIndent()
        val requestBody = jsonBody.toRequestBody("application/json; charset=utf-8".toMediaType())
        val request = Request.Builder()
            .url(url)
            .delete(requestBody)
            .build()
        val responseTime = measureResponseTime(request)
        assertResponseTime(url, 200)
    }

    // Test Cases for Session endpoints

    @Test
    fun testJoinSessionResponseTime() {
        val sessionId = "validSessionId" // Replace with a valid session ID for testing
        val url = baseURL.toString() + "/session/$sessionId/join"
        val jsonBody = """
            {
                "userId": "validUserId"
            }
        """.trimIndent()
        val requestBody = jsonBody.toRequestBody("application/json; charset=utf-8".toMediaType())
        val request = Request.Builder()
            .url(url)
            .put(requestBody)
            .build()
        val responseTime = measureResponseTime(request)
        assertResponseTime(url, 200)
    }

    @Test
    fun testHostSessionResponseTime() {
        val url = baseURL.toString() + "/session"
        val jsonBody = """
            {
                "name": "Test Session",
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
        """.trimIndent()
        val requestBody = jsonBody.toRequestBody("application/json; charset=utf-8".toMediaType())
        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()
        val responseTime = measureResponseTime(request)
        assertResponseTime(url, 200)
    }

    @Test
    fun testLeaveSessionResponseTime() {
        val sessionId = "validSessionId" // Replace with a valid session ID for testing
        val url = baseURL.toString() + "/session/$sessionId/leave"
        val jsonBody = """
            {
                "userId": "validUserId"
            }
        """.trimIndent()
        val requestBody = jsonBody.toRequestBody("application/json; charset=utf-8".toMediaType())
        val request = Request.Builder()
            .url(url)
            .put(requestBody)
            .build()
        val responseTime = measureResponseTime(request)
        assertResponseTime(url, 200)
    }

    @Test
    fun testDeleteSessionResponseTime() {
        val sessionId = "validSessionId" // Replace with a valid session ID for testing
        val url = baseURL.toString() + "/session/$sessionId"
        val request = Request.Builder()
            .url(url)
            .delete()
            .build()
        val responseTime = measureResponseTime(request)
        assertResponseTime(url, 200)
    }

    @Test
    fun testGetAvailableSessionsResponseTime() {
        val userId = "validUserId" // Replace with a valid user ID for testing
        val url = baseURL.toString() + "/session/availableSessions/$userId"
        assertResponseTime(url, 200)
    }

    @Test
    fun testGetJoinedSessionsResponseTime() {
        val url = baseURL.toString() + "/session/joinedSessions"
        assertResponseTime(url, 200)
    }

    @Test
    fun testGetHostedSessionsResponseTime() {
        val url = baseURL.toString() + "/session/hostedSessions"
        assertResponseTime(url, 200)
    }

    @Test
    fun testGetNearbySessionsResponseTime() {
        val url = baseURL.toString() + "/session/nearbySessions"
        assertResponseTime(url, 200)
    }
}