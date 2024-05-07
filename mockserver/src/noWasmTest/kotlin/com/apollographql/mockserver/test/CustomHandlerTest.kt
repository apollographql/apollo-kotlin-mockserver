package com.apollographql.mockserver.test

import com.apollographql.mockserver.MockRequestBase
import com.apollographql.mockserver.MockResponse
import com.apollographql.mockserver.MockServer
import com.apollographql.mockserver.MockServerHandler
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import kotlinx.coroutines.test.runTest
import okio.use
import kotlin.test.Test

class CustomHandlerTest {
  @Test
  fun customHandler() = runTest {

    val mockResponse0 = MockResponse.Builder()
        .body("Hello, World! 000")
        .statusCode(404)
        .addHeader("Content-Type", "text/plain")
        .build()
    val mockResponse1 = MockResponse.Builder()
        .body("Hello, World! 001")
        .statusCode(200)
        .addHeader("X-Test", "true")
        .build()

    val mockServerHandler = object : MockServerHandler {
      override fun handle(request: MockRequestBase): MockResponse {
        return when (request.path) {
          "/0" -> mockResponse0
          "/1" -> mockResponse1
          else -> error("Unexpected path: ${request.path}")
        }
      }
    }

    MockServer.Builder().handler(mockServerHandler).build().use { mockServer ->
        val client = HttpClient()

        var httpResponse = client.get(mockServer.url() + "1")
        assertMockResponse(mockResponse1, httpResponse)

        httpResponse = client.get(mockServer.url() + "0")
        assertMockResponse(mockResponse0, httpResponse)

        httpResponse = client.get(mockServer.url() + "1")
        assertMockResponse(mockResponse1, httpResponse)
    }
  }

}
