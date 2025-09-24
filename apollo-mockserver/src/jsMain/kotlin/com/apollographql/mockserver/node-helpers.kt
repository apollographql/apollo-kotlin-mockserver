package com.apollographql.mockserver

import org.khronos.webgl.ArrayBuffer
import org.khronos.webgl.Int8Array
import org.khronos.webgl.Uint8Array

fun ArrayBuffer.toByteArray(): ByteArray = Int8Array(this).unsafeCast<ByteArray>()


fun ByteArray.toUint8Array(): Uint8Array {
  val i8a = unsafeCast<Int8Array>()
  return Uint8Array(i8a.buffer, i8a.byteOffset, i8a.byteLength)
}