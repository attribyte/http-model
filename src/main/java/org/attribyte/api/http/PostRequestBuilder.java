/*
 * Copyright 2010 Attribyte, LLC 
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
import org.attribyte.api.InvalidURIException;

import java.net.URI;

/**
 * Builds immutable HTTP <code>POST</code> requests.
 */
public class PostRequestBuilder extends RequestBuilder {

   /**
    * Creates a <code>POST</code> request builder.
    * @param uri The URI string to be parsed.
    * @param body The request body.
    * @throws InvalidURIException if URI is invalid.
    */
   public PostRequestBuilder(final String uri, final byte[] body) throws InvalidURIException {
      super(uri);
      this.body = ByteString.copyFrom(body);
   }

   /**
    * Creates a <code>POST</code> request builder.
    * @param uri The URI.
    * @param body The request body.
    */
   public PostRequestBuilder(final URI uri, final byte[] body) {
      super(uri);
      this.body = ByteString.copyFrom(body);
   }

   /**
    * Creates a <code>POST</code> request builder.
    * @param uri The URI string to be parsed.
    * @param body The request body.
    * @throws InvalidURIException if URI is invalid.
    */
   public PostRequestBuilder(final String uri, final ByteString body) throws InvalidURIException {
      super(uri);
      this.body = body;
   }

   /**
    * Creates a <code>POST</code> request builder.
    * @param uri The URI.
    * @param body The request body.
    */
   public PostRequestBuilder(final URI uri, final ByteString body) {
      super(uri);
      this.body = body;
   }

   @Override
   public Request create() {
      return new Request(Request.Method.POST, uri, headers, null, false, body, attributes);
   }

   private final ByteString body;
}

