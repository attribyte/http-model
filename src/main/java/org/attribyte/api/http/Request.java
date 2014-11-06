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

import com.google.common.base.Charsets;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.io.ByteStreams;
import com.google.protobuf.ByteString;
import org.attribyte.api.DataLimitException;
import org.attribyte.api.InvalidURIException;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;


/**
 * An immutable HTTP request.
 */
public final class Request {

   /**
    * The HTTP methods.
    */
   public static enum Method {

      /**
       * The HTTP <code>HEAD</code> method.
       */
      HEAD,

      /**
       * The HTTP <code>GET</code> method.
       */
      GET,

      /**
       * The HTTP <code>POST</code> method.
       */
      POST,

      /**
       * The HTTP <code>PUT</code> method.
       */
      PUT,

      /**
       * The HTTP <code>DELETE</code> method.
       */
      DELETE;

      /**
       * Gets a method from a string.
       * @param str The string.
       * @return The method or <code>null</code> if string does not match a valid method.
       */
      public static Method fromString(String str) {
         return strMap.get(str.toUpperCase());
      }

      private static final ImmutableMap<String, Method> strMap = ImmutableMap.of(
              "GET", GET,
              "POST", POST,
              "HEAD", HEAD,
              "DELETE", DELETE,
              "PUT", PUT
      );
   }

   /**
    * Creates a request with the body specified as a byte array.
    * @param method The method.
    * @param uri The URI.
    * @param headers The HTTP headers.
    * @param parameters The request parameters.
    * @param body The body. May be null.
    * @param attributes Additional attributes.
    */
   Request(final Method method, final URI uri, Map<String, Header> headers, Map<String, Parameter> parameters,
           final byte[] body, final Map<String, Object> attributes) {
      this.method = method;
      this.uri = uri;
      this.headers = Header.createImmutableMap(headers);
      this.parameters = Parameter.createImmutableMap(parameters);
      this.body = body != null ? ByteString.copyFrom(body) : null;
      this.attributes = attributes != null ? ImmutableMap.copyOf(attributes) : ImmutableMap.<String, Object>of();
   }

   /**
    * Creates a request with the body specified as a <code>ByteString</code>.
    * @param method The method.
    * @param uri The URI.
    * @param headers The HTTP headers.
    * @param parameters The request parameters.
    * @param body The body. May be null.
    * @param attributes Additional attributes.
    */
   Request(final Method method, final URI uri, Map<String, Header> headers, Map<String, Parameter> parameters,
           final ByteString body, final Map<String, Object> attributes) {
      this.method = method;
      this.uri = uri;
      this.headers = Header.createImmutableMap(headers);
      this.parameters = Parameter.createImmutableMap(parameters);
      this.body = body;
      this.attributes = attributes != null ? ImmutableMap.copyOf(attributes) : ImmutableMap.<String, Object>of();
   }

   /**
    * Gets the HTTP method.
    * @return The method.
    */
   public Method getMethod() {
      return method;
   }

   /**
    * Gets the query string, with decoding.
    * @return The decoded query string or <code>null</code> if none.
    */
   public String getQueryString() {
      return uri.getQuery();
   }

   /**
    * Gets the query string, without decoding.
    * @return The query string or <code>null</code> if none.
    */
   public String getRawQueryString() {
      return uri.getRawQuery();
   }

   /**
    * Gets the path component of the request URI.
    * <p>
    * Path always begins with '/' and is never <code>null</code>.
    * </p>
    * @return The path.
    */
   public String getRequestPath() {
      String path = uri.getPath();
      return path != null ? path : "/";
   }

   /**
    * Gets the path component of a URI.
    * <p>
    * Path always begins with '/' and is never <code>null</code>.
    * Components are decoded.
    * </p>
    * @param uri The URI.
    * @return The path.
    * @throws InvalidURIException if URI is invalid.
    */
   public static final String getRequestPath(final String uri) throws InvalidURIException {
      try {
         String path = new URI(uri).getPath();
         return path != null ? path : "/";
      } catch(URISyntaxException use) {
         throw new InvalidURIException(use);
      }
   }

