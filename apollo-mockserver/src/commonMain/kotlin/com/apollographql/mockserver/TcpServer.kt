package com.apollographql.mockserver

import okio.Closeable

interface TcpSocket: Closeable {
  /**
   * Suspend until data is received and returns any available data
   *
   * @throws [okio.IOException] if there is an error reading data
   */
  suspend fun receive(): ByteArray

  /**
   * Schedules data to be sent.
   *
   * Data is buffered unbounded.
   *
   * There is no guarantee that the data is actually transmitted or processed by the remote side.
   */
  fun send(data: ByteArray)

  /**
   * Closes the socket.
   *
   * Sends TCP FIN packet.
   * Pending or subsequent [receive] calls throw [okio.IOException]
   */
  override fun close()
}

/**
 * A server that handles [TcpSocket]
 * [TcpServer] is not thread safe and its method must be called from the same coroutine.
 */
interface TcpServer : Closeable {
  /**
   * Starts a background coroutine that calls [block] on incoming connections.
   * [listen] can only be called once.
   */
  suspend fun listen(block: (socket: TcpSocket) -> Unit)

  /**
   * Returns the local address the server is bound to. Only valid after calling [listen]
   */
  suspend fun address(): Address

  /**
   * Closes the server.
   * [close] is idempotent.
   */
  override fun close()
}

class Address(
    val hostname: String,
    val port: Int
)

expect fun TcpServer(port: Int): TcpServer
