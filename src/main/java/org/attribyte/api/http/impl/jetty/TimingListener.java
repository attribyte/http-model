package org.attribyte.api.http.impl.jetty;

import com.google.common.util.concurrent.SettableFuture;
import org.attribyte.api.http.ResponseBuilder;
import org.attribyte.api.http.Timing;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.api.Response;
import org.eclipse.jetty.client.api.Result;
import org.eclipse.jetty.client.util.BufferingResponseListener;
import org.eclipse.jetty.http.HttpField;
import org.eclipse.jetty.http.HttpFields;

import java.nio.ByteBuffer;

class TimingListener extends BufferingResponseListener implements Request.Listener {

   TimingListener(final SettableFuture<org.attribyte.api.http.Response> fut, final int maxResponseBytes) {
      super(maxResponseBytes);
      this.fut = fut;
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
         ResponseBuilder builder = new ResponseBuilder();
         Response response = result.getResponse();
         builder.setStatusCode(response.getStatus());
         HttpFields headers = response.getHeaders();
         for(HttpField header : headers) {
            builder.addHeader(header.getName(), header.getValue());
         }
         byte[] responseContent = getContent();
         if(responseContent != null) {
            builder.setBody(responseContent);
         }
         builder.setTiming(timing());
         fut.set(builder.create());
      } else {
         fut.setException(result.getFailure());
      }
   }

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
    * The future result.
    */
   private final SettableFuture<org.attribyte.api.http.Response> fut;

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
    * Gets the current tick in nanoseconds.
    * @return The tick.
    */
   private static long getTick() {
      return System.nanoTime();
   }
}
