package com.apollographql.mockserver

import js.typedarrays.toUint8Array
import kotlinx.coroutines.channels.Channel
import node.buffer.Buffer
import node.events.Event
import node.net.AddressInfo
import node.net.createServer
import okio.IOException
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import node.net.Server as WrappedServer
import node.net.Socket as WrappedSocket

internal class NodeTcpSocket(private val netSocket: WrappedSocket) : TcpSocket {
  private val readQueue = Channel<ByteArray>(Channel.UNLIMITED)

  init {
    netSocket.on(Event.DATA) { chunk ->
      val bytes = when (chunk) {
        is String -> chunk.encodeToByteArray()
        is Buffer -> chunk.toByteArray()
        else -> error("Unexpected chunk type: ${chunk::class}")
      }
      readQueue.trySend(bytes)
    }

    netSocket.on(Event.CLOSE) { _ ->
      readQueue.close(IOException("The socket was closed"))
    }
  }

  override suspend fun receive(): ByteArray {
    return readQueue.receive()
  }

  override fun send(data: ByteArray) {
    // Enqueue everything
    netSocket.write(data.toUint8Array())
  }

  override fun close() {
    /**
     * [Event.CLOSE] will be invoked and the readQueue will be closed
     */
    netSocket.destroy()
  }
}

internal class NodeTcpServer(private val port: Int) : TcpServer {

  private var server: WrappedServer? = null
  private var address: Address? = null

  override fun listen(block: (socket: TcpSocket) -> Unit) {
    require(!isRunning()) { "Server is already running" }

    server = createServer { netSocket ->
      block(NodeTcpSocket(netSocket))
    }.apply {
      listen(port)
    }
  }

  override suspend fun address(): Address {
    require(isRunning()) { "Server is not running, please call listen() before calling address()" }

    return address ?: suspendCoroutine { cont ->
      server!!.once(Event.LISTENING) {
        val server = requireNotNull(server) {
          "close() was called during a call to address()"
        }

        val address = server.address().unsafeCast<AddressInfo>()
        this.address = Address(address.address, address.port.toInt()).also {
          cont.resume(it)
        }
      }
    }
  }

  override fun close() {
    server?.let {
      it.close()
      server = null
    }
  }

  override fun isRunning(): Boolean {
    return server != null
  }
}

actual fun TcpServer(port: Int): TcpServer = NodeTcpServer(port)
