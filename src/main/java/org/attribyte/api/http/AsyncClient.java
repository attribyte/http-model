/*
 * Copyright 2010, 2014 Attribyte, LLC
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

package org.attribyte.api.http;

import com.google.common.util.concurrent.ListenableFuture;

import java.util.concurrent.CompletableFuture;

/**
 * Sends requests asynchronously with a "future" result.
 */
public interface AsyncClient extends Client {

   /**
    * Sends a request with default options that completes with a {@code ListenableFuture}.
    * @param request The request.
    * @return The (listenable) response future.
    */
   public ListenableFuture<Response> asyncSend(Request request);

   /**
    * Sends a request with specified options that completes with a {@code ListenableFuture}.
    * @param request The request.
    * @param options The request options.
    * @return The (listenable) response future.
    */
   public ListenableFuture<Response> asyncSend(Request request, RequestOptions options);


   /**
    * Sends a request with default options that completes with a {@code CompletableFuture}.
    * @param request The request.
    * @return The (completable) response future.
    */
   public CompletableFuture<Response> completableSend(org.attribyte.api.http.Request request);

   /**
    * Sends a request with specified options that completes with a {@code CompletableFuture}.
    * @param request The request.
    * @param options The request options.
    * @return The (completable) response future.
    */
   public CompletableFuture<Response> completableSend(org.attribyte.api.http.Request request, RequestOptions options);
}
