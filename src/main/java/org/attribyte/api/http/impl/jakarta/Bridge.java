/*
 * Copyright (C) 2010, 2016 Attribyte, LLC  All Rights Reserved.
 *
 * This software is the confidential and proprietary information of Attribyte, LLC.
 * ("Confidential Information").  You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Attribyte, LLC
 *
 * ATTRIBYTE, LLC MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE SUITABILITY OF
 * THE SOFTWARE, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
 * PURPOSE, OR NON-INFRINGEMENT. ATTRIBYTE, LLC SHALL NOT BE LIABLE FOR ANY DAMAGES
 * SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING OR DISTRIBUTING
 * THIS SOFTWARE OR ITS DERIVATIVES.
 */

package org.attribyte.api.http.impl.jakarta;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.io.ByteStreams;
import com.google.protobuf.ByteString;
import org.attribyte.api.http.DeleteRequestBuilder;
import org.attribyte.api.http.FormPostRequestBuilder;
import org.attribyte.api.http.GetRequestBuilder;
import org.attribyte.api.http.HeadRequestBuilder;
import org.attribyte.api.http.Header;
import org.attribyte.api.http.PostRequestBuilder;
import org.attribyte.api.http.PutRequestBuilder;
import org.attribyte.api.http.Request;
import org.attribyte.api.http.Request.Method;
import org.attribyte.api.http.Response;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;

/**
 * Utilities to bridge Jakarta Servlet API to Attribyte API.
 */
public class Bridge {

   /**
    * Per the servlet API definition: returns the original request, excluding the query string.
    * <p>
    * <cite>
    * Reconstructs the URL the client used to make the request.
    * The returned URL contains a protocol, server name, port number, and server path,
    * but it does not include query string parameters.
    * </cite>
    * </p>
    * @param request The request.
    * @return A {@code StringBuilder} containing the original URL, excluding the query string.
    */
   public static StringBuilder getRequestURL(final Request request) {

      URI uri = request.getURI();

      String scheme = uri.getScheme();
      String authority = uri.getRawAuthority();
      String path = uri.getRawPath();

      StringBuilder buf = new StringBuilder();

      if(scheme != null) {
         buf.append(scheme);
         buf.append("://");
      }

      if(authority != null) {
         buf.append(authority);
      }

      if(path != null) {
         buf.append(path);
      }

      return buf;
   }

   /**
    * Per the servlet API definition: Returns the part of the request URL
    * from the protocol name up to the query string in the first line of the HTTP request.
    * <p>
    * <cite>
    * Returns the part of the request URL from the protocol name up to the query string in the first line of the HTTP request.
    * The web container does not decode this String.
    * </cite>
    * </p>
    * @param request The request.
    * @return The URI.
    */
   public static String getRequestURI(final Request request) {
      return request.getURI().getRawPath();
   }

   /**
    * Creates a request from a Jakarta servlet HTTP request.
    * <p>
    *   Sets an attribute, {@code remoteAddr} with the address reported
    *   by the servlet API.
    * </p>
    * @param request The servlet request.
    * @param maxBodyBytes The maximum number of bytes read. If &lt; 1, the body is not read.
    * @return The request.
    * @throws IOException on invalid request.
    */
   @SuppressWarnings("unchecked")
   public static final Request fromServletRequest(final HttpServletRequest request,
                                                  final int maxBodyBytes) throws IOException {

      Map<String, Header> headers = Maps.newHashMapWithExpectedSize(8);
      List<String> valueList = Lists.newArrayListWithExpectedSize(2);
      Enumeration<?> headerNames = request.getHeaderNames();
      while(headerNames.hasMoreElements()) {
         String name = (String)headerNames.nextElement();
         Enumeration<?> headerValues = request.getHeaders(name);
         valueList.clear();
         while(headerValues.hasMoreElements()) {
            valueList.add((String)headerValues.nextElement());
         }

         if(valueList.size() == 1) {
            headers.put(name, new Header(name, valueList.get(0)));
         } else {
            headers.put(name, new Header(name, valueList.toArray(new String[0])));
         }
      }

      final String queryString = request.getQueryString();

      final String requestURL = Strings.isNullOrEmpty(queryString) ?
              request.getRequestURL().toString() : request.getRequestURL().append('?').append(queryString).toString();

      final Map<?,?> parameterMap = request.getParameterMap();

      Method method = Method.fromString(request.getMethod());
      switch(method) {
         case GET: {
            GetRequestBuilder grb = new GetRequestBuilder(requestURL, parameterMap);
            grb.addHeaders(headers);
            grb.addAttribute("remoteAddr", request.getRemoteAddr());
            return grb.create();
         }
         case HEAD: {
            HeadRequestBuilder hrb = new HeadRequestBuilder(requestURL, parameterMap);
            hrb.addHeaders(headers);
            hrb.addAttribute("remoteAddr", request.getRemoteAddr());
            return hrb.create();
         }
         case DELETE: {
            DeleteRequestBuilder drb = new DeleteRequestBuilder(requestURL, request.getParameterMap());
            drb.addHeaders(headers);
            drb.addAttribute("remoteAddr", request.getRemoteAddr());
            return drb.create();
         }
      }

      if(parameterMap != null && parameterMap.size() > 0) {
         FormPostRequestBuilder prb = new FormPostRequestBuilder(requestURL);
         prb.addHeaders(headers);
         prb.addParameters(request.getParameterMap());
         prb.addAttribute("remoteAddr", request.getRemoteAddr());
         return prb.create();
      } else {
         byte[] body = null;
         if(maxBodyBytes > 0) {
            try(InputStream is = request.getInputStream()) {
               body = Request.bodyFromInputStream(is, maxBodyBytes);
            }
         } else {
            ByteStreams.toByteArray(request.getInputStream()); //Read, but ignore the body...
         }

         if(method == Method.POST) {
            PostRequestBuilder prb = new PostRequestBuilder(requestURL, body);
            prb.addHeaders(headers);
            prb.addAttribute("remoteAddr", request.getRemoteAddr());
            return prb.create();
         } else {
            PutRequestBuilder prb = new PutRequestBuilder(requestURL, body);
            prb.addHeaders(headers);
            prb.addAttribute("remoteAddr", request.getRemoteAddr());
            return prb.create();
         }
      }
   }

   /**
    * Sends an Attribyte response using a Jakarta servlet response.
    * @param response The Attribyte response.
    * @param servletResponse The servlet response.
    * @throws java.io.IOException on transmit error.
    */
   public static final void sendServletResponse(Response response, HttpServletResponse servletResponse) throws IOException {

      servletResponse.setStatus(response.getStatusCode());
      Collection<Header> headers = response.getHeaders();
      for(Header header : headers) {
         String[] values = header.getValues();
         for(String value : values) {
            servletResponse.setHeader(header.getName(), value);
         }
      }

      ByteString bodyString = response.getBody();

      if(bodyString != null) {
         try(BufferedOutputStream baos = new BufferedOutputStream(servletResponse.getOutputStream())) {
            baos.write(bodyString.toByteArray());
         }
      }
   }
}
