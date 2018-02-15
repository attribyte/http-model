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

package org.attribyte.api.http.impl.ning;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.AsyncHttpClientConfig;
import com.ning.http.client.AsyncHttpClientConfigDefaults;
import com.ning.http.client.ProxyServer;
import com.ning.http.client.Request;
import com.ning.http.client.RequestBuilder;
import org.attribyte.api.InitializationException;
import org.attribyte.api.Logger;
import org.attribyte.api.http.AsyncClient;
import org.attribyte.api.http.ClientOptions;
import org.attribyte.api.http.Header;
import org.attribyte.api.http.Parameter;
import org.attribyte.api.http.RequestOptions;

import java.io.IOException;
import java.net.URI;
import java.util.Collection;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

public class NingClient implements AsyncClient {

   private void initFromOptions(ClientOptions options) {

      if(options != ClientOptions.IMPLEMENTATION_DEFAULT) {
         AsyncHttpClientConfig.Builder config = new AsyncHttpClientConfig.Builder();
         config.setUserAgent(options.userAgent);
         if(options.proxyHost != null) {
            config.setProxyServer(new ProxyServer(options.proxyHost, options.proxyPort));
         }
         config.setConnectTimeout(options.connectionTimeoutMillis);
         config.setRequestTimeout(options.requestTimeoutMillis);
         config.setFollowRedirect(options.followRedirects);
         config.setMaxConnectionsPerHost(options.maxConnectionsPerDestination);
         config.setMaxConnections(options.maxConnectionsTotal);
         config.setAllowPoolingConnections(options.getBooleanProperty("allowPoolingConnections",
                 AsyncHttpClientConfigDefaults.defaultAllowPoolingConnections()));
         config.setIOThreadMultiplier(options.getIntProperty("ioThreadMultiplier",
                 AsyncHttpClientConfigDefaults.defaultIoThreadMultiplier()));
         config.setCompressionEnforced(options.getBooleanProperty("compressionEnforced",
                 AsyncHttpClientConfigDefaults.defaultCompressionEnforced()));
         config.setPooledConnectionIdleTimeout(options.getTimeProperty("pooledConnectionIdleTimeout",
                 AsyncHttpClientConfigDefaults.defaultPooledConnectionIdleTimeout()));
         config.setConnectionTTL(options.getTimeProperty("maxConnectionLife", AsyncHttpClientConfigDefaults.defaultConnectionTTL()));
         config.setAcceptAnyCertificate(options.trustAllCertificates);
         //TODO: A few more configuration parameters...
         this.httpClient = new AsyncHttpClient(config.build());
      } else {
         this.httpClient = new AsyncHttpClient(new AsyncHttpClientConfig.Builder().build());
      }
   }

   /**
    * Creates an <em>uninitialized</em> client.
    */
   public NingClient() {
   }

   /**
    * Creates a client with specified options.
    * @param options The options.
    */
   public NingClient(final ClientOptions options) {
      initFromOptions(options);
   }

   /**
    * Creates a client with a pre-configured ning client.
    * @param asyncHttpClient The ning client.
    */
   public NingClient(final AsyncHttpClient asyncHttpClient) {
      this.httpClient = asyncHttpClient;
   }

   /**
    * Initialize from properties.
    * @param prefix A prefix applied to all property names.
    * @param props The properties.
    * @param logger A logger.
    * @throws InitializationException on initialization error.
    */
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
   public ListenableFuture<org.attribyte.api.http.Response> asyncSend(final org.attribyte.api.http.Request request,
                                                                      final RequestOptions options) {
      final SettableFuture<org.attribyte.api.http.Response> fut = SettableFuture.create();
      httpClient.executeRequest(toNingRequest(request, options), new ListenableFutureCompletionHandler(fut, options.maxResponseBytes));
      return fut;
   }

   @Override
   public CompletableFuture<org.attribyte.api.http.Response> completableSend(final org.attribyte.api.http.Request request) {
      return completableSend(request, RequestOptions.DEFAULT);
   }

   @Override
   public CompletableFuture<org.attribyte.api.http.Response> completableSend(final org.attribyte.api.http.Request request,
                                                                             final RequestOptions options) {
      final CompletableFuture<org.attribyte.api.http.Response> fut = new CompletableFuture<>();
      httpClient.executeRequest(toNingRequest(request, options), new CompletableFutureCompletionHandler(fut, options.maxResponseBytes));
      return fut;
   }

   @Override
   public void shutdown() {
      httpClient.close();
   }


   private Request toNingRequest(final org.attribyte.api.http.Request request, final RequestOptions options) {

      final RequestBuilder ningRequestBuilder = new RequestBuilder();
      final URI requestURI = request.getURI();
      ningRequestBuilder.setUri(new com.ning.http.client.uri.Uri(requestURI.getScheme(),
              requestURI.getUserInfo(), requestURI.getHost(), requestURI.getPort(),
              requestURI.getPath(), requestURI.getQuery()));
      ningRequestBuilder.setFollowRedirects(options.followRedirects);

      switch(request.getMethod()) {
         case GET:
            ningRequestBuilder.setMethod("GET");
            break;
         case POST:
            ningRequestBuilder.setMethod("POST");
            Collection<Parameter> parameters = request.getParameters();
            if(parameters.size() > 0) {
               for(Parameter parameter : parameters) {
                  ningRequestBuilder.addFormParam(parameter.getName(), parameter.getValue());
               }
            } else if(request.getBody() != null) {
               ningRequestBuilder.setBody(request.getBody().toByteArray());
            }
            break;
         case PUT:
            ningRequestBuilder.setMethod("PUT");
            if(request.getBody() != null) {
               ningRequestBuilder.setBody(request.getBody().toByteArray());
            }
            break;
         case DELETE:
            ningRequestBuilder.setMethod("DELETE");
            break;
         case HEAD:
            ningRequestBuilder.setMethod("HEAD");
            break;
      }

      Collection<Header> headers = request.getHeaders();
      for(Header header : headers) {
         header.getValueList().forEach(value -> ningRequestBuilder.addHeader(header.getName(), value));
      }

      return ningRequestBuilder.build();
   }

   private AsyncHttpClient httpClient;
   private final AtomicBoolean isInit = new AtomicBoolean(false);
}