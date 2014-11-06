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

import com.google.common.base.Strings;
import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.ByteArrayRequestEntity;
import org.apache.commons.httpclient.methods.DeleteMethod;
import org.apache.commons.httpclient.methods.EntityEnclosingMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.HeadMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.PutMethod;
import org.apache.commons.httpclient.params.HttpConnectionManagerParams;
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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;


/**
 * An HTTP client based on Apache commons HTTP.
 */
public class Commons3Client implements org.attribyte.api.http.Client {

   /**
    * Creates an <em>uninitialized</em> client.
    */
   public Commons3Client() {

   }

   /**
    * Creates a client with specified options.
    * @param options The options.
    */
   public Commons3Client(final ClientOptions options) {
      connectionManager = new MultiThreadedHttpConnectionManager();
      connectionManager.setParams(fromOptions(options));
      httpClient = new HttpClient(connectionManager);
      HostConfiguration hostConfig = new HostConfiguration();

      if(options.proxyHost != null) {
         hostConfig.setProxy(options.proxyHost, options.proxyPort);
      }
      httpClient.setHostConfiguration(hostConfig);
      userAgent = options.userAgent;
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
    * @throws InitializationException on initialization error.
    */
   public synchronized void init(String prefix, Properties props, Logger logger) throws InitializationException {

      if(isInit.compareAndSet(false, true)) {

         ClientOptions options = new ClientOptions(prefix, props);
         connectionManager = new MultiThreadedHttpConnectionManager();
         connectionManager.setParams(fromOptions(options));

         httpClient = new HttpClient(connectionManager);
         HostConfiguration hostConfig = new HostConfiguration();

         if(options.proxyHost != null) {
            hostConfig.setProxy(options.proxyHost, options.proxyPort);
         }
         httpClient.setHostConfiguration(hostConfig);

         userAgent = options.userAgent;
      }
   }

   private HttpConnectionManagerParams fromOptions(ClientOptions options) {
      HttpConnectionManagerParams connectionParams = new HttpConnectionManagerParams();
      if(options != ClientOptions.IMPLEMENTATION_DEFAULT) {
         connectionParams.setConnectionTimeout(options.connectionTimeoutMillis);
         connectionParams.setSoTimeout(options.socketTimeoutMillis);
         connectionParams.setDefaultMaxConnectionsPerHost(options.maxConnectionsPerDestination);
         connectionParams.setMaxTotalConnections(options.maxConnectionsTotal);
         connectionParams.setSendBufferSize(options.requestBufferSize);
         connectionParams.setReceiveBufferSize(options.responseBufferSize);
         //connectionParams.setLinger();
         //connectionParams.setStaleCheckingEnabled();
         //connectionParams.setTcpNoDelay();
      }
      return connectionParams;
   }


   @Override
   public Response send(Request request) throws IOException {
      return send(request, RequestOptions.DEFAULT);
   }

   @Override
   public Response send(Request request, RequestOptions options) throws IOException {

      HttpMethod method = null;

      switch(request.getMethod()) {
         case GET:
            method = new GetMethod(request.getURI().toString());
            method.setFollowRedirects(options.followRedirects);
            break;
         case DELETE:
            method = new DeleteMethod(request.getURI().toString());
            break;
         case HEAD:
            method = new HeadMethod(request.getURI().toString());
            method.setFollowRedirects(options.followRedirects);
            break;
         case POST:
            method = new PostMethod(request.getURI().toString());
            if(request.getBody() != null) {
               ByteArrayRequestEntity requestEntity = new ByteArrayRequestEntity(request.getBody().toByteArray());
               ((EntityEnclosingMethod)method).setRequestEntity(requestEntity);
            } else {
               PostMethod postMethod = (PostMethod)method;
               Collection<Parameter> parameters = request.getParameters();
               for(Parameter parameter : parameters) {
                  String[] values = parameter.getValues();
                  for(String value : values) {
                     postMethod.addParameter(parameter.getName(), value);
                  }
               }
            }
            break;
         case PUT:
            method = new PutMethod(request.getURI().toString());
            if(request.getBody() != null) {
               ByteArrayRequestEntity requestEntity = new ByteArrayRequestEntity(request.getBody().toByteArray());
               ((EntityEnclosingMethod)method).setRequestEntity(requestEntity);
            }
            break;
      }

      if(userAgent != null && Strings.isNullOrEmpty(request.getHeaderValue("User-Agent"))) {
         method.setRequestHeader("User-Agent", userAgent);
      }

      Collection<Header> headers = request.getHeaders();
      for(Header header : headers) {
         String[] values = header.getValues();
         for(String value : values) {
            method.setRequestHeader(header.getName(), value);
         }
      }

      int responseCode;
      InputStream is = null;

      try {
         responseCode = httpClient.executeMethod(method);
         is = method.getResponseBodyAsStream();
         if(is != null) {
            byte[] body = Request.bodyFromInputStream(is, options.maxResponseBytes);
            ResponseBuilder builder = new ResponseBuilder();
            builder.setStatusCode(responseCode);
            builder.addHeaders(getMap(method.getResponseHeaders()));
            return builder.setBody(body.length != 0 ? body : null).create();
         } else {
            ResponseBuilder builder = new ResponseBuilder();
            builder.setStatusCode(responseCode);
            builder.addHeaders(getMap(method.getResponseHeaders()));
            return builder.create();
         }

      } finally {
         if(is != null) {
            try {
               is.close();
            } catch(IOException ioe) {
               //Ignore
            }
         }
         method.releaseConnection();
      }
   }

   @SuppressWarnings("unchecked")
   private Map<String, Object> getMap(NameValuePair[] pairs) {

      if(pairs == null || pairs.length == 0) {
         return Collections.emptyMap();
      }

      Map<String, Object> parameterMap = new HashMap<String, Object>();
      for(NameValuePair pair : pairs) {
         Object o = parameterMap.get(pair.getName());
         if(o == null) {
            parameterMap.put(pair.getName(), pair.getValue());
         } else {
            if(o instanceof List) {
               ((List<String>)o).add(pair.getValue());
            } else {
               List<String> vlist = new ArrayList<String>(2);
               vlist.add(pair.getValue());
               parameterMap.put(pair.getName(), vlist);
            }
         }
      }

      return parameterMap;
   }

   @Override
   public void shutdown() {
      connectionManager.shutdown();
   }


   private HttpClient httpClient;
   private MultiThreadedHttpConnectionManager connectionManager;
   private String userAgent;
   private final AtomicBoolean isInit = new AtomicBoolean(false);
}

