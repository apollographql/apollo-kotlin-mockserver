# Getting-started

Apollo MockServer is a [KMP](https://kotlinlang.org/docs/multiplatform.html) server for your HTTP and WebSocket tests.

**Features:**
* Enqueue mocked HTTP responses
* Dequeue recorded HTTP requests
* Enqueue mocked WebSocket messages
* Dequeue recorded WebSocket messages
* JVM, Native and JS (Node) support

Apollo MockServer was initially developed for [Apollo Kotlin](https://github.com/apollographql/apollo-kotlin/) integration tests and is provided as-is to the community.

Performance and compatibility are minimal. Do not use in production.

**Non-goals:**
* HTTP2/HTTP3
* HTTPS/TLS
* Performance

Should a future version of [Ktor](https://ktor.io/docs/server-platforms.html) or any other server framework provide a `commonMain` API, Apollo Kotlin MockServer would probably become deprecated.

In the meantime, add the dependency to your project and enjoy cross-platform integration tests!

```toml
[libraries]
apollo-mockserver = "com.apollographql.mockserver:apollo-mockserver:%latest_version%"
```

### Enqueuing mock responses

To enqueue responses, use `MockServer.enqueue`:

```kotlin
// .use {} makes sure to release the resources at the end of the test
MockServer().use { mockServer ->
  mockServer.enqueue(
    MockResponse.Builder()
      .statusCode(200)
      .body("Hello World")
      .build()
  )
  
  // You can enqueue multiple responses if needed
}
```

> `MockServer` holds resources and must be closed after use.  `.use {}` does it automatically.
{style="note"}


### Reading recorded requests

Use `MockServer.awaitRequest()` to retrieve a recorded request that you can assert to check the behaviour of your client code.

```kotlin
// .use {} makes sure to release the resources at the end of the test
MockServer().use { mockServer ->
  mockServer.enqueue(...)
  
  // Client code under test
  doHttpRequest(mockServer.url())

  mockServer.awaitRequest().apply {
    // Assert the request here
    // assertEquals("GET", method)
    // ...
  }
}
```

### Next steps

Take a look at the [Recipes](recipes.md) or [WebSockets](websockets.md) pages to learn more.