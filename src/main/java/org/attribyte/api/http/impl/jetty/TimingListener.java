/*
 * Copyright 2018 Attribyte, LLC
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

package org.attribyte.api.http.impl.jetty;

import org.attribyte.api.http.ResponseBuilder;
import org.attribyte.api.http.Timing;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.api.Response;
import org.eclipse.jetty.client.api.Result;
import org.eclipse.jetty.http.HttpField;

import java.nio.ByteBuffer;

abstract class TimingListener extends BufferingResponseListener implements Request.Listener {

   TimingListener(final int maxResponseBytes,
                  final boolean truncateOnLimit) {
      super(maxResponseBytes, truncateOnLimit);
   }

   @Override
   public void onQueued(Request request) {
      requestQueuedTick = getTick();
   }

   @Override
   public void onBegin(Request request) {
      //Ignore
   }

   @Override
   public void onHeaders(Request request) {
      //Ignore
   }

   @Override
   public void onCommit(Request request) {
      requestSentTick = getTick();
   }

   @Override
   public void onContent(Request request, ByteBuffer content) {
   }

   @Override
   public void onSuccess(Request request) {
      requestCompleteTick = getTick();
   }

   @Override
   public void onFailure(Request request, Throwable failure) {
      requestCompleteTick = getTick();
   }

   @Override
   public void onBegin(Response response) {
      responseStatusReceivedTick = getTick();
   }

   @Override
   public boolean onHeader(Response response, HttpField field) {
      firstHeaderReceivedTick = getTick();
      return true;
   }

   @Override
   public void onContent(Response response, ByteBuffer content) {
      if(responseContentStartedTick == 0L) {
         responseContentStartedTick = getTick();
      }
      super.onContent(response, content);
   }

   @Override
   public void onSuccess(Response response) {
      responseCompleteTick = getTick();
   }

   @Override
   public void onFailure(Response response, Throwable failure)   {
      responseCompleteTick = getTick();
   }

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
    * Called when the request is completed with success.
    * @param response The complete response.
    */
   abstract protected void completed(final org.attribyte.api.http.Response response);

   /**
    * Called when the request fails with an exception.
    * @param failure The failure.
    */
   abstract protected void failed(final Throwable failure);

   /**
    * Creates the accumulated timing information.
    * @return The timing.
    */
   private Timing timing() {
      return new Timing(
              requestSentTick - requestQueuedTick,
              requestCompleteTick - requestSentTick,
              responseStatusReceivedTick - requestSentTick,
              firstHeaderReceivedTick - requestSentTick,
              responseContentStartedTick - requestSentTick,
              responseCompleteTick - requestSentTick
      );
   }

   /**
    * The tick in nanoseconds when the request was queued for processing.
    */
   private long requestQueuedTick;

   /**
    * The tick in nanoseconds when the request was sent.
    */
   private long requestSentTick;

   /**
    * The tick in nanoseconds when the request was complete.
    */
   private long requestCompleteTick;

   /**
    * The tick in nanoseconds when the status line receive started.
    */
   private long responseStatusReceivedTick;

   /**
    * The tick in nanoseconds when the first header was received.
    */
   private long firstHeaderReceivedTick;

   /**
    * The tick in nanoseconds when the first chunk of content is received.
    */
   private long responseContentStartedTick = 0L;

   /**
    * The tick in nanoseconds when the response is complete.
    */
   private long responseCompleteTick;

   /**
    * Count the number of bytes read.
    */
   private int responseBytesRead = 0;

   /**
    * Gets the current tick in nanoseconds.
    * @return The tick.
    */
   private static long getTick() {
      return System.nanoTime();
   }
}
