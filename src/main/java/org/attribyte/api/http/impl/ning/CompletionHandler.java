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

import com.ning.http.client.AsyncCompletionHandler;
import com.ning.http.client.Response;
import org.attribyte.api.http.ResponseBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract class CompletionHandler extends AsyncCompletionHandler<Response> {

   CompletionHandler(final int maxResponseBytes) {
      this.maxResponseBytes = maxResponseBytes;
   }

   @Override
   public com.ning.http.client.Response onCompleted(com.ning.http.client.Response response) throws Exception {
      ResponseBuilder builder = new ResponseBuilder();
      builder.setStatusCode(response.getStatusCode());
      Set<Map.Entry<String, List<String>>> entries = response.getHeaders().entrySet();
      for(Map.Entry<String, List<String>> header : entries) {
         header.getValue().forEach(value -> builder.addHeader(header.getKey(), value));
      }
      InputStream is = response.getResponseBodyAsStream();
      if(is != null) {
         try {
            builder.setBody(org.attribyte.api.http.Request.bodyFromInputStream(is, maxResponseBytes));
         } finally {
            try {
               is.close();
            } catch(IOException ioe) {
               failed(ioe);
            }
         }
      }
      completed(builder.create());
      return response;
   }

   @Override
   public void onThrowable(Throwable t) {
      failed(t);
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

   private final int maxResponseBytes;
}
