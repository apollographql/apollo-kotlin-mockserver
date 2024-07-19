# Recipes

### Enqueue String responses

```kotlin
mockServer.enqueueString(
  string = "{\"status\":200}",
  contentType = "application/json"
)
```

### Enqueue Error responses

```kotlin
mockServer.enqueueError(500)
```

### Enqueue Multipart responses

````kotlin
mockServer.enqueueMultipart(
  partsContentType = "application/json"
).apply {
  enqueuePart("{\"part\": 1}".toByteString(), isLast = false)
  enqueuePart("{\"part\": 2}".toByteString(), isLast = false)
  // There **must** be a last part
  enqueuePart("{\"part\": 3}".toByteString(), isLast = true)
}

````

### Add an artificial delay

```kotlin
// Wait 1s before returning the response
mockServer.enqueue(
  MockResponse.Builder()
    .delayMillis(1000)
    .body("OK")
    .build()
)
```
