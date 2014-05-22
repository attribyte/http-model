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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * A HTTP request parameter.
 */
public final class Parameter extends ImmutableNamedValues {

   private static final Parameter NULL_INSTANCE = new Parameter(null, (String[])null);

   /**
    * A <code>NameComparator</code> instance. Safe for use by many threads.
    */
   public static final NameComparator nameComparator = new NameComparator();

   @SuppressWarnings("unchecked")
   /**
    * Creates a map of parameters from a generic map.
    * <p>
    *   Map values may be <code>Parameter</code>, <code>String[]</code>, or a <code>Collection</code> If
    *   value is none of these, <code>toString</code> is used to generate a single value.
    * </p>
    */
   static final Map<String, Parameter> createMap(Map inputParameters) {

      if(inputParameters == null) {
         return Collections.emptyMap();
      } else {
         HashMap<String, Parameter> parameters = new HashMap<String, Parameter>();
         NULL_INSTANCE.copyMap(inputParameters, parameters, true); //Intern keys
         return parameters;
      }
   }

   /**
    * Creates a parameter.
    * @param name The parameter name.
    * @param value The parameter value.
    */
   public Parameter(String name, String value) {
      super(name, value);
   }

   /**
    * Creates a multi-valued parameter.
    * @param name The parameter name.
    * @param values The parameter values.
    */
   public Parameter(String name, String[] values) {
      super(name, values);
   }

   /**
    * Returns a copy of this parameter with the new value added.
    * @param value The added value.
    * @return The new parameter.
    */
   public Parameter addValue(String value) {
      String[] newValues = new String[values.length + 1];
      System.arraycopy(values, 0, newValues, 0, values.length);
      newValues[values.length] = value;
      return new Parameter(name, newValues);
   }

   @Override
   protected Parameter create(String name, String[] values) {
      return new Parameter(name, values);
   }

   @Override
   protected Parameter create(String name, String value) {
      return new Parameter(name, value);
   }
}

