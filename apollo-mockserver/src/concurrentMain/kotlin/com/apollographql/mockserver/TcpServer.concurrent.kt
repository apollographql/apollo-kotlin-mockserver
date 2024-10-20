package com.apollographql.mockserver

import io.ktor.network.selector.SelectorManager
import io.ktor.network.sockets.InetSocketAddress
import io.ktor.network.sockets.ServerSocket
import io.ktor.network.sockets.aSocket
import io.ktor.network.sockets.openReadChannel
import io.ktor.network.sockets.openWriteChannel
import io.ktor.utils.io.readAvailable
import io.ktor.utils.io.writeFully
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import okio.IOException
import io.ktor.network.sockets.Socket as WrappedSocket

actual fun TcpServer(port: Int): TcpServer = KtorTcpServer(port)

class KtorTcpServer(
  private val port: Int = 0,
  private val acceptDelayMillis: Int = 0,
  private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : TcpServer {

  private var selectorManager: SelectorManager? = null
  private var serverScope: CoroutineScope? = null
  private var serverSocket: ServerSocket? = null

  override fun listen(block: (socket: TcpSocket) -> Unit) {
    require(!isRunning()) { "Server is already running" }

    val selectorManager = createSelectorManager().also { this.selectorManager = it }
    val serverScope = createServerScope().also { this.serverScope = it }

    serverScope.launch {
      val serverSocket = createServerSocket(selectorManager).also { serverSocket = it }

      while (isActive) {
        if (acceptDelayMillis > 0) {
          delay(acceptDelayMillis.toLong())
        }
        val socket: WrappedSocket = try {
          serverSocket.accept()
        } catch (t: Throwable) {
          if (t is CancellationException) {
            throw t
          }
          delay(1000)
          continue
        }

        val ktorSocket = KtorTcpSocket(socket).apply(block)
        launch {
          ktorSocket.loop()
        }
      }
    }
  }

  override suspend fun address(): Address {
    require(isRunning()) { "Server is not running, please call listen() before calling address()" }

    return withTimeout(1000) {
      var address: Address
      while (true) {
        val serverSocket = requireNotNull(serverSocket) {
          "close() was called during a call to address()"
        }

        try {
          address = (serverSocket.localAddress as InetSocketAddress).let {
            Address(it.hostname, it.port)
          }
          break
        } catch (e: Exception) {
          delay(50)
          continue
        }
      }
      address
    }
  }

  override fun close() {
    serverScope?.let {
      it.cancel()
      serverScope = null
    }
    selectorManager?.let {
      it.close()
      selectorManager = null
    }
    serverSocket?.let {
      it.close()
      serverSocket = null
    }
  }

  override fun isRunning(): Boolean {
    return serverSocket != null && serverScope?.isActive == true && selectorManager?.isActive == true
  }

  /**
   * Creates a new [SelectorManager] instance using the provided [dispatcher].
   * @return a new [SelectorManager] instance
   */
  private fun createSelectorManager(): SelectorManager = SelectorManager(dispatcher)

  /**
   * Creates a new [CoroutineScope] instance using the provided [dispatcher].
   * [SupervisorJob] is used to ensure that the scope is not cancelled when a child job fails.
   * @return a new [CoroutineScope] instance
   */
  private fun createServerScope(): CoroutineScope = CoroutineScope(SupervisorJob() + dispatcher)

  /**
   * Creates a new [ServerSocket] instance using the provided [selectorManager].
   * Starts listening on the provided [port] and binds to localhost.
   * @return a new [ServerSocket] instance
   */
  private suspend fun createServerSocket(selectorManager: SelectorManager): ServerSocket =
    aSocket(selectorManager).tcp().bind("127.0.0.1", port)
}

internal class KtorTcpSocket(private val socket: WrappedSocket) : TcpSocket {
  private val receiveChannel = socket.openReadChannel()
  private val writeChannel = socket.openWriteChannel()

  private val writeQueue = Channel<ByteArray>(Channel.UNLIMITED)
  private val readQueue = Channel<ByteArray>(Channel.UNLIMITED)

  private suspend fun readLoop() {
    val buffer = ByteArray(8192)
    while (true) {
      val ret = receiveChannel.readAvailable(buffer, 0, buffer.size)
      if (ret == -1) {
        readQueue.close(IOException("Error reading socket", receiveChannel.closedCause))
        break
      } else if (ret > 0) {
        readQueue.send(ByteArray(ret) { buffer[it] })
      }
    }
  }

  private suspend fun writeLoop() {
    while (true) {
      val data = writeQueue.receive()
      writeChannel.writeFully(data, 0, data.size)
      writeChannel.flush()
    }
  }

  suspend fun loop() = coroutineScope {
    launch {
      readLoop()
      // Whenever the socket get closed, cancel the writeLoop
      this@coroutineScope.cancel()
    }
    launch {
      writeLoop()
    }
  }

  override fun close() {
    socket.close()
  }

  override suspend fun receive(): ByteArray {
    return readQueue.receive()
  }

  override fun send(data: ByteArray) {
    writeQueue.trySend(data)
  }
}
