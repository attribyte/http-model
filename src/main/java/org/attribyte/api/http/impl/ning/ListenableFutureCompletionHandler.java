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

import com.google.common.util.concurrent.SettableFuture;
import org.attribyte.api.http.Response;

class ListenableFutureCompletionHandler extends CompletionHandler {

   ListenableFutureCompletionHandler(final SettableFuture<Response> fut, final int maxResponseBytes) {
      super(maxResponseBytes);
      this.fut = fut;
   }

   @Override
   protected void completed(final Response response) {
      fut.set(response);
   }

   @Override
   protected void failed(final Throwable failure) {
      fut.setException(failure);
   }

   /**
    * The future result.
    */
   private final SettableFuture<org.attribyte.api.http.Response> fut;
}