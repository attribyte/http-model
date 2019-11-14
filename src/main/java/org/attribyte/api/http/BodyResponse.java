/*
 * Copyright 2014 Attribyte, LLC
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

import com.google.protobuf.ByteString;

import java.util.Collection;
import java.util.Map;

/**
 * A response that holds the full response body in-memory.
 */
public class BodyResponse extends Response {

   /**
    * Creates a response.
    * @param statusCode The HTTP response status code.
    * @param headers The response headers.
    * @param body The response body.
    * @param attributes A map of attributes associated with the response.
    * @param stats Full stats for the response.
    * @param cookies A collection of cookies.
    */
   BodyResponse(final int statusCode, final Map<?, ?> headers,
                final ByteString body, final Map<String, Object> attributes,
                final Stats stats, final Collection<Cookie> cookies) {
      super(statusCode, headers, attributes, stats, cookies);
      this.body = body;
   }

   @Override
   public ByteString getBody() {
      return body;
   }

   /**
    * The (immutable) body.
    */
   public final ByteString body;
}
