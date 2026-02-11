/*
 * Copyright 2024 Attribyte, LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *
 */

package org.attribyte.api.http.impl.jdk;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import org.attribyte.api.InitializationException;
import org.attribyte.api.Logger;
import org.attribyte.api.http.AsyncClient;
import org.attribyte.api.http.ClientOptions;
import org.attribyte.api.http.Header;
import org.attribyte.api.http.Parameter;
import org.attribyte.api.http.RequestOptions;
import org.attribyte.api.http.Response;
import org.attribyte.api.http.ResponseBuilder;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ProxySelector;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Collection;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * HTTP client implementation using the JDK {@code java.net.http.HttpClient}.
 * <p>
 *   Requires Java 11+. No external dependencies beyond the JDK and Guava.
 * </p>
 */
public class JdkClient implements AsyncClient {

   /**
    * Creates an <em>uninitialized</em> client.
    */
   public JdkClient() {
   }

   /**
    * Creates a client with specified options.
    * @param options The options.
    * @throws InitializationException if options are invalid or other initialization error.
    */
   public JdkClient(final ClientOptions options) throws InitializationException {
      initFromOptions(options);
   }

   private void initFromOptions(final ClientOptions options) throws InitializationException {
      HttpClient.Builder builder = HttpClient.newBuilder();

      if(options.connectionTimeoutMillis > 0) {
         builder.connectTimeout(Duration.ofMillis(options.connectionTimeoutMillis));
      }
      // Default to following redirects. Per-request redirect control
      // is handled by creating separate client instances if needed.
      builder.followRedirects(HttpClient.Redirect.NORMAL);
      this.defaultFollowRedirects = options.followRedirects;

      if(options != ClientOptions.IMPLEMENTATION_DEFAULT && options.proxyHost != null) {
         builder.proxy(ProxySelector.of(
                 new InetSocketAddress(options.proxyHost, options.proxyPort)));
      }

      this.executor = Executors.newCachedThreadPool();
      builder.executor(this.executor);
      this.httpClient = builder.build();
      this.options = options;
   }

   @Override
   public void init(String prefix, Properties props, Logger logger) throws InitializationException {
      if(isInit.compareAndSet(false, true)) {
         initFromOptions(new ClientOptions(prefix, props));
      }
   }

   @Override
   public Response send(org.attribyte.api.http.Request request) throws IOException {
      return send(request, RequestOptions.DEFAULT);
   }

