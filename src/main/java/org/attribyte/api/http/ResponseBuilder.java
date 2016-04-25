/*
 * Copyright 2010,2014 Attribyte, LLC
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

import com.google.common.base.Charsets;
import com.google.common.collect.Maps;
import com.google.common.io.ByteSource;
import com.google.protobuf.ByteString;

import java.util.Collection;
import java.util.Map;

/**
 * Builds immutable HTTP responses.
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
    * Creates a response builder with a response code and body as bytes.
    * @param statusCode The response code.
    * @param body The response body.
    */
   public ResponseBuilder(final int statusCode, final byte[] body) {
      this.statusCode = statusCode;
      this.body = body != null ? ByteString.copyFrom(body) : null;
   }

   /**
    * Creates a response builder with a response code and body as a string.
    * @param statusCode The response code.
    * @param body The response body.
    */
   public ResponseBuilder(final int statusCode, final String body) {
      this.statusCode = statusCode;
      this.body = body != null ? ByteString.copyFrom(body.getBytes(Charsets.UTF_8)) : null;
   }

   /**
    * Creates a response builder with a response code and body as a <code>ByteSource</code>.
    * @param statusCode The response code.
    * @param bodySource The response body source.
    */
   public ResponseBuilder(final int statusCode, final ByteSource bodySource) {
      this.statusCode = statusCode;
      this.bodySource = bodySource;
   }

   /**
    * Sets the response code.
    * @param statusCode The response code.
    * @return A self-reference.
    */
   public ResponseBuilder setStatusCode(final int statusCode) {
      this.statusCode = statusCode;
      return this;
   }

   /**
    * Sets the response body bytes.
    * @param body The body.
    * @return A self-reference.
    */
   public ResponseBuilder setBody(final byte[] body) {
      this.body = body != null ? ByteString.copyFrom(body) : null;
      return this;
   }

   /**
    * Sets the response body as a string.
    * @param body The body.
    * @return A self-reference.
    */
   public ResponseBuilder setBody(final String body) {
      this.body = body != null ? ByteString.copyFrom(body.getBytes(Charsets.UTF_8)) : null;
      return this;
   }

   /**
    * Sets a <code>ByteSource</code> for the response body.
    * @param body The body byte source.
    * @return A self-reference.
    */
   public ResponseBuilder setBody(ByteSource body) {
      this.bodySource = body;
      return this;
   }

   /**
    * Creates an immutable response.
    * @return The response.
    */
   public Response create() {
      return bodySource == null ? new BodyResponse(statusCode, headers, body, attributes) :
              new StreamedResponse(statusCode, headers, bodySource, attributes);
   }

   /**
    * Creates an immutable streamed response.
    * @return The response.
    */
   public StreamedResponse createStreamed() {
      final ByteSource bodySource =
              this.bodySource != null ? this.bodySource : body != null ? ByteSource.wrap(body.toByteArray()) : null;
      return new StreamedResponse(statusCode, headers, bodySource, attributes);
   }

   /**
    * Adds a header.
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
    * Adds a collection of headers.
    * @param headers The headers to add.
    * @return A self-reference.
    */
   public ResponseBuilder putHeaders(final Collection<Header> headers) {
      if(headers != null) {
         for(Header header : headers) {
            this.headers.put(header.getName().toLowerCase(), header);
         }
      }
      return this;
   }

   /**
    * Adds a map of headers.
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
    * Adds an attribute.
    * @param name The attribute name.
    * @param object The attribute value.
    * @return A self-reference.
    */
   public ResponseBuilder addAttribute(final String name, final Object object) {
      maybeCreateAttributes().put(name, object);
      return this;
   }

   /**
    * Adds a map of attributes.
    * @param attributes The map of attributes.
    * @return A self-reference.
    */
   public ResponseBuilder addAttributes(final Map<String, Object> attributes) {
      if(attributes != null) {
         maybeCreateAttributes().putAll(attributes);
      }
      return this;
   }

   /**
    * Creates the attributes map if necessary.
    * @return The attributes.
    */
   private Map<String, Object> maybeCreateAttributes() {
      if(this.attributes == null) {
         this.attributes = Maps.newHashMapWithExpectedSize(4);
      }
      return this.attributes;
   }

   //Every response is going to have headers...
   final Map<String, Header> headers = Maps.newHashMapWithExpectedSize(8);
   //...but maybe will have attributes...
   Map<String, Object> attributes = null;
   ByteString body = null;
   ByteSource bodySource = null;
   //Constructors force this to be initialized to something...
   int statusCode;
}