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

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * An immutable HTTP request (query string) parameter.
 */
public final class Parameter {

   /**
    * Compare parameters by name. Safe for use by many threads.
    */
   public static final Comparator<Parameter> nameComparator = new Comparator<Parameter>() {
      @Override
      public int compare(final Parameter p1, final Parameter p2) {
         return p1.name.compareTo(p2.name);
      }
   };

   /**
    * Creates a parameter.
    * @param name The parameter name.
    * @param value The parameter value.
    */
   public Parameter(final String name, final String value) {
      this.name = name;
      this.values = Strings.isNullOrEmpty(value) ? ImmutableList.<String>of() : ImmutableList.of(value);
   }

   /**
    * Creates a multi-valued parameter.
    * @param name The parameter name.
    * @param values The parameter values.
    */
   public Parameter(final String name, final String[] values) {
      this.name = name;
      this.values = NamedValues.copyValues(values);
   }

   /**
    * Creates a multi-valued parameter from a collection of values.
    * @param name The parameter name.
    * @param values The parameter values.
    */
   public Parameter(final String name, final Collection<String> values) {
      this.name = name;
      this.values = NamedValues.copyValues(values);
   }

   /**
    * Returns a copy of this parameter with the new value added.
    * @param value The added value.
    * @return The new parameter.
    */
   public Parameter addValue(final String value) {
      return new Parameter(name, ImmutableList.<String>builder().addAll(values).add(value).build());
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
    * <p>
    * Never returns null. If the parameter has no value(s), an
    * empty string is returned.
    * </p>
    */
   public String getValue() {
      return values.isEmpty() ? "" : values.get(0);
   }

   /**
    * Gets all the values.
    * @return The values.
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

   @Override
   public String toString() {
      return NamedValues.toString(name, values);
   }

   /**
    * The parameter name.
    */
   public final String name;

   /**
    * An immutable list of parameter values.
    */
   public final ImmutableList<String> values;

   /**
    * Creates a mutable map of parameters.
    * @param inputParameters The generic map.
    * @return The mutable map.
    */
   static final Map<String, Parameter> createMap(final Map inputParameters) {
      return createMap(inputParameters, Maps.<String, Parameter>newHashMapWithExpectedSize(inputParameters.size()));
   }

   /**
    * Creates an immutable map of parameters.
    * @param inputParameters The generic map.
    * @return The immutable map.
    */
   static final ImmutableMap<String, Parameter> createImmutableMap(final Map inputParameters) {
      if(inputParameters == null) {
         return ImmutableMap.of();
      }

      return ImmutableMap.copyOf(createMap(inputParameters));
   }

   @SuppressWarnings("unchecked")
   /**
    * Creates a map of parameters from a generic map.
    * <p>
    *   Map values may be {@code Parameter}, {@code String[]}, or a {@code Collection} If
    *   value is none of these, {@code toString} is used to generate a single value.
    * </p>
    */
   static final Map<String, Parameter> createMap(final Map inputParameters, final Map<String, Parameter> outputMap) {

      if(inputParameters == null) return Maps.newHashMap();

      for(final Map.Entry curr : (Iterable<Map.Entry>)inputParameters.entrySet()) {
         Object key = curr.getKey();
         String keyStr = key.toString();
         Object value = curr.getValue();
         if(value instanceof Parameter) {
            outputMap.put(keyStr, (Parameter)value);
         } else if(value instanceof Collection) {
            Collection c = (Collection)value;
            List<String> values = Lists.newArrayListWithExpectedSize(c.size());
            for(Object o : c) {
               if(o != null) {
                  values.add(o.toString());
               }
            }
            outputMap.put(keyStr, new Parameter(keyStr, values));
         } else if(value instanceof String[]) {
            outputMap.put(keyStr, new Parameter(keyStr, (String[])value));
         } else {
            outputMap.put(keyStr, new Parameter(keyStr, value != null ? value.toString() : null));
         }
      }

      return outputMap;
   }
}