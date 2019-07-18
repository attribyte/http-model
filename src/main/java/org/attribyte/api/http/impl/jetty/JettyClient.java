/*
 * Copyright 2014-2018 Attribyte, LLC
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

package org.attribyte.api.http.impl.jetty;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import org.attribyte.api.InitializationException;
import org.attribyte.api.Logger;
import org.attribyte.api.http.AsyncClient;
import org.attribyte.api.http.ClientOptions;
import org.attribyte.api.http.Parameter;
import org.attribyte.api.http.RequestOptions;
import org.attribyte.api.http.Response;
import org.attribyte.api.http.ResponseBuilder;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.ProxyConfiguration;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.ByteBufferContentProvider;
import org.eclipse.jetty.client.util.InputStreamResponseListener;
import org.eclipse.jetty.http.HttpField;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.util.HttpCookieStore;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.client.HttpProxy;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpCookie;
import java.util.Collection;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public class JettyClient implements AsyncClient {

   /**
    * Creates an <em>uninitialized</em> client.
    */
   public JettyClient() {
   }

   /**
    * Creates a client with specified options.
    * @param options The options.
    * @throws InitializationException if options are invalid or other initialization error.
    */
   public JettyClient(final ClientOptions options) throws InitializationException {
      initFromOptions(options);
   }

   private void initFromOptions(final ClientOptions options) throws InitializationException {
      this.httpClient = jettyClientFromOptions(options);
      try {
         this.httpClient.start();
      } catch(Exception e) {
         throw new InitializationException("Problem starting client", e);
      }
   }

   /**
    * Creates and initializes a new Jetty client from HTTP client options.
    * @param options The options.
    * @return The initialized (but not started) client.
    */
   public static HttpClient jettyClientFromOptions(final ClientOptions options) {

      if(options != ClientOptions.IMPLEMENTATION_DEFAULT) {
         SslContextFactory sslContextFactory = new SslContextFactory(options.trustAllCertificates);
         sslContextFactory.setExcludeCipherSuites("^.*_(MD5)$");
         HttpClient httpClient = new HttpClient(sslContextFactory);
         httpClient.setFollowRedirects(options.followRedirects);
         httpClient.setConnectTimeout(options.connectionTimeoutMillis);
         httpClient.setMaxConnectionsPerDestination(options.maxConnectionsPerDestination);
         httpClient.setCookieStore(new HttpCookieStore.Empty());
         if(options.proxyHost != null) {
            ProxyConfiguration proxyConfig = httpClient.getProxyConfiguration();
            proxyConfig.getProxies().add(new HttpProxy(options.proxyHost, options.proxyPort));
         }
         httpClient.setUserAgentField(new HttpField(HttpHeader.USER_AGENT, options.userAgent));
         httpClient.setRequestBufferSize(options.requestBufferSize);
         httpClient.setResponseBufferSize(options.responseBufferSize);
         httpClient.setIdleTimeout(options.getIntProperty("idleTimeout", 0));
         httpClient.setAddressResolutionTimeout(options.getIntProperty("addressResolutionTimeout", 15000));
         httpClient.setMaxRedirects(options.getIntProperty("maxRedirects", 8));
         httpClient.setMaxRequestsQueuedPerDestination(options.getIntProperty("maxRequestsQueuedPerDestination", 1024));
         return httpClient;
      } else {
         SslContextFactory sslContextFactory = new SslContextFactory();
         sslContextFactory.setExcludeCipherSuites("^.*_(MD5)$");
         return new HttpClient(sslContextFactory);
      }
   }

   public void init(String prefix, Properties props, Logger logger) throws InitializationException {
      if(isInit.compareAndSet(false, true)) {
         initFromOptions(new ClientOptions(prefix, props));
      }
   }

   @Override
   public org.attribyte.api.http.Response send(org.attribyte.api.http.Request request) throws IOException {
      return send(request, RequestOptions.DEFAULT);
   }

   @Override
   public org.attribyte.api.http.Response send(org.attribyte.api.http.Request request, RequestOptions options) throws IOException {
      try {
         return asyncSend(request, options).get(options.timeoutSeconds, TimeUnit.SECONDS);
      } catch(TimeoutException | ExecutionException e) {
         throw new IOException(e);
      } catch(InterruptedException ie) {
         Thread.currentThread().interrupt();
         throw new IOException(ie);
      }
   }

   @Override
   public ListenableFuture<org.attribyte.api.http.Response> asyncSend(org.attribyte.api.http.Request request) {
      return asyncSend(request, RequestOptions.DEFAULT);
   }

   @Override
   public ListenableFuture<org.attribyte.api.http.Response> asyncSend(org.attribyte.api.http.Request request, RequestOptions options) {
      final SettableFuture<org.attribyte.api.http.Response> fut = SettableFuture.create();
      final ListenableFutureTimingListener listener =
              new ListenableFutureTimingListener(fut, options.maxResponseBytes, options.truncateOnLimit);
      toJettyRequest(request)
              .followRedirects(options.followRedirects)
              .listener(listener)
              .send(listener);
      return fut;
   }

   @Override
   public CompletableFuture<Response> completableSend(org.attribyte.api.http.Request request) {
      return completableSend(request, RequestOptions.DEFAULT);
   }

   @Override
   public CompletableFuture<Response> completableSend(org.attribyte.api.http.Request request, RequestOptions options) {
      final CompletableFuture<org.attribyte.api.http.Response> fut = new CompletableFuture<>();
      final CompletableFutureTimingListener listener =
              new CompletableFutureTimingListener(fut, options.maxResponseBytes, options.truncateOnLimit);
      toJettyRequest(request)
              .followRedirects(options.followRedirects)
              .listener(listener)
              .send(listener);
      return fut;
   }

   /**
    * Send a request and optionally stream the content.
    * @param request The request.
    * @param responseBuilder A builder to receive status and headers. May be {@code null}.
    * @param writeOnStatus Writes the HTTP response to the output on any of these status. If {@code null} writes on any status.
    * @param consumer A consumer for the input stream. Input stream must be closed after response is consumed.
    * @param timeout The time to wait for the response.
    * @param timeoutUnits The timeout units.
    * @throws TimeoutException on timeout.
    * @throws InterruptedException on interrupted.
    * @throws ExecutionException on send exception.
    * @return The status code.
    */
   public int send(final org.attribyte.api.http.Request request,
                   final ResponseBuilder responseBuilder,
                   final Set<Integer> writeOnStatus,
                   final Consumer<InputStream> consumer,
                   final long timeout,
                   final TimeUnit timeoutUnits)
           throws TimeoutException, InterruptedException, ExecutionException {

      InputStreamResponseListener listener = new InputStreamResponseListener();
      toJettyRequest(request).send(listener);
      org.eclipse.jetty.client.api.Response response =
              listener.get(timeout, timeoutUnits);

      if(responseBuilder != null) {
         responseBuilder.setStatusCode(response.getStatus());
         response.getHeaders().forEach(header -> {
            responseBuilder.addHeader(header.getName(), header.getValue());
         });
      }

      if(writeOnStatus == null || writeOnStatus.contains(response.getStatus())) {
         consumer.accept(listener.getInputStream());
      }

      return response.getStatus();
   }

   @Override
   public void shutdown() throws Exception {
      httpClient.stop();
   }


   private Request toJettyRequest(org.attribyte.api.http.Request request) {

      final Request jettyRequest = httpClient.newRequest(request.getURI());
      switch(request.getMethod()) {
         case GET:
            jettyRequest.method(HttpMethod.GET);
            break;
         case POST:
            jettyRequest.method(HttpMethod.POST);
            Collection<Parameter> parameters = request.getParameters();
            if(parameters.size() > 0) {
               for(Parameter parameter : parameters) {
                  jettyRequest.param(parameter.getName(), parameter.getValue());
               }
            } else if(request.getBody() != null) {
               jettyRequest.content(new ByteBufferContentProvider(request.getBody().asReadOnlyByteBuffer()));
            }
            break;
         case PUT:
            jettyRequest.method(HttpMethod.PUT);
            if(request.getBody() != null) {
               jettyRequest.content(new ByteBufferContentProvider(request.getBody().asReadOnlyByteBuffer()));
            }
            break;
         case DELETE:
            jettyRequest.method(HttpMethod.DELETE);
            break;
         case HEAD:
            jettyRequest.method(HttpMethod.HEAD);
            break;
      }

      request.getHeaders().forEach(header -> {
         header.getValueList().forEach(value -> jettyRequest.header(header.getName(), value));
      });

      request.cookies.forEach(cookie -> {
         jettyRequest.cookie(new HttpCookie(cookie.name, cookie.value));
      });

      return jettyRequest;
   }

   private HttpClient httpClient;
   private final AtomicBoolean isInit = new AtomicBoolean(false);
}
