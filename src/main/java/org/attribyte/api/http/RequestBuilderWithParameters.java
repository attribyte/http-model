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
 * Builds HTTP requests that allow parameters (<code>GET, HEAD, DELETE</code>).
 */
public abstract class RequestBuilderWithParameters extends RequestBuilder {

   /**
    * Creates a request builder with URI parsed from a string.
    * @param uri The URI string to be parsed.
    * @param caseSensitiveParameters Should case be preserved for URI parameter names?
    * @throws InvalidURIException if URI is invalid.
    */
   protected RequestBuilderWithParameters(final String uri, final boolean caseSensitiveParameters) throws InvalidURIException {
      super(uri);
      this.caseSensitiveParameters = caseSensitiveParameters;
   }

   /**
    * Creates a request builder.
    * @param uri The URI.
    * @param caseSensitiveParameters Should case be preserved for URI parameter names?
    */
   protected RequestBuilderWithParameters(final URI uri, final boolean caseSensitiveParameters) {
      super(uri);
      this.caseSensitiveParameters = caseSensitiveParameters;
   }

   /**
    * Adds a parameter to the request to be built.
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
    * Adds a parameter to the request to be built.
    * @param name The parameter name.
    * @param values The parameter values.
    */
   public void setParameter(final String name, final String[] values) {
      String lcName = caseSensitiveParameters ? name.toLowerCase() : name;
      parameters.put(lcName, new Parameter(name, values)); //TODO - Think about behavior
   }

   /**
    * Adds a map of parameters to the request to be built.
    * <p>
    * The <code>toString</code> method will be called on map keys
    * to generate parameter names. Map values may be <code>String</code>,
    * <code>String[]</code>, <code>Collection&lt;String></code>.
    * </p>
    * @param parameters The map of parameters.
    */
   public void addParameters(final Map<?, ?> parameters) {
      if(parameters != null) {
         this.parameters.putAll(Parameter.createMap(parameters));
      }
   }

   final Map<String, Parameter> parameters = Maps.newHashMapWithExpectedSize(8);
   final boolean caseSensitiveParameters;

}

