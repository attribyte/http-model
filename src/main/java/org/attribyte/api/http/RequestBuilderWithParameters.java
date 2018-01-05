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

import com.google.common.collect.Maps;
import org.attribyte.api.InvalidURIException;

import java.net.URI;
import java.util.Map;

/**
 * Builds HTTP requests that allow parameters.
 */
public abstract class RequestBuilderWithParameters extends RequestBuilder {

   /**
    * Creates a request builder with a parsed URI and
    * a generic map of parameters (maybe...from a servlet request).
    * The URI parameters are not parsed, nor checked against the specified parameters.
    * @param uri The URI string to be parsed.
    * @param parameters The parameters.
    * @throws InvalidURIException if URI is invalid.
    */
   protected RequestBuilderWithParameters(final String uri, final Map<?,?> parameters) throws InvalidURIException {
      super(uri);
      this.caseSensitiveParameters = false;
      this.parameters = Parameter.createMap(parameters);
   }

   /**
    * Creates a request builder with a parsed URI.
    * @param uri The URI string to be parsed.
    * @param caseSensitiveParameters Should case be preserved for URI parameter names?
    * @throws InvalidURIException if URI is invalid.
    */
   protected RequestBuilderWithParameters(final String uri, final boolean caseSensitiveParameters) throws InvalidURIException {
      super(uri);
      this.caseSensitiveParameters = caseSensitiveParameters;
      this.parameters = Maps.newHashMapWithExpectedSize(8);

   }

   /**
    * Creates a request builder with parameter case-sensitivity specified.
    * @param uri The URI.
    * @param caseSensitiveParameters Should case be preserved for URI parameter names?
    */
   protected RequestBuilderWithParameters(final URI uri, final boolean caseSensitiveParameters) {
      super(uri);
      this.caseSensitiveParameters = caseSensitiveParameters;
      this.parameters = Maps.newHashMapWithExpectedSize(8);
   }

   final Map<String, Parameter> parameters;
   final boolean caseSensitiveParameters;
}

