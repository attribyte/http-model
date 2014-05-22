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

import com.google.common.collect.Maps;

import java.io.UnsupportedEncodingException;
import java.util.Collection;
import java.util.Map;

/**
 * Builds HTTP responses.
 */
public class ResponseBuilder {

   /**
    * Creates a response builder.
    */
   public ResponseBuilder() {
   }

   /**
    * Creates a response builder with a response code.
    * @param statusCode The response code.
    */
   public ResponseBuilder(final int statusCode) {
      this.statusCode = statusCode;
   }

   /**
    * Creates a response builder with a response code and body.
    * @param statusCode The response code.
    * @param body The response body.
    */
   public ResponseBuilder(final int statusCode, final byte[] body) {
      this.statusCode = statusCode;
      this.body = body;
   }

   /**
    * Create a response builder with a response code and body.
    * @param statusCode The response code.
    * @param body The response body.
    */
   public ResponseBuilder(final int statusCode, final String body) {
      this.statusCode = statusCode;
      try {
         this.body = body.getBytes("UTF-8");
      } catch(UnsupportedEncodingException uee) {
         this.body = body.getBytes();
      }
   }

   /**
    * Sets the response code for the response.
    * @param statusCode The response code.
    * @return A self-reference.
    */
   public ResponseBuilder setStatusCode(final int statusCode) {
      this.statusCode = statusCode;
      return this;
   }

   /**
    * Sets the response body.
    * @param body The body.
    * @return A self-reference.
    */
   public ResponseBuilder setBody(byte[] body) {
      this.body = body;
      return this;
   }

   /**
    * Sets the response body.
    * <p>
    * Converts to <code>UTF-8</code>
    * </p>
    * @param body The body.
    * @return A self-reference.
    */
   public ResponseBuilder setBody(String body) {

      try {
         this.body = body.getBytes("UTF-8");
      } catch(UnsupportedEncodingException uee) {
         this.body = body.getBytes();
      }

      return this;
   }

   /**
    * Creates the response.
    * @return The response.
    */
   public Response create() {
      return new Response(statusCode, headers, body);
   }

   /**
    * Adds a header for the request to be built.
    * @param name The header name.
    * @param value The header value.
    * @return A self-reference.
    */
   public ResponseBuilder addHeader(final String name, final String value) {
      String lcName = name.toLowerCase();
      Header currHeader = headers.get(lcName);
      if(currHeader == null) {
         headers.put(lcName, new Header(name, value));
      } else {
         headers.put(lcName, currHeader.addValue(value));
      }
      return this;
   }

   /**
    * Adds a collection of headers for the request to be built.
    * @param headers The headers to add.
    * @return A self-reference.
    */
   public ResponseBuilder addHeaders(final Collection<Header> headers) {
      if(headers != null) {
         for(Header header : headers) {
            this.headers.put(header.getName(), header);
         }
      }
      return this;
   }

   /**
    * Adds a map of headers for the request.
    * @param headerMap The map of headers.
    * @return A self-reference.
    */
   public ResponseBuilder addHeaders(final Map<?, ?> headerMap) {
      if(headerMap != null) {
         this.headers.putAll(Header.createMap(headerMap));
      }
      return this;
   }

   /**
    * Adds an attribute to the request to be built.
    * @param name The attribute name.
    * @param object The attribute value.
    * @return A self-reference.
    */
   public ResponseBuilder addAttribute(final String name, final Object object) {
      if(this.attributes == null) {
         this.attributes = Maps.newHashMapWithExpectedSize(4);
      }
      this.attributes.put(name, object);
      return this;
   }

   /**
    * Adds a map of attributes for the request.
    * @param attributes The map of attributes.
    * @return A self-reference.
    */
   public ResponseBuilder addAttributes(final Map<String, Object> attributes) {
      if(attributes != null) {
         this.attributes.putAll(attributes);
      }
      return this;
   }

   final Map<String, Header> headers = Maps.newHashMapWithExpectedSize(16);
   Map<String, Object> attributes = null;
   byte[] body;
   int statusCode;
}

