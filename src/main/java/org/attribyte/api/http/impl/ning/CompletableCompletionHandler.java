/*
 * Copyright 2018 Attribyte, LLC
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

import org.attribyte.api.http.Response;

import java.util.concurrent.CompletableFuture;

class CompletableCompletionHandler extends CompletionHandler {

   CompletableCompletionHandler(final CompletableFuture<org.attribyte.api.http.Response> fut, final int maxResponseBytes) {
      super(maxResponseBytes);
      this.fut = new CompletableFuture<>();
   }

   @Override
   protected void completed(final org.attribyte.api.http.Response response) {
      fut.complete(response);
   }

   @Override
   protected void failed(final Throwable failure) {
      fut.completeExceptionally(failure);
   }

   /**
    * The future result.
    */
   private final CompletableFuture<Response> fut;
}
