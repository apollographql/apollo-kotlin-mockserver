package com.apollographql.mockserver.test

import com.apollographql.mockserver.MockResponse
import com.apollographql.mockserver.MockServer
import com.apollographql.mockserver.asChunked
import io.ktor.client.*
import io.ktor.client.request.*
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import okio.ByteString.Companion.encodeUtf8
import okio.use
import kotlin.test.Test

class EnqueueTest {
  @Test
  fun enqueue() = runTest {
    val mockResponses = listOf(
        MockResponse.Builder()
            .body("Hello, World! 000")
            .statusCode(404)
            .addHeader("Content-Type", "text/plain")
            .build(),
        MockResponse.Builder()
            .body("Hello, World! 001")
            .statusCode(200)
            .addHeader("X-Test", "true")
            .build(),
        MockResponse.Builder()
            .body("ä".trimIndent())
            .build(),
        MockResponse.Builder()
            .body(flowOf("First chunk\n".encodeUtf8(), "Second chunk".encodeUtf8()).asChunked())
            .statusCode(200)
            .addHeader("X-Test", "false")
            .addHeader("Transfer-Encoding", "chunked")
            .build(),
    )

    MockServer().use { mockServer ->
      for (mockResponse in mockResponses) {
        mockServer.enqueue(mockResponse)
      }

      val httpClient = HttpClient()
      for (mockResponse in mockResponses) {
        val httpResponse = httpClient.get(mockServer.url())
        assertMockResponse(mockResponse, httpResponse)
      }
    }
  }
}
