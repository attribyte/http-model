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

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.protobuf.ByteString;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;


/**
 * An immutable HTTP response.
 */
public class Response {

   /**
    * Common HTTP response codes.
    */
   public static class Code {
      /**
       * Successful (200).
       */
      public static final int OK = 200;

      /**
       * Request was accepted (202).
       */
      public static final int ACCEPTED = 202;

      /**
       * No content (204).
       */
      public static final int NO_CONTENT = 204;

      /**
       * Request problem (400).
       */
      public static final int BAD_REQUEST = 400;

      /**
       * Request request requires authorization (401).
       */
      public static final int UNAUTHORIZED = 401;

      /**
       * Access to resource is forbidden (403).
       */
      public static final int FORBIDDEN = 403;

      /**
       * Resource was not found (404).
       */
      public static final int NOT_FOUND = 404;

      /**
       * Server produced an error (500).
       */
      public static final int SERVER_ERROR = 500;

      /**
       * Server is unavailable (503).
       */
      public static final int SERVER_UNAVAILABLE = 503;

      /**
       * Determines if a response code is "OK".
       * @param code The response code.
       * @return Is it "OK"?
       */
      public static final boolean isOK(int code) {
         return code > 199 && code < 300;
      }
   }

   /**
    * Creates a response.
    * @param statusCode The HTTP response status code.
    * @param headers The response headers.
    */
   public Response(final int statusCode, final Map<?, ?> headers) {
      this.statusCode = statusCode;
      this.headers = Header.createImmutableMap(headers);
      this.attributes = ImmutableMap.of();
      this.cookies = ImmutableList.of();
      this.timing = null;
      this.stats = null;
   }

   /**
    * Creates a response with attributes.
    * @param statusCode The HTTP response status code.
    * @param headers The response headers.
    * @param attributes The attributes.
    */
   public Response(final int statusCode, final Map<?, ?> headers, final Map<String, Object> attributes) {
      this.statusCode = statusCode;
      this.headers = Header.createImmutableMap(headers);
      this.attributes = attributes != null ? ImmutableMap.copyOf(attributes) : ImmutableMap.of();
      this.cookies = ImmutableList.of();
      this.timing = null;
      this.stats = null;
   }

   /**
    * Creates a response with attributes and timing.
    * @param statusCode The HTTP response status code.
    * @param headers The response headers.
    * @param attributes The attributes.
    * @param timing Timing information associated with the response.
    */
   public Response(final int statusCode, final Map<?, ?> headers, final Map<String, Object> attributes,
                   final Timing timing) {
      this.statusCode = statusCode;
      this.headers = Header.createImmutableMap(headers);
      this.attributes = attributes != null ? ImmutableMap.copyOf(attributes) : ImmutableMap.of();
      this.cookies = ImmutableList.of();
      this.timing = timing;
      this.stats = null;
   }

   /**
    * Creates a response with attributes, timing and cookies.
    * @param statusCode The HTTP response status code.
    * @param headers The response headers.
    * @param attributes The attributes.
    * @param timing Timing information associated with the response.
    * @param cookies A collection of cookies.
    */
   public Response(final int statusCode, final Map<?, ?> headers, final Map<String, Object> attributes,
                   final Timing timing, final Collection<Cookie> cookies) {
      this.statusCode = statusCode;
      this.headers = Header.createImmutableMap(headers);
      this.attributes = attributes != null ? ImmutableMap.copyOf(attributes) : ImmutableMap.of();
      this.cookies = cookies != null ? ImmutableList.copyOf(cookies) : ImmutableList.of();
      this.timing = timing;
      this.stats = null;
   }

   /**
    * Creates a response with attributes, stats and cookies.
    * @param statusCode The HTTP response status code.
    * @param headers The response headers.
    * @param attributes The attributes.
    * @param timing The timing.
    * @param stats The stats associated with the response.
    * @param cookies A collection of cookies.
    */
   public Response(final int statusCode, final Map<?, ?> headers, final Map<String, Object> attributes,
                   final Stats stats, final Timing timing, final Collection<Cookie> cookies) {
      this.statusCode = statusCode;
      this.headers = Header.createImmutableMap(headers);
      this.attributes = attributes != null ? ImmutableMap.copyOf(attributes) : ImmutableMap.of();
      this.cookies = cookies != null ? ImmutableList.copyOf(cookies) : ImmutableList.of();
      this.timing = timing;
      this.stats = stats;
   }

   /**
    * Gets the HTTP response code.
    * @return The response code.
    */
   public int getStatusCode() {
      return statusCode;
   }

