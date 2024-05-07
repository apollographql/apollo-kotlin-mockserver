package com.apollographql.mockserver.test

import com.apollographql.mockserver.Reader
import com.apollographql.mockserver.TextMessage
import com.apollographql.mockserver.WebSocketMessage
import com.apollographql.mockserver.WebsocketMockRequest
import com.apollographql.mockserver.readFrames
import com.apollographql.mockserver.webSocketAccept
import kotlinx.coroutines.test.runTest
import okio.Buffer
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

private fun ByteArray.toReader(): Reader {
  val buffer = Buffer().write(this)

  return object : Reader {
    override val buffer: Buffer
      get() = buffer

    override suspend fun fillBuffer() {
      throw Exhausted
    }
  }
}

private object Exhausted : Exception("Buffer is exhausted")

class ReadFrameTest {
  // Samples from https://datatracker.ietf.org/doc/html/rfc6455#section-5.7
  @Test
  fun readSingleMessage() = runTest {
    // single frame, non-masked
    assertSingleMessage(byteArrayOf(0x81.toByte(), 0x05, 0x48, 0x65, 0x6c, 0x6c, 0x6f)) {
      assertIs<TextMessage>(it)
      assertEquals("Hello", it.text)
    }
    // single frame, masked
    assertSingleMessage(byteArrayOf(0x81.toByte(), 0x85.toByte(), 0x37, 0xfa.toByte(), 0x21, 0x3d, 0x7f, 0x9f.toByte(), 0x4d, 0x51, 0x58)) {
      assertIs<TextMessage>(it)
      assertEquals("Hello", it.text)
    }
    // fragmented frame, non-masked
    assertSingleMessage(byteArrayOf(0x01, 0x03, 0x48, 0x65, 0x6c, 0x80.toByte(), 0x02, 0x6c, 0x6f)) {
      assertIs<TextMessage>(it)
      assertEquals("Hello", it.text)
    }
  }

  @Test
  fun testAccept() {
    // from https://datatracker.ietf.org/doc/html/rfc6455#section-4.2.2
    val accept = webSocketAccept(
        com.apollographql.mockserver.WebsocketMockRequest(
            "GET",
            "/",
            "1.1",
            headers = mapOf(
                "Sec-WebSocket-Key" to "dGhlIHNhbXBsZSBub25jZQ==",
            )
        )
    )

    assertEquals("s3pPLMBiTxaQ9kYGzzhZRbK+xOo=", accept)
  }

  private suspend fun assertSingleMessage(bytes: ByteArray, assert: (WebSocketMessage) -> Unit) {
    val messages = mutableListOf<WebSocketMessage>()
    try {
      readFrames(bytes.toReader()) {
        messages.add(it)
      }
      error("An exception was expected")
    } catch (_: Exhausted) {

    }

    assertEquals(1, messages.size)
    assert(messages.first())
  }
}
