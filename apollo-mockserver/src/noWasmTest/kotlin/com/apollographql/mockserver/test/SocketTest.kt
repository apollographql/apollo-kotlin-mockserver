package com.apollographql.mockserver.test

import com.apollographql.mockserver.MockServer
import com.apollographql.mockserver.enqueueString
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class SocketTest {
  @Test
  fun writeMoreThan8kToTheSocket() = runTest {
    val mockServer = MockServer()

    val builder = StringBuilder()
    0.until(10000).forEach {
      builder.append("aa")
    }

    mockServer.enqueueString(builder.toString())
    val str = builder.toString()
    val client = HttpClient()
    val response = client.get(mockServer.url())

    assertEquals(str, response.body())

    mockServer.close()
  }
}
