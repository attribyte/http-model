/*
 * Copyright 2010 Attribyte, LLC 
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

package org.attribyte.api.http.impl.commons;

import com.google.common.collect.Lists;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.EntityBuilder;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.config.ConnectionConfig;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.attribyte.api.InitializationException;
import org.attribyte.api.Logger;
import org.attribyte.api.http.ClientOptions;
import org.attribyte.api.http.Header;
import org.attribyte.api.http.Parameter;
import org.attribyte.api.http.Request;
import org.attribyte.api.http.RequestOptions;
import org.attribyte.api.http.Response;
import org.attribyte.api.http.ResponseBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * An HTTP client based on Apache commons HTTP.
 */
public class Commons4Client implements org.attribyte.api.http.Client {

   /**
    * Creates an <em>uninitialized</em> client.
    */
   public Commons4Client() {

   }

   private void initFromOptions(final ClientOptions options) {
      HttpClientBuilder builder = HttpClients.custom();
      builder.setMaxConnTotal(options.maxConnectionsTotal);
      builder.setMaxConnPerRoute(options.maxConnectionsPerDestination);
      builder.setUserAgent(options.userAgent);
      if(options.proxyHost != null) {
         builder.setProxy(new HttpHost(options.proxyHost, options.proxyPort));
      }

      this.defaultRequestConfig =
              RequestConfig.custom()
                      .setConnectTimeout(options.connectionTimeoutMillis)
                      .setConnectionRequestTimeout(options.requestTimeoutMillis)
                      .setRedirectsEnabled(RequestOptions.DEFAULT_FOLLOW_REDIRECTS)
                      .setMaxRedirects(RequestOptions.DEFAULT_FOLLOW_REDIRECTS ? 5 : 0)
                      .setAuthenticationEnabled(false)
                      .setCircularRedirectsAllowed(false)
                      .setSocketTimeout(options.socketTimeoutMillis)
                      .build();
      builder.setDefaultRequestConfig(defaultRequestConfig);


      ConnectionConfig connectionConfig =
              ConnectionConfig.custom()
                      .setBufferSize(options.requestBufferSize > options.responseBufferSize ?
                              options.requestBufferSize : options.responseBufferSize)
                      .build();
      builder.setDefaultConnectionConfig(connectionConfig);

      this.httpClient = builder.build();
   }

   /**
    * Creates a client with specified options.
    * @param options The options.
    */
   public Commons4Client(final ClientOptions options) {
      initFromOptions(options);
   }

   @Override
   /**
    * Initializes the client from properties.
    * <p>
    *   The following properties are available. <b>Bold</b> properties are required.
    *
    *   <h2>HTTP Client</h2>
    *   <dl>
    *     <dt><b>User-Agent</b></dt>
    *     <dd>The default User-Agent string sent with requests. Added only if
    *     request has no <code>User-Agent</code> header.</dd>
    *     <dt><b>connectionTimeoutMillis</b></dt>
    *     <dd>The HTTP connection timeout in milliseconds.</dd>
    *     <dt><b>socketTimeoutMillis</b></dt>
    *     <dd>The HTTP client socket timeout in milliseconds.</dd>
    *     <dt>proxyHost</dt>
    *     <dd>The HTTP proxy host. If specified, all client requests will use this proxy.</dd>
    *     <dt>proxyPort</dt>
    *     <dd>The HTTP proxy port. Required when <code>proxyHost</code> is specified</dd>
    *   </dl>
    * </p>
    * @param prefix The prefix for all properties (e.g. 'client.').
    * @param props The properties.
    * @param logger The logger. If unspecified, messages are logged to the console.
    * @throws org.attribyte.api.InitializationException on initialization error.
    */
   public void init(String prefix, Properties props, Logger logger) throws InitializationException {
      if(isInit.compareAndSet(false, true)) {
         ClientOptions options = new ClientOptions(prefix, props);
         initFromOptions(options);
      }
   }

   @Override
   public Response send(Request request) throws IOException {
      return send(request, RequestOptions.DEFAULT);
   }

   @Override
   public Response send(Request request, RequestOptions options) throws IOException {

      HttpUriRequest commonsRequest = null;

      switch(request.getMethod()) {
         case GET:
            commonsRequest = new HttpGet(request.getURI());
            break;
         case DELETE:
            commonsRequest = new HttpDelete(request.getURI());
            break;
         case HEAD:
            commonsRequest = new HttpHead(request.getURI());
            break;
         case POST: {
            HttpEntityEnclosingRequestBase entityEnclosingRequest = new HttpPost(request.getURI());
            commonsRequest = entityEnclosingRequest;
            EntityBuilder entityBuilder = EntityBuilder.create();
            if(request.getBody() != null) {
               entityBuilder.setBinary(request.getBody().toByteArray());
            } else {
               Collection<Parameter> parameters = request.getParameters();
               List<NameValuePair> nameValuePairs = Lists.newArrayListWithExpectedSize(parameters.size());
               for(Parameter parameter : parameters) {
                  String[] values = parameter.getValues();
                  for(String value : values) {
                     nameValuePairs.add(new BasicNameValuePair(parameter.getName(), value));
                  }
               }
            }
            entityEnclosingRequest.setEntity(entityBuilder.build());
            break;
         }
         case PUT: {
            HttpEntityEnclosingRequestBase entityEnclosingRequest = new HttpPut(request.getURI());
            commonsRequest = entityEnclosingRequest;
            EntityBuilder entityBuilder = EntityBuilder.create();
            if(request.getBody() != null) {
               entityBuilder.setBinary(request.getBody().toByteArray());
            }
            entityEnclosingRequest.setEntity(entityBuilder.build());
            break;
         }
      }

      Collection<Header> headers = request.getHeaders();
      for(Header header : headers) {
         String[] values = header.getValues();
         for(String value : values) {
            commonsRequest.setHeader(header.getName(), value);
         }
      }

      ResponseBuilder builder = new ResponseBuilder();
      CloseableHttpResponse response = null;
      InputStream is = null;
      try {
         if(options.followRedirects != RequestOptions.DEFAULT_FOLLOW_REDIRECTS) {
            RequestConfig localConfig =
                    RequestConfig.copy(defaultRequestConfig)
                            .setRedirectsEnabled(options.followRedirects)
                            .setMaxRedirects(options.followRedirects ? 5 : 0).build();
            HttpClientContext localContext = HttpClientContext.create();
            localContext.setRequestConfig(localConfig);
            response = httpClient.execute(commonsRequest, localContext);
         } else {
            response = httpClient.execute(commonsRequest);
         }

         builder.setStatusCode(response.getStatusLine().getStatusCode());
         for(org.apache.http.Header header : response.getAllHeaders()) {
            builder.addHeader(header.getName(), header.getValue());

         }
         HttpEntity entity = response.getEntity();
         if(entity != null) {
            is = entity.getContent();
            if(is != null) {
               builder.setBody(Request.bodyFromInputStream(is, options.maxResponseBytes));
            }
         }

      } finally {
         if(is != null) {
            try {
               is.close();
            } catch(IOException ioe) {
               //TODO?
            }
         }
         if(response != null) {
            response.close();
         }
      }

      return builder.create();
   }

   @Override
   public void shutdown() {
      try {
         httpClient.close();
      } catch(IOException ioe) {
         //TODO?
      }
   }

   private CloseableHttpClient httpClient;
   private RequestConfig defaultRequestConfig;
   private final AtomicBoolean isInit = new AtomicBoolean(false);
}

