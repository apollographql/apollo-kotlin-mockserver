package com.apollographql.apollo3.mockserver.test

import com.apollographql.apollo3.mockserver.MockServer
import com.apollographql.apollo3.mockserver.enqueueMultipart
import com.apollographql.apollo3.mockserver.enqueueStrings
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import kotlinx.coroutines.test.runTest
import okhttp3.MultipartReader
import okio.Buffer
import kotlin.test.Test
import kotlin.test.assertEquals

class MultipartTest {
  /**
   * Check that the multipart encoded by MockServer can be decoded by OkHttp's MultipartReader.
   */
  @Test
  fun receiveAndDecodeParts() = runTest {
    val part0 = """{"data":{"song":{"firstVerse":"Now I know my ABC's."}},"hasNext":true}"""
    val part1 = """{"data":{"secondVerse":"Next time won't you sing with me?"},"path":["song"],"hasNext":false}"""
    val boundary = "-"

    MockServer().use { mockServer ->
      mockServer.enqueueMultipart(boundary = boundary, partsContentType = "application/json; charset=utf-8")
          .enqueueStrings(listOf(part0, part1))

      val httpResponse = HttpClient().get(mockServer.url())

      val parts = mutableListOf<MultipartReader.Part>()
      val partBodies = mutableListOf<String>()
      // Taken from https://square.github.io/okhttp/4.x/okhttp/okhttp3/-multipart-reader/
      val source = Buffer().writeUtf8(httpResponse.body<String>())
      val multipartReader = MultipartReader(source, boundary = boundary)
      multipartReader.use {
        while (true) {
          val part = multipartReader.nextPart() ?: break
          parts.add(part)
          partBodies.add(part.body.readUtf8())
        }
      }

      assertEquals("application/json; charset=utf-8", parts[0].headers["Content-Type"])
      assertEquals(part0.length.toString(), parts[0].headers["Content-Length"])
      assertEquals(part0, partBodies[0])

      assertEquals("application/json; charset=utf-8", parts[1].headers["Content-Type"])
      assertEquals(part1.length.toString(), parts[1].headers["Content-Length"])
      assertEquals(part1, partBodies[1])
    }
  }
}
