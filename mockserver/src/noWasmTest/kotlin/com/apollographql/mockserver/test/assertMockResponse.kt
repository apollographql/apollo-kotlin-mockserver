package com.apollographql.mockserver.test

import com.apollographql.mockserver.MockResponse
import com.apollographql.mockserver.readChunked
import io.ktor.client.call.*
import io.ktor.client.statement.*
import kotlinx.coroutines.flow.toList
import okio.Buffer
import kotlin.test.assertEquals
import kotlin.test.assertTrue

suspend fun assertMockResponse(
  mockResponse: MockResponse,
  httpResponse: HttpResponse,
) {
  val mockResponseBody = mockResponse.body.toList().fold(Buffer()) { buffer, byteString ->
    buffer.write(byteString)
  }.let {
    if (mockResponse.headers["Transfer-Encoding"] == "chunked") {
      Buffer().apply { it.readChunked(this) }
    } else {
      it
    }
  }.readByteString()

  assertEquals(mockResponseBody.utf8(), httpResponse.body())
  assertEquals(mockResponse.statusCode, httpResponse.status.value)
  // JS MockServer serves headers in lowercase, so convert before comparing
  // Also, remove 'Transfer-Encoding' header before comparison since it is changed by the Apple client
  val mockResponseHeaders = mockResponse.headers.map { it.key.lowercase() to it.value.lowercase() }.filterNot { (key, _) -> key == "transfer-encoding" }
  val httpResponseHeaders = httpResponse.headers.entries().map { it.key.lowercase() to it.value.single().lowercase() }.filterNot { (key, _) -> key == "transfer-encoding" }

  // Use contains because there can be more headers than requested in the response
  assertTrue(httpResponseHeaders.containsAll(mockResponseHeaders), "Headers do not match: httpResponseHeaders=$httpResponseHeaders mockResponseHeadersLowercase=$mockResponseHeaders")
}
