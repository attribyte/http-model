package org.attribyte.api.http;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;

import java.util.Collection;

public class NamedValues {

   /**
    * Copies values from an array to an immutable list.
    * Empty or <code>null</code> values are ignored.
    * @param values The input values.
    * @return The internal values.
    */
   static final ImmutableList<String> copyValues(String[] values) {
      if(values == null || values.length == 0) {
         return ImmutableList.of();
      } else {
         ImmutableList.Builder<String> builder = ImmutableList.builder();
         for(final String value : values) {
            if(!Strings.isNullOrEmpty(value)) {
               builder.add(value);
            }
         }
         return builder.build();
      }
   }

   /**
    * Copies values from a collection to an immutable list.
    * Empty or <code>null</code> values are ignored.
    * @param values The input values.
    * @return The internal values.
    */
   static final ImmutableList<String> copyValues(Collection<String> values) {
      if(values == null) {
         return ImmutableList.of();
      } else {
         ImmutableList.Builder<String> builder = ImmutableList.builder();
         for(final String value : values) {
            if(!Strings.isNullOrEmpty(value)) {
               builder.add(value);
            }
         }
         return builder.build();
      }
   }

   static final Joiner valueJoiner = Joiner.on(',').useForNull("[null]");

   /**
    * Creates a string for named values.
    * @param name The name.
    * @param values The collection of values.
    * @return The string.
    */
   static final String toString(final String name, final Collection<String> values) {
      return values != null ? name + "=" + valueJoiner.join(values) : name + "=";
   }
}