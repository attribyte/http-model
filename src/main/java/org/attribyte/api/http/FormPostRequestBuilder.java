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

import org.attribyte.api.InvalidURIException;

import java.net.URI;

/**
 * Builds HTTP <code>POST</code> requests with body specified as <code>application/x-www-form-urlencoded</code> parameters.
 */
public class FormPostRequestBuilder extends RequestBuilderWithParameters {

   /**
    * Creates a <code>POST</code> request builder.
    * @param uri The URI. If a query string is specified, it is preserved but parameters are ignored.
    * @throws InvalidURIException if URI is invalid.
    */
   public FormPostRequestBuilder(final String uri) throws InvalidURIException {
      super(uri, true);
   }

   /**
    * Creates a <code>POST</code> request builder.
    * @param uri The URI. If a query string is specified, it is preserved but parameters are ignored.
    */
   public FormPostRequestBuilder(final URI uri) {
      super(uri, true);
   }

   @Override
   public Request create() {
      return new Request(Request.Method.POST, uri, headers, parameters, (byte[])null, attributes);
   }
}

