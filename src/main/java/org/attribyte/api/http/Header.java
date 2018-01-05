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

import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * An immutable HTTP request or response header.
 * @author Matt Hamer
 */
public final class Header {

   /**
    * The content type header.
    */
   public static final String CONTENT_TYPE = "Content-Type";

   /**
    * Compare headers by name. Safe for use by many threads.
    */
   public static final Comparator<Header> nameComparator = new Comparator<Header>() {
      @Override
      public int compare(final Header h1, final Header h2) {
         return h1.name.compareToIgnoreCase(h2.name);
      }
   };

   /**
    * Creates a single-valued header.
    * @param name The header name.
    * @param value The header value.
    */
   public Header(final String name, final String value) {
      this.name = name;
      this.values = Strings.isNullOrEmpty(value) ? ImmutableList.<String>of() : ImmutableList.of(value);
   }

   /**
    * Creates a multi-valued header from an array of values.
    * @param name The header name.
    * @param values The header values.
    */
   public Header(final String name, final String[] values) {
      this.name = name;
      this.values = NamedValues.copyValues(values);
   }

   /**
    * Creates a multi-valued header from a collection of values.
    * @param name The header name.
    * @param values The header values.
    */
   public Header(final String name, final Collection<String> values) {
      this.name = name;
      this.values = NamedValues.copyValues(values);
   }

   /**
    * Returns a copy of this header with the new value added.
    * @param value The added value.
    * @return The new header.
    */
   public Header addValue(final String value) {
      return new Header(name, ImmutableList.<String>builder().addAll(values).add(value).build());
   }

   /**
    * Gets the name.
    * @return The name.
    */
   public String getName() {
      return name;
   }

   /**
    * Gets the first value.
    * @return The value or an empty string if no value.
    */
   public String getValue() {
      return values.isEmpty() ? "" : values.get(0);
   }

   /**
    * Gets all the values.
    * @return The values or an zero-lengh array if none.
    */
   public String[] getValues() {
      return values.toArray(new String[values.size()]);
   }

   /**
    * Gets an immutable list of values.
    * @return The values.
    */
   public ImmutableList<String> getValueList() {
      return values;
   }

   /**
    * Parses ';' separated parameters in a header value.
    * @param headerValue The header value.
    * @return A list of parameters.
    */
   public static List<Parameter> parseParameters(final String headerValue) {
      if(headerValue == null) {
         return Collections.emptyList();
      }

      int start = headerValue.indexOf(';');
      if(start < 2) {
         return Collections.emptyList();
      }

      List<Parameter> parameters = Lists.newArrayListWithExpectedSize(4);

      try {
         Map<String, String> parameterMap = parameterSplitter.split(headerValue.substring(start));
         for(Map.Entry<String, String> kv : parameterMap.entrySet()) {
            parameters.add(new Parameter(kv.getKey(), kv.getValue()));
         }

         return parameters;
      } catch(IllegalArgumentException iae) { //Invalid parameters...
         return Collections.emptyList();
      }
   }

   /**
    * Gets the charset from a content type header.
    * @param contentType The content type header value.
    * @param defaultCharset The default charset to return if none is specified.
    * @return The charset.
    */
   public static String getCharset(final String contentType, final String defaultCharset) {
      if(contentType == null) {
         return defaultCharset;
      }

      List<Parameter> parameters = Header.parseParameters(contentType);
      for(Parameter parameter : parameters) {
         if(parameter.name.equalsIgnoreCase("charset")) {
            return parameter.getValue();
         }
      }

      return defaultCharset;
   }

   /**
    * Splits parameters in header values.
    */
   private static Splitter.MapSplitter parameterSplitter =
           Splitter.on(';').omitEmptyStrings().limit(16).trimResults().withKeyValueSeparator('=');

   /**
    * Creates a mutable map of headers from a generic map.
    * @param inputHeaders The input header map.
    * @return The mutable map.
    */
   static final Map<String, Header> createMap(final Map<?,?> inputHeaders) {
      return createMap(inputHeaders, Maps.<String, Header>newHashMapWithExpectedSize(inputHeaders.size()));
   }

   /**
    * Creates an immutable map of headers from a generic map.
    * @param inputHeaders The input header map.
    * @return The mutable map.
    */
   static final ImmutableMap<String, Header> createImmutableMap(final Map<?,?> inputHeaders) {
      if(inputHeaders == null) {
         return ImmutableMap.of();
      }
      return ImmutableMap.copyOf(createMap(inputHeaders, Maps.<String, Header>newHashMapWithExpectedSize(inputHeaders.size())));
   }

   @SuppressWarnings("unchecked")
   /**
    * Creates a map of headers from a generic map.
    * <p>
    *   Map values may be {@code Header}, {@code String[]}, or {@code Collection<String>}.
    *   If value is none of these, {@code toString} is called
    *   to create <em>a single value</em>.
    *   Header keys are case-insensitive, so keys in the new map are lower-case.
    * </p>
    * @param inputHeaders The input header map.
    * @return The new header map.
    */
   static final Map<String, Header> createMap(final Map<?,?> inputHeaders, final Map<String, Header> outputMap) {

      if(inputHeaders == null) return Maps.newHashMap();
      for(final Map.Entry<?,?> curr : inputHeaders.entrySet()) {
         Object key = curr.getKey();
         String keyStr = key.toString().intern();
         String lcKey = keyStr.intern();
         Object value = curr.getValue();
         if(value instanceof Header) {
            outputMap.put(lcKey, (Header)value);
         } else if(value instanceof Collection) {
            Collection<?> c = (Collection)value;
            List<String> values = Lists.newArrayListWithExpectedSize(c.size());
            for(Object o : c) {
               if(o != null) {
                  values.add(o.toString());
               }
            }
            outputMap.put(lcKey, new Header(keyStr, values));
         } else if(value instanceof String[]) {
            outputMap.put(lcKey, new Header(keyStr, (String[])value));
         } else {
            outputMap.put(lcKey, new Header(keyStr, value != null ? value.toString() : null));
         }
      }

      return outputMap;
   }

   @Override
   public String toString() {
      return NamedValues.toString(name, values);
   }

   /**
    * The header name.
    */
   public final String name;

   /**
    * An immutable list of header values.
    */
   public final ImmutableList<String> values;
}