   /**
    * Gets the response body as an immutable {@code ByteString}.
    * @return The response body, or {@code null} if none.
    * @throws IOException if the body is streamed and an input error occurs.
    */
   public ByteString getBody() throws IOException {
      return null;
   }

   /**
    * Gets the first header value.
    * @param name The header name.
    * @return The value or {@code null} if none.
    */
   public String getHeaderValue(String name) {
      Header h = headers.get(name.toLowerCase());
      return h == null ? null : h.getValue();
   }

   /**
    * Gets all values for a header.
    * @param name The header name.
    * @return The values or {@code null} if none.
    */
   public String[] getHeaderValues(String name) {
      Header h = headers.get(name.toLowerCase());
      return h == null ? null : h.getValues();
   }

   /**
    * Gets an immutable list of values for a header.
    * @param name The header name.
    * @return The values or {@code null} if none.
    */
   public ImmutableList<String> getHeaderValueList(String name) {
      Header h = headers.get(name);
      if(h != null) return h.getValueList();
      h = headers.get(name.toLowerCase());
      return h == null ? ImmutableList.of() : h.getValueList();
   }

   /**
    * Gets the value of the {@code Content-Type} header.
    * @return The content type, or {@code null} if none.
    */
   public String getContentType() {
      return getHeaderValue(Header.CONTENT_TYPE);
   }

   /**
    * Gets the charset specified for this response or the default
    * charset if none specified.
    * @param defaultCharset The default charset.
    * @return The charset.
    */
   public String getCharset(String defaultCharset) {
      return Header.getCharset(getContentType(), defaultCharset);
   }

   /**
    * Gets all headers.
    * @return An immutable collection of headers.
    */
   public Collection<Header> getHeaders() {
      return headers.values();
   }

   /**
    * Gets an attribute.
    * @param name The attribute name.
    * @return The attribute or {@code null} if none set.
    */
   public Object getAttribute(final String name) {
      return attributes == null ? null : attributes.get(name);
   }

   /**
    * Gets an attribute with a default value.
    * @param name The name.
    * @param defaultValue The default value.
    * @param <T> The expected type.
    * @return The variable or {@code null}.
    */
   @SuppressWarnings("unchecked")
   public <T> T getAttribute(String name, T defaultValue) {
      if(attributes == null) {
         return defaultValue;
      } else {
         T value = (T)attributes.get(name);
         return value != null ? value : defaultValue;
      }
   }

   /**
    * Gets the request/response timing, if available.
    * @return The timing or {@code null} if none set.
    */
   public Timing getTiming() {
      return timing;
   }

   @Override
   public String toString() {
      String newline = System.getProperty("line.separator");
      StringBuilder buf = new StringBuilder();
      buf.append(statusCode);

      buf.append(newline).append(newline);
      buf.append("Headers: ").append(newline);
      if(headers != null) {
         for(Header header : headers.values()) {
            buf.append(header.getName()).append(": ").append(NamedValues.valueJoiner.join(header.getValueList()));
            buf.append(newline);
         }
      } else {
         buf.append(newline);
      }

      buf.append(newline);
      buf.append("Attributes: ").append(newline);
      if(attributes != null) {
         for(Map.Entry<String, Object> entry : attributes.entrySet()) {
            buf.append(entry.getKey()).append(": ").append(entry.getValue().toString());
            buf.append(newline);
         }
      } else {
         buf.append(newline);
      }

      buf.append(newline);
      buf.append("Body: ").append(newline);

      try {
         ByteString body = getBody();
         if(body != null) {
            buf.append(new String(body.toByteArray(), Charsets.UTF_8));
         }
      } catch(IOException ioe) {
         ioe.printStackTrace();
      }

      if(timing != null) {
         buf.append(newline);
         buf.append(newline);
         buf.append(timing.toString());
      }

      return buf.toString();
   }

   /**
    * The status code.
    */
   public final int statusCode;

   /**
    * An immutable map of headers.
    * <p>
    * The keys in this map are <em>lower-case</em>.
    * </p>
    */
   public final ImmutableMap<String, Header> headers;

   /**
    * An immutable map of attributes.
    * <p>
    * Beware: The values may not be immutable.
    * </p>
    */
   public final ImmutableMap<String, Object> attributes;

   /**
    * An immutable list of cookies.
    */
   public final ImmutableList<Cookie> cookies;

   /**
    * Request/response timing, if any.
    */
   public final Timing timing;

   /**
    * The stats, if any.
    */
   public final Stats stats;
}

