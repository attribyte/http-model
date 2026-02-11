/*
 * Copyright 2019 Attribyte, LLC
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

import com.google.common.hash.HashFunction;
import com.google.common.hash.Hasher;
import com.google.common.hash.Hashing;
import org.attribyte.api.http.ResponseBuilder;
import org.eclipse.jetty.client.Response;
import org.eclipse.jetty.client.Result;

import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;

class TestResponseListener extends BaseResponseListener {

   public TestResponseListener(final CompletableFuture<org.attribyte.api.http.Response> fut) {
       super(Integer.MAX_VALUE, false);
       this.fut = fut;
       this.hasher = hashFunction.newHasher();
   }

   @Override
   public void onContent(Response response, ByteBuffer content) {
      super.onContent(response, content);
      this.length += content.remaining();
      hasher.putBytes(content);
   }


   protected void completed(final org.attribyte.api.http.Response response) {
      fut.complete(response);
   }

   protected void failed(final Throwable failure) {
      fut.completeExceptionally(failure);
   }

   @Override
   public void onComplete(Result result) {
      if(!result.isFailed()) {
         ResponseBuilder builder = fromResult(result, false);
         builder.addAttribute("responseLength", length);
         builder.addAttribute("responseHash", hasher.hash());
         completed(builder.create());
      } else if(truncateOnLimit && result.getFailure() instanceof CapacityReached) {
         ResponseBuilder builder = fromResult(result, true);
         builder.addAttribute("responseLength", length);
         builder.addAttribute("responseHash", hasher.hash());
         completed(builder.create());
      } else {
         failed(result.getFailure());
      }
   }

   private ResponseBuilder fromResult(final Result result,
                                      final boolean truncated) {

      ResponseBuilder builder = new ResponseBuilder();
      Response response = result.getResponse();
      builder.setStatusCode(response.getStatus());
      response.getHeaders().forEach(header -> {
         builder.addHeader(header.getName(), header.getValue());
      });
      builder.setStats(stats());
      if(truncated) {
         builder.addAttribute("truncated", Boolean.TRUE);
      }
      return builder;
   }

   /**
    * The future result.
    */
   private final CompletableFuture<org.attribyte.api.http.Response> fut;

   /**
    * The hash function.
    */
   private static final HashFunction hashFunction = Hashing.sha256();


   private int length = 0;

   /**
    * The current hasher.
    */
   private final Hasher hasher;

}
