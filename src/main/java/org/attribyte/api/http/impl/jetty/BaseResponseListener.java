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

import org.eclipse.jetty.client.Response;
import org.eclipse.jetty.http.HttpFields;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpMethod;

abstract class BaseResponseListener extends StatsListener {

   /**
    * Thrown when buffering capacity or maximum length is exceeded.
    */
   protected static class CapacityReached extends Exception {
      CapacityReached(final String message) {
         super(message);
      }
   }

   /**
    * Creates an instance with the given maximum length
    * @param maxLength the maximum length of the content
    * @param truncateOnLimit If {@code true}, content will be truncated if maximum length is reached without error.
    */
   public BaseResponseListener(final int maxLength,
                               final boolean truncateOnLimit) {
      if(maxLength < 0) {
         throw new IllegalArgumentException("Invalid max length " + maxLength);
      }

      this.maxLength = maxLength;
      this.truncateOnLimit = truncateOnLimit;
   }

   @Override
   public void onHeaders(Response response) {
      super.onHeaders(response);
      HttpFields headers = response.getHeaders();

      long length = headers.getLongField(HttpHeader.CONTENT_LENGTH.asString());
      if(truncateOnLimit || HttpMethod.HEAD.is(response.getRequest().getMethod())) {
         length = 0;
      }

      if(length > maxLength) {
         response.abort(new CapacityReached("Buffering capacity " + maxLength + " exceeded"));
      }
   }

   /**
    * Should the response be truncated if {@code maxResponseBytes} is reached
    * instead of allowing an exception to be thrown?
    */
   protected final boolean truncateOnLimit;

   /**
    * The maximum allowed length of the response in bytes.
    */
   protected final int maxLength;
}
