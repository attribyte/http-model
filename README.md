## Attribyte HTTP Model

An immutable HTTP request/response model with swappable client implementations. Requires Java 17+.

### Features

- Immutable `Request` and `Response` objects built via type-safe builders
- Two client implementations: **JDK** (`java.net.http.HttpClient`) and **Jetty** (Eclipse Jetty 12)
- Async support with both `ListenableFuture` and `CompletableFuture`
- Streaming responses (Jetty client)
- Request/response timing and statistics
- Servlet bridges for both `javax.servlet` and `jakarta.servlet`

### Client Implementations

#### JdkClient

Uses the built-in `java.net.http.HttpClient`. No additional dependencies beyond the JDK and Guava.

```java
JdkClient client = new JdkClient(ClientOptions.builder().build());
```

#### JettyClient

Uses Eclipse Jetty 12. Adds streaming response support and detailed request/response statistics.
Requires `org.eclipse.jetty:jetty-client` as a dependency.

```java
JettyClient client = new JettyClient(ClientOptions.builder().build());
```

Both implement `AsyncClient`, which provides `asyncSend` (returns `ListenableFuture<Response>`)
and `completableSend` (returns `CompletableFuture<Response>`) in addition to synchronous `send`.

### Usage

#### Building and Sending Requests

```java
// GET
Request get = new GetRequestBuilder("https://example.com/api/resource").create();
Response response = client.send(get);

// POST with body
Request post = new PostRequestBuilder("https://example.com/api/resource", body)
        .addHeader("Content-Type", "application/json")
        .create();

// POST with form parameters
Request form = new FormPostRequestBuilder("https://example.com/api/login")
        .addParameter("username", "user")
        .addParameter("password", "pass")
        .create();

// PUT, PATCH, DELETE, HEAD, OPTIONS follow the same pattern
```

#### Reading Responses

```java
Response response = client.send(request);
int status = response.getStatusCode();
String body = response.getBody().toStringUtf8();
String contentType = response.getContentType();
Header header = response.getHeader("X-Custom-Header");
```

#### Async

```java
CompletableFuture<Response> future = client.completableSend(request);
future.thenAccept(response -> System.out.println(response.getStatusCode()));
```

#### Request Options

```java
RequestOptions options = new RequestOptions(
        30000,  // max response bytes
        10000,  // connection timeout ms
        30000,  // request timeout ms
        true,   // follow redirects
        false   // trust all certificates
);
Response response = client.send(request, options);
```

### Servlet Bridges

Convert between servlet requests/responses and the HTTP model:

```java
// javax.servlet
Request request = org.attribyte.api.http.impl.servlet.Bridge.fromServletRequest(servletRequest, maxBodyBytes);
org.attribyte.api.http.impl.servlet.Bridge.sendServletResponse(response, servletResponse);

// jakarta.servlet
Request request = org.attribyte.api.http.impl.jakarta.Bridge.fromServletRequest(servletRequest, maxBodyBytes);
org.attribyte.api.http.impl.jakarta.Bridge.sendServletResponse(response, servletResponse);
```

### Documentation

* [Javadoc](https://attribyte.github.io/http-model/)

### License

Copyright 2014-2026 [Attribyte Labs, LLC](https://attribyte.com)

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

[http://www.apache.org/licenses/LICENSE-2.0](http://www.apache.org/licenses/LICENSE-2.0)

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and limitations under the License.
