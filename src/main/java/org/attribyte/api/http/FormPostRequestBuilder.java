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

package org.attribyte.api.http;

import org.attribyte.api.InvalidURIException;

import java.net.URI;
import java.util.Map;

/**
 * Builds immutable HTTP {@code POST} requests with body specified as {@code application/x-www-form-urlencoded} parameters.
 * <p>
 * Parameters in the URI are <em>not</em> parsed.
 * </p>
 */
public class FormPostRequestBuilder extends RequestBuilderWithParameters {

   /**
    * Creates a {@code POST} request builder.
    * @param uri The URI. If a query string is specified, it is preserved but parameters are ignored.
    * @throws InvalidURIException if URI is invalid.
    */
   public FormPostRequestBuilder(final String uri) throws InvalidURIException {
      super(uri, true);
   }

   /**
    * Creates a {@code POST} request builder.
    * @param uri The URI. If a query string is specified, it is preserved but parameters are ignored.
    */
   public FormPostRequestBuilder(final URI uri) {
      super(uri, true);
   }

   @Override
   public Request create() {
      return new Request(Request.Method.POST, uri, headers, parameters, caseSensitiveParameters, (byte[])null, attributes, cookies);
   }


   /**
    * Adds a parameter.
    * @param name The parameter name.
    * @param value The parameter value.
    */
   public void addParameter(final String name, final String value) {

      String lcName = caseSensitiveParameters ? name.toLowerCase() : name;
      Parameter currParameter = parameters.get(lcName);
      if(currParameter == null) {
         parameters.put(lcName, new Parameter(name, value));
      } else {
         parameters.put(lcName, currParameter.addValue(value));
      }
   }

   /**
    * Adds a multi-valued parameter.
    * @param name The parameter name.
    * @param values The parameter values.
    */
   public void setParameter(final String name, final String[] values) {
      String lcName = caseSensitiveParameters ? name.toLowerCase() : name;
      parameters.put(lcName, new Parameter(name, values)); //TODO - Think about behavior
   }

   /**
    * Adds a map of parameters.
    * <p>
    * The {@code toString} method will be called on map keys
    * to generate parameter names. Map values may be {@code String},
    * {@code String[]}, {@code Collection<String>}.
    * </p>
    * @param parameters The map of parameters.
    */
   public void addParameters(final Map<?, ?> parameters) {
      if(parameters != null) {
         this.parameters.putAll(Parameter.createMap(parameters));
      }
   }
}