   /**
    * Gets a URL for the host portion of a URI without decoding.
    * <p>
    * Includes the protocol, if specified. Never ends with '/'.
    * </p>
    * @param uri The URI.
    * @return The host URL.
    */
   public static final String getHostURL(final String uri) throws InvalidURIException {

      try {
         URI _uri = new URI(uri);
         String scheme = _uri.getScheme();
         String authority = _uri.getRawAuthority();
         StringBuilder buf = new StringBuilder();
         if(scheme != null) {
            buf.append(scheme).append("://");
         }

         if(authority != null) {
            buf.append(authority);
         }

         return buf.toString();

      } catch(URISyntaxException use) {
         throw new InvalidURIException(use);
      }
   }

   /**
    * Gets the URI.
    * @return The URI.
    */
   public URI getURI() { return uri; }

   /**
    * Gets the first header value.
    * @param name The header name.
    * @return The value or <code>null</code> if none.
    */
   public String getHeaderValue(final String name) {
      Header h = headers.get(name);
      if(h != null) return h.getValue();
      h = headers.get(name.toLowerCase());
      return h == null ? null : h.getValue();
   }

   /**
    * Gets all values for a header.
    * @param name The header name.
    * @return The values or <code>null</code> if none.
    */
   public String[] getHeaderValues(final String name) {
      Header h = headers.get(name);
      if(h != null) return h.getValues();
      h = headers.get(name.toLowerCase());
      return h == null ? null : h.getValues();
   }

   /**
    * Gets an immutable list of values for a header.
    * @param name The header name.
    * @return The values or <code>null</code> if none.
    */
   public ImmutableList<String> getHeaderValueList(final String name) {
      Header h = headers.get(name);
      if(h != null) return h.getValueList();
      h = headers.get(name.toLowerCase());
      return h == null ? ImmutableList.<String>of() : h.getValueList();
   }

   /**
    * Gets a header.
    * @param name The header name.
    * @return The header or <code>null</code> if none.
    */
   public Header getHeader(final String name) {
      Header h = headers.get(name);
      if(h != null) return h;
      return headers.get(name.toLowerCase());
   }

   /**
    * Gets the first parameter value.
    * @param name The parameter name.
    * @return The value or <code>null</code> if none.
    */
   public String getParameterValue(final String name) {
      Parameter p = parameters.get(name);
      return p == null ? null : p.getValue();
   }

   /**
    * Gets all values for a parameter.
    * @param name The parameter name.
    * @return The values or <code>null</code> if none.
    */
   public String[] getParameterValues(final String name) {
      Parameter p = parameters.get(name);
      return p == null ? null : p.getValues();
   }

   /**
    * Gets an immutable list of values for a parameter.
    * @param name The parameter name.
    * @return The immutable list of values.
    */
   public ImmutableList<String> getParameterValueList(final String name) {
      Parameter p = parameters.get(name);
      return p == null ? ImmutableList.<String>of() : p.getValueList();
   }

   /**
    * Gets all headers.
    * @return An unmodifiable collection of headers.
    */
   public Collection<Header> getHeaders() {
      return headers.values();
   }

   /**
    * Gets all parameters.
    * @return An unmodifiable collection of parameters.
    */
   public Collection<Parameter> getParameters() {
      return parameters.values();
   }

   /**
    * Gets the request body, if any.
    * @return The body, or <code>null</code> if none.
    */
   public ByteString getBody() {
      return body;
   }

   /**
    * Gets the value of the <code>Content-Type</code> header.
    * @return The content type, or <code>null</code> if none.
    */
   public String getContentType() {
      return getHeaderValue(Header.CONTENT_TYPE);
   }

   /**
    * Gets the charset specified with the request <code>Content-Type</code> header, if any.
    * @param defaultCharset The default charset to return if none is specified in the header.
    * @return The charset or the default charset.
    */
   public String getCharset(String defaultCharset) {
      return Header.getCharset(getContentType(), defaultCharset);
   }

   /**
    * The expected value for a form encoded header: <code>application/x-www-form-urlencoded</code>.
    */
   public static final String FORM_ENCODED_CONTENT_TYPE = "application/x-www-form-urlencoded";

