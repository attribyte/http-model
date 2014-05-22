package org.attribyte.api.http;

import com.google.common.util.concurrent.ListenableFuture;

/**
 * Defines the async HTTP client interface.
 */
public interface AsyncClient extends Client {

   /**
    * Sends a request with default options.
    * @param request The request.
    * @return The (listenable) response future.
    */
   public ListenableFuture<Response> asyncSend(Request request);

   /**
    * Sends a request with specified options.
    * @param request The request.
    * @param options The request options.
    * @return The (listenable) response future.
    */
   public ListenableFuture<Response> asyncSend(Request request, RequestOptions options);
}
