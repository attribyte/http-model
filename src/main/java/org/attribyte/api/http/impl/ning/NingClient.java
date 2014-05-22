package org.attribyte.api.http.impl.ning;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import com.ning.http.client.AsyncCompletionHandler;
import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.AsyncHttpClientConfig;
import com.ning.http.client.ProxyServer;
import com.ning.http.client.Request;
import com.ning.http.client.RequestBuilder;
import com.ning.http.client.Response;
import org.attribyte.api.InitializationException;
import org.attribyte.api.Logger;
import org.attribyte.api.http.AsyncClient;
import org.attribyte.api.http.ClientOptions;
import org.attribyte.api.http.Header;
import org.attribyte.api.http.Parameter;
import org.attribyte.api.http.RequestOptions;
import org.attribyte.api.http.ResponseBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

public class NingClient implements AsyncClient {

   //For default values, see:
   //https://github.com/AsyncHttpClient/async-http-client/blob/master/api/src/main/java/org/asynchttpclient/AsyncHttpClientConfig.java

   private void initFromOptions(ClientOptions options) {
      AsyncHttpClientConfig.Builder config = new AsyncHttpClientConfig.Builder();
      config.setUserAgent(options.userAgent);
      if(options.proxyHost != null) {
         config.setProxyServer(new ProxyServer(options.proxyHost, options.proxyPort));
      }
      config.setConnectionTimeoutInMs(options.connectionTimeoutMillis);
      config.setRequestTimeoutInMs(options.requestTimeoutMillis);
      config.setFollowRedirects(options.followRedirects);
      config.setMaximumConnectionsPerHost(options.maxConnectionsPerDestination);
      config.setMaximumConnectionsTotal(options.maxConnectionsTotal);
      config.setFollowRedirects(RequestOptions.DEFAULT_FOLLOW_REDIRECTS);
      config.setAllowPoolingConnection(options.getBooleanProperty("allowPoolingConnection", true));
      config.setIOThreadMultiplier(options.getIntProperty("ioThreadMultiplier", 2));
      config.setIdleConnectionInPoolTimeoutInMs(options.getTimeProperty("idleConnectionInPoolTimeout", 60 * 1000));
      config.setIdleConnectionTimeoutInMs(options.getTimeProperty("idleConnectionTimeout", 60 * 1000));
      config.setMaxConnectionLifeTimeInMs(options.getTimeProperty("maxConnectionLife", -1));
      config.setCompressionEnabled(options.getBooleanProperty("compressionEnabled", false));
      config.setRequestCompressionLevel(options.getIntProperty("requestCompressionLevel", 1));
      this.httpClient = new AsyncHttpClient(config.build());
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
   public ListenableFuture<org.attribyte.api.http.Response> asyncSend(final org.attribyte.api.http.Request request,
                                                                      final RequestOptions options) {

      final SettableFuture<org.attribyte.api.http.Response> fut = SettableFuture.create();

      try {
         httpClient.executeRequest(toNingRequest(request, options), new AsyncCompletionHandler<Response>() {
            @Override
            public Response onCompleted(Response response) throws Exception {
               ResponseBuilder builder = new ResponseBuilder();
               builder.setStatusCode(response.getStatusCode());
               Set<Map.Entry<String, List<String>>> entries = response.getHeaders().entrySet();
               for(Map.Entry<String, List<String>> header : entries) {
                  builder.addHeader(header.getKey(), header.getValue().get(0));
               }
               InputStream is = response.getResponseBodyAsStream();
               if(is != null) {
                  try {
                     builder.setBody(org.attribyte.api.http.Request.bodyFromInputStream(is, options.maxResponseBytes));
                  } finally {
                     try {
                        is.close();
                     } catch(IOException ioe) {
                        //TODO
                     }
                  }
               }
               fut.set(builder.create());
               return response;
            }

            @Override
            public void onThrowable(Throwable t) {
               fut.setException(t);
            }
         });
      } catch(Throwable t) {
         fut.setException(t);
      }

      return fut;
   }

   @Override
   public void shutdown() throws Exception {
      httpClient.close();
   }


   private Request toNingRequest(final org.attribyte.api.http.Request request, final RequestOptions options) {

      RequestBuilder ningRequestBuilder = new RequestBuilder();
      ningRequestBuilder.setURI(request.getURI());
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
                  ningRequestBuilder.addParameter(parameter.getName(), parameter.getValue());
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
         ningRequestBuilder.addHeader(header.getName(), header.getValue());
      }

      return ningRequestBuilder.build();
   }

   private AsyncHttpClient httpClient;
   private final AtomicBoolean isInit = new AtomicBoolean(false);
}