   @Override
   public Response send(org.attribyte.api.http.Request request, RequestOptions options) throws IOException {
      try {
         HttpRequest httpRequest = toJdkRequest(request, options);
         HttpResponse<byte[]> httpResponse = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofByteArray());
         return toResponse(httpResponse);
      } catch(InterruptedException ie) {
         Thread.currentThread().interrupt();
         throw new IOException(ie);
      }
   }

   @Override
   public ListenableFuture<Response> asyncSend(org.attribyte.api.http.Request request) {
      return asyncSend(request, RequestOptions.DEFAULT);
   }

   @Override
   public ListenableFuture<Response> asyncSend(org.attribyte.api.http.Request request, RequestOptions options) {
      final SettableFuture<Response> fut = SettableFuture.create();
      HttpRequest httpRequest = toJdkRequest(request, options);
      httpClient.sendAsync(httpRequest, HttpResponse.BodyHandlers.ofByteArray())
              .thenAccept(httpResponse -> {
                 try {
                    fut.set(toResponse(httpResponse));
                 } catch(Exception e) {
                    fut.setException(e);
                 }
              })
              .exceptionally(throwable -> {
                 fut.setException(throwable);
                 return null;
              });
      return fut;
   }

   @Override
   public CompletableFuture<Response> completableSend(org.attribyte.api.http.Request request) {
      return completableSend(request, RequestOptions.DEFAULT);
   }

   @Override
   public CompletableFuture<Response> completableSend(org.attribyte.api.http.Request request, RequestOptions options) {
      HttpRequest httpRequest = toJdkRequest(request, options);
      return httpClient.sendAsync(httpRequest, HttpResponse.BodyHandlers.ofByteArray())
              .thenApply(this::toResponse);
   }

   @Override
   public void shutdown() {
      if(executor != null) {
         executor.shutdownNow();
      }
   }

   private HttpRequest toJdkRequest(org.attribyte.api.http.Request request, RequestOptions options) {
      HttpRequest.Builder builder = HttpRequest.newBuilder(request.getURI());

      if(options.timeoutSeconds > 0) {
         builder.timeout(Duration.ofSeconds(options.timeoutSeconds));
      }

      HttpRequest.BodyPublisher bodyPublisher = HttpRequest.BodyPublishers.noBody();

      switch(request.getMethod()) {
         case GET:
            builder.GET();
            break;
         case HEAD:
            builder.method("HEAD", HttpRequest.BodyPublishers.noBody());
            break;
         case DELETE:
            builder.DELETE();
            break;
         case POST: {
            Collection<Parameter> parameters = request.getParameters();
            if(!parameters.isEmpty()) {
               String formBody = encodeFormParameters(parameters);
               bodyPublisher = HttpRequest.BodyPublishers.ofString(formBody);
               builder.header("Content-Type", "application/x-www-form-urlencoded");
            } else if(request.getBody() != null) {
               bodyPublisher = HttpRequest.BodyPublishers.ofByteArray(request.getBody().toByteArray());
            }
            builder.POST(bodyPublisher);
            break;
         }
         case PUT: {
            if(request.getBody() != null) {
               bodyPublisher = HttpRequest.BodyPublishers.ofByteArray(request.getBody().toByteArray());
            }
            builder.PUT(bodyPublisher);
            break;
         }
         case PATCH: {
            if(request.getBody() != null) {
               bodyPublisher = HttpRequest.BodyPublishers.ofByteArray(request.getBody().toByteArray());
            }
            builder.method("PATCH", bodyPublisher);
            break;
         }
         case OPTIONS: {
            builder.method("OPTIONS", HttpRequest.BodyPublishers.noBody());
            break;
         }
      }

      // Add headers
      for(Header header : request.getHeaders()) {
         for(String value : header.getValueList()) {
            builder.header(header.getName(), value);
         }
      }

      // Set User-Agent if configured and not already in request headers
      if(this.options != null && this.options != ClientOptions.IMPLEMENTATION_DEFAULT
              && this.options.userAgent != null
              && request.getHeaderValue("User-Agent") == null) {
         builder.header("User-Agent", this.options.userAgent);
      }

      return builder.build();
   }

   private Response toResponse(HttpResponse<byte[]> httpResponse) {
      ResponseBuilder builder = new ResponseBuilder();
      builder.setStatusCode(httpResponse.statusCode());

      httpResponse.headers().map().forEach((name, values) -> {
         for(String value : values) {
            builder.addHeader(name, value);
         }
      });

      byte[] body = httpResponse.body();
      if(body != null && body.length > 0) {
         builder.setBody(body);
      }

      return builder.create();
   }

   private static String encodeFormParameters(Collection<Parameter> parameters) {
      StringBuilder sb = new StringBuilder();
      boolean first = true;
      for(Parameter param : parameters) {
         for(String value : param.getValues()) {
            if(!first) {
               sb.append('&');
            }
            sb.append(URLEncoder.encode(param.getName(), StandardCharsets.UTF_8));
            sb.append('=');
            sb.append(URLEncoder.encode(value, StandardCharsets.UTF_8));
            first = false;
         }
      }
      return sb.toString();
   }

   private HttpClient httpClient;
   private ClientOptions options;
   private boolean defaultFollowRedirects;
   private ExecutorService executor;
   private final AtomicBoolean isInit = new AtomicBoolean(false);
}
