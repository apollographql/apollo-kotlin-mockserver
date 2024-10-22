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
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import okio.IOException
import io.ktor.network.sockets.Socket as WrappedSocket

actual fun TcpServer(port: Int): TcpServer = KtorTcpServer(port)

class KtorTcpServer(
  private val port: Int = 0,
  private val acceptDelayMillis: Int = 0,
  dispatcher: CoroutineDispatcher = Dispatchers.IO
) : TcpServer {

  private val selectorManager = SelectorManager(dispatcher)
  private val serverScope = CoroutineScope(SupervisorJob() + dispatcher)
  private var serverSocket: ServerSocket = runBlocking {
    // Since Ktor 3.0.0, "bind" is a suspend function
    // https://youtrack.jetbrains.com/issue/KTOR-7605
    // We need to call "bind" during the constructor to keep the same behavior as before
    aSocket(selectorManager).tcp().bind("127.0.0.1", port)
  }
  private var serverJob: Job? = null

  override fun listen(block: (socket: TcpSocket) -> Unit) {
    require(serverScope.isActive) { "Server is closed and cannot be restarted" }
    require(serverJob == null) { "Server is already started" }

    serverJob = serverScope.launch {
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
    require(serverScope.isActive) { "Server is closed" }
    requireNotNull(serverJob) { "Server is not listening, please call listen() before calling address()" }

    return withTimeout(1000) {
      var address: Address? = null
      while (address == null) {
        val serverSocket = requireNotNull(serverSocket) {
          "close() was called during a call to address()"
        }

        try {
          address = (serverSocket.localAddress as InetSocketAddress).let {
            Address(it.hostname, it.port)
          }
        } catch (_: Exception) {
          delay(50)
          continue
        }
      }
      address
    }
  }

  override fun close() {
    serverJob?.let { serverJob ->
      // Cancel the server scope only if the server was started before
      // If the server was not started, the scope keeps being active and server can be started for the first time
      // Otherwise, the scope is cancelled to prevent the server from being started again
      serverScope.cancel()
      selectorManager.close()
      serverSocket.close()
      serverJob.cancel()
      this.serverJob = null
    }
  }
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
