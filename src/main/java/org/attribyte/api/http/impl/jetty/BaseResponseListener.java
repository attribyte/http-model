//
//  ========================================================================
//  Copyright (c) 1995-2019 Mort Bay Consulting Pty. Ltd.
//  ------------------------------------------------------------------------
//  All rights reserved. This program and the accompanying materials
//  are made available under the terms of the Eclipse Public License v1.0
//  and Apache License v2.0 which accompanies this distribution.
//
//      The Eclipse Public License is available at
//      http://www.eclipse.org/legal/epl-v10.html
//
//      The Apache License v2.0 is available at
//      http://www.opensource.org/licenses/apache2.0.php
//
//  You may elect to redistribute this code under either of these licenses.
//  ========================================================================
//
//  Modified to allow partial response, even if Content-Length exceeds buffer capacity.
//  See: https://github.com/eclipse/jetty.project/blob/jetty-9.4.x/jetty-client/src/main/java/org/eclipse/jetty/client/util/BufferingResponseListener.java
//


package org.attribyte.api.http.impl.jetty;

import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.api.Response;
import org.eclipse.jetty.http.HttpFields;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpMethod;
import java.util.Locale;

abstract class BaseResponseListener extends StatsListener {

   /**
    * Thrown when buffering capacity or maximum length is exceeded.
    */
   protected static class CapacityReached extends Exception {
      CapacityReached(final String message) {
         super(message);
      }
   }

   /**
    * Creates an instance with the given maximum length
    * @param maxLength the maximum length of the content
    * @param truncateOnLimit If {@code true}, content will be truncated if maximum length is reached without error.
    */
   public BaseResponseListener(final int maxLength,
                               final boolean truncateOnLimit) {
      if(maxLength < 0) {
         throw new IllegalArgumentException("Invalid max length " + maxLength);
      }

      this.maxLength = maxLength;
      this.truncateOnLimit = truncateOnLimit;
   }

   @Override
   public void onHeaders(Response response) {
      super.onHeaders(response);
      Request request = response.getRequest();
      HttpFields headers = response.getHeaders();

      long length = headers.getLongField(HttpHeader.CONTENT_LENGTH.asString());
      if(truncateOnLimit || HttpMethod.HEAD.is(request.getMethod())) {
         length = 0;
      }

      if(length > maxLength) {
         response.abort(new CapacityReached("Buffering capacity " + maxLength + " exceeded"));
         return;
      }

      String contentType = headers.get(HttpHeader.CONTENT_TYPE);
      if(contentType != null) {
         String media = contentType;

         String charset = "charset=";
         int index = contentType.toLowerCase(Locale.ENGLISH).indexOf(charset);
         if(index > 0) {
            media = contentType.substring(0, index);
            String encoding = contentType.substring(index + charset.length());

            // Sometimes charsets arrive with an ending semicolon.
            int semicolon = encoding.indexOf(';');
            if(semicolon > 0) {
               encoding = encoding.substring(0, semicolon).trim();
            }
            // Sometimes charsets are quoted.
            int lastIndex = encoding.length() - 1;
            if(encoding.charAt(0) == '"' && encoding.charAt(lastIndex) == '"') {
               encoding = encoding.substring(1, lastIndex).trim();
            }

            this.encoding = encoding;
         }

         int semicolon = media.indexOf(';');
         if(semicolon > 0) {
            media = media.substring(0, semicolon).trim();
         }

         this.mediaType = media;
      }
   }

   /**
    * @return The media type.
    */
   public String getMediaType() {
      return mediaType;
   }

   /**
    * @return The encoding.
    */
   public String getEncoding() {
      return encoding;
   }

   /**
    * Should the response be truncated if {@code maxResponseBytes} is reached
    * instead of allowing an exception to be thrown?
    */
   protected final boolean truncateOnLimit;

   /**
    * The maximum allowed length of the response in bytes.
    */
   protected final int maxLength;

   /**
    * The media type.
    */
   protected String mediaType;

   /**
    * The encoding.
    */
   protected String encoding;
}
