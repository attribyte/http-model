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

import java.nio.ByteBuffer;

import org.attribyte.api.http.ResponseBuilder;
import org.eclipse.jetty.client.Response;
import org.eclipse.jetty.client.Result;
import org.eclipse.jetty.util.BufferUtil;

abstract class BufferingResponseListener extends BaseResponseListener {

   /**
    * Creates an instance with the given maximum length
    * @param maxLength the maximum length of the content
    * @param truncateOnLimit If we reach the maximum length, should the content simply be truncated?
    */
   public BufferingResponseListener(final int maxLength,
                                    final boolean truncateOnLimit) {
      super(maxLength, truncateOnLimit);
   }

   @Override
   public void onContent(Response response, ByteBuffer content) {
      super.onContent(response, content);

      int length = content.remaining();
      if(length > BufferUtil.space(buffer)) {
         int remaining = buffer == null ? 0 : buffer.remaining();
         if(remaining + length > maxLength) {
            response.abort(new CapacityReached("Buffering capacity " + maxLength + " exceeded"));
         }
         int requiredCapacity = buffer == null ? length : buffer.capacity() + length;
         int newCapacity = Math.min(Integer.highestOneBit(requiredCapacity) << 1, maxLength);
         buffer = BufferUtil.ensureCapacity(buffer, newCapacity);
      }
      BufferUtil.append(buffer, content);
   }

   /**
    * Called when the request is completed with success.
    * @param response The complete response.
    */
   abstract protected void completed(final org.attribyte.api.http.Response response);

   /**
    * Called when the request fails with an exception.
    * @param failure The failure.
    */
   abstract protected void failed(final Throwable failure);

   @Override
   public void onComplete(Result result) {
      if(!result.isFailed()) {
         ResponseBuilder builder = fromResult(result, false);
         completed(builder.create());
      } else if(truncateOnLimit && result.getFailure() instanceof CapacityReached) {
         ResponseBuilder builder = fromResult(result, true);
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
         builder.addHeader(header.getName(), header.getValue()); //Note that getValues returns quoted csv so don't want that.
      });
      byte[] responseContent = getContent();
      if(responseContent != null) {
         builder.setBody(responseContent);
      }
      builder.setStats(stats());

      if(truncated) {
         builder.addAttribute("truncated", Boolean.TRUE);
      }

      return builder;
   }

   /**
    * Gets the content.
    * @return The content.
    */
   public byte[] getContent() {
      return buffer != null ? BufferUtil.toArray(buffer) : new byte[0];
   }

   /**
    * Holds the response content.
    */
   private ByteBuffer buffer;
}
