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

import com.google.common.io.ByteSource;
import com.google.protobuf.ByteString;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;

/**
 * A response that allows the body to be streamed.
 */
public class StreamedResponse extends Response {

   /**
    * Creates a response.
    * @param statusCode The HTTP response status code.
    * @param headers The response headers.
    * @param body The response body source.
    * @param attributes The attributes.
    * @param cookies The cookies.
    */
   StreamedResponse(final int statusCode, final Map<?, ?> headers,
                    final ByteSource body, final Map<String, Object> attributes,
                    final Collection<Cookie> cookies) {
      super(statusCode, headers, attributes, null, cookies);
      this.body = body;
   }

   @Override
   public ByteString getBody() throws IOException {
      return body != null ? ByteString.copyFrom(body.read()) : null;
   }

   /**
    * Gets a source for the body.
    * @return The body, or {@code null} if none.
    */
   public ByteSource getBodySource() {
      return body;
   }

   /**
    * The source for the body.
    */
   public final ByteSource body;
}