   /**
    * Determine if the request is encoded as a form <code>application/x-www-form-urlencoded</code>
    * @return Is the request encoded as a form?
    */
   public boolean isFormEncoded() {

      if(method == Method.POST && parameters.size() > 0) {
         return true;
      }

      String contentType = getContentType();
      if(contentType == null) {
         return false;
      }

      contentType = contentType.toLowerCase().trim();
      return contentType.startsWith(FORM_ENCODED_CONTENT_TYPE);
   }

   /**
    * The name of an attribute that <em>may</em> hold the remote address.
    */
   public static final String REMOTE_ADDR = "remoteAddr";

   /**
    * Gets the IP address of the client that sent the request.
    * @return The IP address or <code>null</code> if unavailable.
    */
   public String getRemoteAddr() {
      return attributes == null ? null : (String)attributes.get(REMOTE_ADDR);
   }

   /**
    * Gets the server name (as specified in the <code>Host</code> header.
    * @return The server name, excluding any port, or <code>null</code> if unspecified.
    */
   public String getServerName() {
      String host = getHeaderValue("Host");
      if(Strings.isNullOrEmpty(host)) {
         return null;
      } else {
         int index = host.indexOf(':');
         if(index == -1) {
            return host;
         } else {
            return host.substring(0, index);
         }
      }
   }

   /**
    * Gets the components of the path (separated by '/').
    * @return The path components, or empty list if none.
    */
   public List<String> getPathComponents() {

      String path = uri.getPath();
      if(path.length() < 2) {
         return Collections.emptyList();
      } else {
         List<String> components = new ArrayList<String>(8);
         StringTokenizer tok = new StringTokenizer(path, "/");
         if(tok.hasMoreTokens()) {
            tok.nextToken(); //Skip the leading '/'
         }

         while(tok.hasMoreTokens()) {
            String currComponent = tok.nextToken().trim();
            if(currComponent.length() > 0) {
               components.add(currComponent);
            }
         }
         return components;
      }
   }

   /**
    * Gets the components of the path (separated by '/').
    * @param uri The URI.
    * @return The path components, or empty list if none.
    * @throws InvalidURIException on invalid URI.
    */
   public static final List<String> getPathComponents(String uri) throws InvalidURIException {

      String requestPath = getRequestPath(uri);
      if(requestPath.length() < 2) {
         return Collections.emptyList();
      }

      requestPath = requestPath.substring(1); //Remove leading '/'
      List<String> components = new ArrayList<String>(8);
      StringTokenizer tok = new StringTokenizer(requestPath, "/");
      while(tok.hasMoreTokens()) {
         String currComponent = tok.nextToken().trim();
         if(currComponent.length() > 0) {
            components.add(currComponent);
         }
      }
      return components;
   }

   /**
    * Adds headers to this request's existing headers, replacing any that are duplicated.
    * @param headers The headers to add.
    * @return The request with additional headers added.
    */
   public Request addHeaders(Collection<Header> headers) {
      Map<String, Header> newHeaders = this.headers == null ? new HashMap<String, Header>() : new HashMap<String, Header>(this.headers);
      for(Header header : headers) {
         newHeaders.put(header.getName(), header);
      }
      return new Request(this.method, this.uri, newHeaders, this.parameters, this.body, this.attributes);
   }

   /**
    * Reads the request body from an input stream.
    * <p>
    * Reads the body up to the specified maximum number of bytes.
    * If byte limit is exceeded, continues to read the rest of the stream,
    * ignoring the data.
    * </p>
    * @param is The input stream.
    * @param maxBytesRead The maximum bytes read.
    * @return The body.
    * @throws java.io.IOException on input exception, or data limit exceeded.
    */
   public static final byte[] bodyFromInputStream(final InputStream is, final int maxBytesRead) throws IOException {

      final InputStream cis;
      if(is instanceof BufferedInputStream) {
         cis = ByteStreams.limit(is, maxBytesRead);
      } else {
         cis = ByteStreams.limit(new BufferedInputStream(is), maxBytesRead);
      }

      byte[] body = ByteStreams.toByteArray(cis);
      if(body.length < maxBytesRead) {
         return body;
      } else {
         throw new DataLimitException("The size of the body exceeds the limit of " + maxBytesRead + " bytes");
      }
   }

