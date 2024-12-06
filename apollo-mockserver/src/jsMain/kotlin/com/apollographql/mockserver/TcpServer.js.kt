package com.apollographql.mockserver

import js.typedarrays.toUint8Array
import kotlinx.coroutines.channels.Channel
import node.events.once
import node.net.AddressInfo
import node.net.createServer
import okio.IOException
import org.w3c.dom.events.Event
import node.net.Server as WrappedServer
import node.net.Socket as WrappedSocket

internal class NodeTcpSocket(private val netSocket: WrappedSocket) : TcpSocket {
  private val readQueue = Channel<ByteArray>(Channel.UNLIMITED)

  init {
    netSocket.dataEvent.addHandler { (chunk) ->
      readQueue.trySend(chunk.toByteArray())
    }

    netSocket.closeEvent.addHandler { _ ->
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
  private var isClosed = false

  override fun listen(block: (socket: TcpSocket) -> Unit) {
    require(!isClosed) { "Server is closed and cannot be restarted" }
    require(server == null) { "Server is already started" }

    server = createServer { netSocket ->
      block(NodeTcpSocket(netSocket))
    }.apply {
      listen(port)
    }
  }

  override suspend fun address(): Address {
    require(!isClosed) { "Server is closed" }
    val server = requireNotNull(this.server) { "Server is not listening, please call listen() before calling address()" }

    if (address != null) {
      return address!!
    }

    server.listeningEvent.once()

    val ai = server.address().unsafeCast<AddressInfo>()
    address = Address(ai.address, ai.port.toInt())
    return address!!
  }

  override fun close() {
    if(isClosed) return

    server?.let { server ->
      server.close()
      this.server = null
      // Is closed only if the server was started before
      isClosed = true
    }
  }
}

actual fun TcpServer(port: Int): TcpServer = NodeTcpServer(port)
