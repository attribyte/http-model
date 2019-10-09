/*
 * Copyright 2010, 2019 Attribyte, LLC
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

import org.attribyte.api.InvalidURIException;

import java.net.URI;
import java.util.Map;

/**
 * Builds immutable HTTP {@code OPTIONS} requests.
 */
public class OptionsRequestBuilder extends RequestBuilderWithParameters {

   /**
    * Creates a {@code OPTIONS} request builder with URI parsed from a string and
    * pre-parsed parameters.
    * The URI parameters are not parsed, nor checked against the specified parameters.
    * @param uri The URI string to be parsed.
    * @param parameters A generic map of parameters.
    * @throws InvalidURIException if URI is invalid.
    */
   public OptionsRequestBuilder(final String uri, final Map<?,?> parameters) throws InvalidURIException {
      super(uri, parameters);
   }

   /**
    * Creates a {@code OPTIONS} request builder with URI parsed from a string with parameter case-sensitivity specified.
    * @param uri The URI string to be parsed.
    * @param caseSensitiveParameters Should case be preserved for URI parameter names?
    * @throws InvalidURIException if URI is invalid.
    */
   public OptionsRequestBuilder(final String uri, final boolean caseSensitiveParameters) throws InvalidURIException {
      super(uri, caseSensitiveParameters);
      String qs = this.uri.getQuery();
      if(qs != null) {
         this.parameters.putAll(Request.parseParameters(qs, caseSensitiveParameters));
      }
   }

   /**
    * Creates a {@code OPTIONS} request builder with parameter case-sensitivity specified.
    * @param uri The URI.
    * @param caseSensitiveParameters Should case be preserved for URI parameter names?
    */
   public OptionsRequestBuilder(final URI uri, final boolean caseSensitiveParameters) {
      super(uri, caseSensitiveParameters);
      String qs = this.uri.getQuery();
      if(qs != null) {
         this.parameters.putAll(Request.parseParameters(qs, caseSensitiveParameters));
      }
   }

   /**
    * Creates a {@code OPTIONS} request builder with URI parsed from a string.
    * <p>
    * Case is preserved for parameter names.
    * </p>
    * @param uri The URI string to be parsed.
    * @throws InvalidURIException if URI is invalid.
    */
   public OptionsRequestBuilder(final String uri) throws InvalidURIException {
      super(uri, true);
      String qs = this.uri.getQuery();
      if(qs != null) {
         this.parameters.putAll(Request.parseParameters(qs, caseSensitiveParameters));
      }
   }

   /**
    * Creates a {@code OPTIONS} request builder.
    * <p>
    * Case is preserved for parameter names.
    * </p>
    * @param uri The URI.
    */
   public OptionsRequestBuilder(final URI uri) {
      super(uri, true);
      String qs = this.uri.getQuery();
      if(qs != null) {
         this.parameters.putAll(Request.parseParameters(qs, caseSensitiveParameters));
      }
   }

   @Override
   public Request create() {
      return new Request(Request.Method.OPTIONS, uri, headers, parameters, caseSensitiveParameters, (byte[])null, attributes, cookies);
   }
}