   /**
    * Parse parameters from a query string.
    * <p>
    * Case is ignored for all names - all are converted to lower-case.
    * </p>
    * @param queryString The query string.
    * @return The map of parameters.
    */
   public static final Map<String, Parameter> parseParameters(String queryString) {
      return parseParameters(queryString, false);
   }

   /**
    * Parse parameters from a query string, preserving case in parameter names.
    * @param queryString The query string.
    * @return The map of parameters.
    */
   public static final Map<String, Parameter> parseParametersPreserveNameCase(String queryString) {
      return parseParameters(queryString, true);
   }

   /**
    * Parse parameters from a query string.
    * <p>
    * Query string should already be unescaped.
    * </p>
    * @param queryString The query string.
    * @param caseSensitiveNames Should case be preserved for parameter names?
    * @return The map of parameters.
    */
   public static final Map<String, Parameter> parseParameters(String queryString, final boolean caseSensitiveNames) {
      if(queryString == null || queryString.length() == 0) {
         return Collections.emptyMap();
      }

      switch(queryString.charAt(0)) {
         case '?':
         case '&': //We'll deal even though not valid
            queryString = queryString.substring(1);
      }

      Map<String, Parameter> parameterMap = Maps.newHashMapWithExpectedSize(8);

      StringTokenizer tok = new StringTokenizer(queryString, "&");
      while(tok.hasMoreTokens()) {
         String nv = tok.nextToken();
         int index = nv.indexOf("=");
         String name, value;
         if(index == -1 || index == nv.length() - 1) {
            name = nv;
            value = "";
         } else {
            name = nv.substring(index);
            value = nv.substring(index + 1);
         }

         if(!caseSensitiveNames) {
            name = name.toLowerCase();
         }

         Parameter currParam = parameterMap.get(name);
         if(currParam == null) {
            parameterMap.put(name, new Parameter(name, value));
         } else {
            parameterMap.put(name, currParam.addValue(value));
         }
      }

      return parameterMap;
   }

   @Override
   public String toString() {
      String newline = System.getProperty("line.separator");
      StringBuilder buf = new StringBuilder();
      buf.append(method != null ? method.toString() : "[null]");
      buf.append(" ");
      if(uri != null) {
         buf.append(uri.toString());
      }

      buf.append(newline).append(newline);
      buf.append("Headers: ").append(newline);
      if(headers != null) {
         for(Header header : headers.values()) {
            buf.append(header.toString());
            buf.append(newline);
         }
      } else {
         buf.append(newline);
      }

      buf.append(newline);
      buf.append("Parameters: ").append(newline);
      if(parameters != null) {
         for(Parameter parameter : parameters.values()) {
            buf.append(parameter.toString());
            buf.append(newline);
         }
      } else {
         buf.append(newline);
      }

      buf.append(newline);
      buf.append("Attributes: ").append(newline);
      if(attributes != null) {
         for(Map.Entry<String, Object> entry : attributes.entrySet()) {
            buf.append(entry.getKey()).append(":").append(entry.getValue().toString());
            buf.append(newline);
         }
      } else {
         buf.append(newline);
      }

      buf.append(newline);
      buf.append("Body: ").append(newline);
      if(body != null) {
         try {
            String bodyStr = body.toString(Charsets.UTF_8.name());
            buf.append(bodyStr);
         } catch(java.io.UnsupportedEncodingException uee) {
            buf.append("[Encoding Unsupported]");
         }
      }
      return buf.toString();
   }

   /**
    * The request method.
    */
   public final Method method;

   /**
    * The request URI.
    */
   public final URI uri;

   /**
    * An immutable map of headers.
    * <p>
    * Note that keys are lower-case.
    * </p>
    */
   public final ImmutableMap<String, Header> headers;

   /**
    * An immutable map of parameters.
    */
   public final ImmutableMap<String, Parameter> parameters;

   /**
    * An immutable map of attributes.
    */
   public final ImmutableMap<String, Object> attributes;

   /**
    * The request body. May be null.
    */
   public final ByteString body;
}