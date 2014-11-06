/*
 * Copyright 2014 Attribyte, LLC
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
import org.attribyte.api.http.Header;
import org.attribyte.api.http.Parameter;
import org.attribyte.api.http.RequestOptions;
import org.attribyte.api.http.ResponseBuilder;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ProxyConfiguration;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.api.Response;
import org.eclipse.jetty.client.api.Result;
import org.eclipse.jetty.client.util.BufferingResponseListener;
import org.eclipse.jetty.client.util.ByteBufferContentProvider;
import org.eclipse.jetty.http.HttpField;
import org.eclipse.jetty.http.HttpFields;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.util.HttpCookieStore;
import org.eclipse.jetty.util.ssl.SslContextFactory;

import java.io.IOException;
import java.util.Collection;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

public class JettyClient implements AsyncClient {

   /**
    * Creates an <em>uninitialized</em> client.
    */
   public JettyClient() {

   }

   /**
    * Creates a client with specified options.
    * @param options The options.
    */
   public JettyClient(final ClientOptions options) throws InitializationException {
      initFromOptions(options);
   }


   private void initFromOptions(final ClientOptions options) throws InitializationException {

      if(options != ClientOptions.IMPLEMENTATION_DEFAULT) {
         SslContextFactory sslContextFactory = new SslContextFactory();
         this.httpClient = new HttpClient(sslContextFactory);
         this.httpClient.setFollowRedirects(options.followRedirects);
         this.httpClient.setConnectTimeout(options.connectionTimeoutMillis);
         this.httpClient.setMaxConnectionsPerDestination(options.maxConnectionsPerDestination);
         this.httpClient.setCookieStore(new HttpCookieStore.Empty());
         if(options.proxyHost != null) {
            this.httpClient.setProxyConfiguration(new ProxyConfiguration(options.proxyHost, options.proxyPort));
         }
         this.httpClient.setUserAgentField(new HttpField(HttpHeader.USER_AGENT, options.userAgent));
         this.httpClient.setRequestBufferSize(options.requestBufferSize);
         this.httpClient.setResponseBufferSize(options.responseBufferSize);
         this.httpClient.setIdleTimeout(options.getIntProperty("idleTimeout", 0));
         this.httpClient.setAddressResolutionTimeout(options.getIntProperty("addressResolutionTimeout", 15000));
         this.httpClient.setMaxRedirects(options.getIntProperty("maxRedirects", 8));
         this.httpClient.setMaxRequestsQueuedPerDestination(options.getIntProperty("maxRequestsQueuedPerDestination", 1024));
         this.httpClient.setDispatchIO(options.getBooleanProperty("dispatchIO", true));
      } else {
         SslContextFactory sslContextFactory = new SslContextFactory();
         this.httpClient = new HttpClient(sslContextFactory);
      }

      try {
         this.httpClient.start();
      } catch(Exception e) {
         throw new InitializationException("Problem starting client", e);
      }
   }

   public synchronized void init(String prefix, Properties props, Logger logger) throws InitializationException {

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
      } catch(TimeoutException te) {
         throw new IOException(te);
      } catch(ExecutionException ee) {
         throw new IOException(ee);
      } catch(InterruptedException ie) {
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
      toJettyRequest(request)
              .followRedirects(options.followRedirects)
              .send(new BufferingResponseListener(options.maxResponseBytes) {
                       @Override
                       public void onComplete(Result result) {
                          if(!result.isFailed()) {
                             ResponseBuilder builder = new ResponseBuilder();
                             Response response = result.getResponse();
                             builder.setStatusCode(response.getStatus());
                             HttpFields headers = response.getHeaders();
                             for(HttpField header : headers) {
                                builder.addHeader(header.getName(), header.getValue());
                             }
                             byte[] responseContent = getContent();
                             if(responseContent != null) {
                                builder.setBody(responseContent);
                             }
                             fut.set(builder.create());
                          } else {
                             fut.setException(result.getFailure());
                          }
                       }
                    }
              );

      return fut;
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

      Collection<Header> headers = request.getHeaders();
      for(Header header : headers) {
         jettyRequest.header(header.getName(), header.getValue());
      }

      return jettyRequest;
   }

   private HttpClient httpClient;
   private final AtomicBoolean isInit = new AtomicBoolean(false);
}
