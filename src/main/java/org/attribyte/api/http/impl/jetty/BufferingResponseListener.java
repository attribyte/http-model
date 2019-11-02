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

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import org.attribyte.api.http.ResponseBuilder;
import org.eclipse.jetty.client.api.Response;
import org.eclipse.jetty.client.api.Response.Listener;
import org.eclipse.jetty.client.api.Result;
import org.eclipse.jetty.util.BufferUtil;

/**
 * <p>Implementation of {@link Listener} that buffers the content up to a maximum length
 * specified to the constructors.</p>
 * <p>The content may be retrieved from {@link #onSuccess(Response)} or {@link #onComplete(Result)}
 * via {@link #getContent()} or {@link #getContentAsString()}.</p>
 * <p>Instances of this class are not reusable, so one must be allocated for each request.</p>
 */
abstract class BufferingResponseListener extends BaseResponseListener {

   /**
    * Creates an instance with the given maximum length
    * @param maxLength the maximum length of the content
    */
   public BufferingResponseListener(final int maxLength,
                                    final boolean truncateOnLimit) {
      super(maxLength, truncateOnLimit);
   }

   @Override
   public void onContent(Response response, ByteBuffer content) {
      super.onContent(response, content);
      int length = content.remaining();
      if(length > BufferUtil.space(buffer)) {
         int requiredCapacity = buffer == null ? length : buffer.capacity() + length;
         if(requiredCapacity > maxLength) {
            response.abort(new CapacityReached("Buffering capacity " + maxLength + " exceeded"));
         }
         int newCapacity = Math.min(Integer.highestOneBit(requiredCapacity) << 1, maxLength);
         buffer = BufferUtil.ensureCapacity(buffer, newCapacity);
      }
      BufferUtil.append(buffer, content);
   }

   /**
    * Called when the request is completed with success.
    * @param response The complete response.
    */
   abstract protected void completed(final org.attribyte.api.http.Response response);

   /**
    * Called when the request fails with an exception.
    * @param failure The failure.
    */
   abstract protected void failed(final Throwable failure);

   @Override
   public void onComplete(Result result) {
      if(!result.isFailed()) {
         ResponseBuilder builder = fromResult(result, false);
         completed(builder.create());
      } else if(truncateOnLimit && result.getFailure() instanceof CapacityReached) {
         ResponseBuilder builder = fromResult(result, true);
         completed(builder.create());
      } else {
         failed(result.getFailure());
      }
   }

   private ResponseBuilder fromResult(final Result result,
                                      final boolean truncated) {

      ResponseBuilder builder = new ResponseBuilder();
      Response response = result.getResponse();
      builder.setStatusCode(response.getStatus());
      response.getHeaders().forEach(header -> {
         builder.addHeader(header.getName(), header.getValue()); //Note that getValues returns quoted csv so don't want that.
      });
      byte[] responseContent = getContent();
      if(responseContent != null) {
         builder.setBody(responseContent);
      }
      builder.setTiming(timing());

      if(truncated) {
         builder.addAttribute("truncated", Boolean.TRUE);
      }

      return builder;
   }

   /**
    * @return the content as bytes
    * @see #getContentAsString()
    */
   public byte[] getContent() {
      if(buffer == null) {
         return new byte[0];
      } else {
         return BufferUtil.toArray(buffer);
      }
   }

   /**
    * @return the content as a string, using the "Content-Type" header to detect the encoding
    * or defaulting to UTF-8 if the encoding could not be detected.
    * @see #getContentAsString(String)
    */
   public String getContentAsString() {
      String encoding = this.encoding;
      if(encoding == null) {
         return getContentAsString(StandardCharsets.UTF_8);
      } else {
         return getContentAsString(encoding);
      }
   }

   /**
    * @param encoding the encoding of the content bytes
    * @return the content as a string, with the specified encoding
    * @see #getContentAsString()
    */
   public String getContentAsString(String encoding) {
      if(buffer == null) {
         return null;
      } else {
         return BufferUtil.toString(buffer, Charset.forName(encoding));
      }
   }

   /**
    * @param encoding the encoding of the content bytes
    * @return the content as a string, with the specified encoding
    * @see #getContentAsString()
    */
   public String getContentAsString(Charset encoding) {
      if(buffer == null) {
         return null;
      } else {
         return BufferUtil.toString(buffer, encoding);
      }
   }

   /**
    * @return Content as InputStream
    */
   public InputStream getContentAsInputStream() {
      if(buffer == null) {
         return new ByteArrayInputStream(new byte[0]);
      } else {
         return new ByteArrayInputStream(buffer.array(), buffer.arrayOffset(), buffer.remaining());
      }
   }

   /**
    * Holds the response content.
    */
   private ByteBuffer buffer;
}
