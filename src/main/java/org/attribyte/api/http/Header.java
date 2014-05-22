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

import java.util.HashMap;
import java.util.Map;

/**
 * An immutable HTTP request or response header.
 * @author Matt Hamer
 */
public final class Header extends ImmutableNamedValues {

   private static final Header NULL_INSTANCE = new Header(null, (String[])null);

   /**
    * Creates a single-valued header.
    * @param name The header name.
    * @param value The header value.
    */
   public Header(final String name, final String value) {
      super(name, value);
   }

   /**
    * Creates a multi-valued header.
    * @param name The header name.
    * @param values The header values.
    */
   public Header(final String name, final String[] values) {
      super(name, values);
   }

   /**
    * Returns a copy of this header with the new value added.
    * @param value The added value.
    * @return The new header.
    */
   public Header addValue(String value) {
      String[] newValues = new String[values.length + 1];
      System.arraycopy(values, 0, newValues, 0, values.length);
      newValues[values.length] = value;
      return new Header(name, newValues);
   }

   @Override
   protected ImmutableNamedValues create(String name, String value) {
      return new Header(name, value);
   }

   @Override
   protected ImmutableNamedValues create(String name, String[] values) {
      return new Header(name, values);
   }

   @SuppressWarnings("unchecked")
   /**
    * Creates a map of headers from a generic map.
    * <p>
    *   Map values may be <code>Header</code> or <code>String[]</code>. If
    *   value is neither of these, <code>toString</code> is called
    *   to create <em>a single value</em>.
    *   Keys in the new map are lower-case.
    * </p>
    * @param inputHeaders The input header map.
    * @return The new header map.
    */
   static final Map<String, Header> createMap(Map inputHeaders) {

      HashMap<String, Header> headers = new HashMap<String, Header>();
      if(inputHeaders == null) {
         return headers;
      } else {
         NULL_INSTANCE.copyMap(inputHeaders, headers, true); //Intern keys
         return headers;
      }
   }
}

