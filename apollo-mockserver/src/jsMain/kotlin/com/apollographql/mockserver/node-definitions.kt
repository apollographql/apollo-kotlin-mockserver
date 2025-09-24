package com.apollographql.mockserver

import org.khronos.webgl.ArrayBuffer
import org.khronos.webgl.Uint8Array

/**
 * https://nodejs.org/docs/latest/api/buffer.html
 */
external interface Buffer {
  val buffer: ArrayBuffer
}

/***
 * https://nodejs.org/docs/latest/api/net.html
 */
external interface Socket {
  fun on(event: String, listener: (dynamic) -> Unit)
  fun destroy()
  fun write(
    buffer: Uint8Array,
    callback: (dynamic) -> Unit,
  ): Boolean
}

external interface Server {
  fun listen(port: Int)
  fun close()
  fun address(): Any?
  fun on(event: String, listener: (dynamic) -> Unit)
}

external interface AddressInfo {
  var address: String
  var family: String
  var port: Double
}