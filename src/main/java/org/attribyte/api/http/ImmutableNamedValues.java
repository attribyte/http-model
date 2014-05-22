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

import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;

/**
 * An immutable pair of name-values.
 * <p>
 * Any <code>null</code> values are translated to empty strings.
 * </p>
 */
abstract class ImmutableNamedValues {

   /**
    * A comparator to sort by name, case ignored.
    */
   static final class NameComparator implements Comparator<ImmutableNamedValues> {

      @Override
      public int compare(ImmutableNamedValues p1, ImmutableNamedValues p2) {
         String n1 = p1.getName();
         String n2 = p2.getName();
         return n1.compareToIgnoreCase(n2);
      }
   }

   /**
    * Creates a single-valued instance.
    * @param name The name.
    * @param value The value.
    */
   protected ImmutableNamedValues(final String name, final String value) {
      this.name = name;
      if(value != null && value.length() > 0) {
         this.values = new String[]{value};
      } else {
         this.values = EMPTY_VALUES;
      }
   }

   /**
    * Creates a multi-valued instance.
    * <p>
    * If input values array is <code>null</code> or zero-length, values are initialized
    * as an array with a single empty string.
    * </p>
    * @param name The name.
    * @param values The values.
    */
   protected ImmutableNamedValues(final String name, final String[] values) {
      this.name = name;
      this.values = copyValues(values);
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
    */
   public String getValue() {
      return values[0];
   }

   /**
    * Gets all the values.
    * @return The values.
    */
   public String[] getValues() {
      return values.clone();
   }

   @Override
   public String toString() {
      StringBuilder buf = new StringBuilder(name);
      buf.append(":");
      buf.append(values[0]);
      for(int i = 1; i < values.length; i++) {
         buf.append(',').append(values[i]);
      }
      return buf.toString();
   }

   protected abstract ImmutableNamedValues create(String name, String value);

   protected abstract ImmutableNamedValues create(String name, String[] values);

   @SuppressWarnings("unchecked")
   /**
    * Creates maps of named values from a map that may contain strings,
    * array of strings, objects, etc.
    */
   final void copyMap(Map inputMap, Map outputMap, boolean internKeys) {

      if(inputMap != null) {
         Iterator<Map.Entry> iter = inputMap.entrySet().iterator();
         while(iter.hasNext()) {
            Map.Entry curr = iter.next();
            Object key = curr.getKey();
            String strKey = internKeys ? key.toString().intern() : key.toString();
            Object value = curr.getValue();
            if(value instanceof ImmutableNamedValues) {
               outputMap.put(strKey, value);
            } else if(value instanceof Collection) {
               Collection c = (Collection)value;
               String[] values = new String[c.size()];
               int pos = 0;
               for(Object o : c) {
                  values[pos++] = o.toString();
               }
               outputMap.put(strKey, create(strKey, values));
            } else if(value instanceof String[]) {
               outputMap.put(strKey, create(strKey, (String[])value));
            } else {
               outputMap.put(strKey, create(strKey, new String[]{value == null ? null : value.toString()}));
            }
         }
      }
   }

   /**
    * Copies input values into internal format by translating <code>null</code> values
    * to empty strings.
    * @param values The input values.
    * @return The internal values.
    */
   static final String[] copyValues(String[] values) {
      if(values == null || values.length == 0) {
         return EMPTY_VALUES;
      } else {
         String[] newValues = new String[values.length];
         for(int i = 0; i < values.length; i++) {
            newValues[i] = values[i] != null ? values[i] : "";
         }
         return newValues;
      }
   }

   /**
    * An array containing a single empty string.
    */
   private static final String[] EMPTY_VALUES = new String[]{""};

   protected final String name;
   protected final String[] values;
}