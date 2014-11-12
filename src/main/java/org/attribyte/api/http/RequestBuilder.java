/*
 * Copyright 2010, 2014 Attribyte, LLC
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
import org.attribyte.api.InvalidURIException;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Map;

/**
 * Builds HTTP requests.
 */
public abstract class RequestBuilder {

   /**
    * Creates a builder with a parsed URI.
    * @param uri The URI string.
    * @throws InvalidURIException if URI is invalid.
    */
   protected RequestBuilder(final String uri) throws InvalidURIException {
      try {
         this.uri = new URI(uri);
      } catch(URISyntaxException use) {
         throw new InvalidURIException(use);
      }
   }

   /**
    * Creates a builder.
    * @param uri The URI.
    */
   protected RequestBuilder(final URI uri) {
      this.uri = uri;
   }

   /**
    * Creates the immutable request after all headers, parameters and attributes are added.
    * @return The request.
    */
   public abstract Request create();

   /**
    * Adds a header.
    * @param name The header name.
    * @param value The header value.
    * @return A self-reference.
    */
   public RequestBuilder addHeader(final String name, final String value) {
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
   public RequestBuilder addHeaders(final Collection<Header> headers) {
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
   public RequestBuilder addHeaders(final Map<?, ?> headerMap) {
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
   public RequestBuilder addAttribute(final String name, final Object object) {
      attributes.put(name, object);
      return this;
   }

   /**
    * Adds a map of attributes.
    * @param attributes The map of attributes.
    * @return A self-reference.
    */
   public RequestBuilder addAttributes(final Map<String, Object> attributes) {
      if(attributes != null) {
         this.attributes.putAll(attributes);
      }
      return this;
   }

   final Map<String, Header> headers = Maps.newHashMapWithExpectedSize(8);
   final Map<String, Object> attributes = Maps.newHashMapWithExpectedSize(2);
   final URI uri;
}

