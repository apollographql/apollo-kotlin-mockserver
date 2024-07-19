# WebSockets

Apollo Kotlin MockServer supports WebSockets

### Mock a WebSocket session

To enqueue a WebSocket response, use `MockServer.enqueueWebSocket`:

```kotlin
val responseBody = mockServer.enqueueWebSocket()
```

When your client sends a request, you can dequeue it with `MockServer.awaitWebSocketRequest()`:

```kotlin
val requestBody = mockServer.awaitWebSocketRequest()
```

`responseBody` and `requestBody` allow enqueuing server messages and dequeuing client responses respectively:

```kotlin
val responseBody = mockServer.enqueueWebSocket()

// Client code under test
connectClient(mockServer.url)

val requestBody = mockServer.awaitWebSocketRequest()

responseBody.enqueueMessage(TextMessage("Hello from Server"))

// Client code under test
sendClientMessage("Hello from client")

requestBody.awaitMessage().apply {
  assertIs<TextMessage>(this)
  assertEquals("Hello from client", text)
}
```

