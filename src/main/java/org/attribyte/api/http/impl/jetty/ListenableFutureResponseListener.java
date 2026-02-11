/*
 * Copyright 2026 Attribyte Labs, LLC
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

import com.google.common.util.concurrent.SettableFuture;

class ListenableFutureResponseListener extends BufferingResponseListener {

   ListenableFutureResponseListener(final SettableFuture<org.attribyte.api.http.Response> fut,
                                    final int maxResponseBytes,
                                    final boolean truncateOnLimit) {
      super(maxResponseBytes, truncateOnLimit);
      this.fut = fut;
   }

   @Override
   protected void completed(final org.attribyte.api.http.Response response) {
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
