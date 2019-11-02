package org.attribyte.api.http.impl.jetty;

import com.google.common.base.Strings;
import org.attribyte.api.http.Timing;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.api.Response;
import org.eclipse.jetty.client.api.Result;
import org.eclipse.jetty.http.HttpField;
import org.eclipse.jetty.http.HttpFields;
import org.eclipse.jetty.util.Callback;

import java.nio.ByteBuffer;
import java.util.function.LongConsumer;

public class StatsListener implements Request.Listener, Response.Listener {

   /* Request.Listener */

   @Override
   public void onQueued(Request request) {
      requestQueuedTick = getTick();
   }

   @Override
   public void onBegin(Request request) {
      //Do nothing...
   }

   @Override
   public void onHeaders(Request request) {
      requestHeaderCount = request.getHeaders().size();
      requestHeaderSize = size(request.getHeaders());
   }

   @Override
   public void onCommit(Request request) {
      requestSentTick = getTick();
   }

   @Override
   public void onContent(Request request, ByteBuffer content) {
      requestChunkCount++;
      if(content.hasRemaining()) {
         requestBodySize += content.remaining();
      }
   }

   @Override
   public void onSuccess(Request request) {
      requestCompleteTick = getTick();
   }

   @Override
   public void onFailure(Request request, Throwable failure) {
      requestCompleteTick = getTick();
   }

   /* Response.Listener */

   @Override
   public void onBegin(Response response) {
      responseStatusReceivedTick = getTick();
   }

   @Override
   public boolean onHeader(Response response, HttpField field) {
      if(firstHeaderReceivedTick == 0L) {
         firstHeaderReceivedTick = getTick();
      }
      return true;
   }

   @Override
   public void onHeaders(Response response) {
      lastHeaderReceivedTick = getTick();
      responseHeaderCount = response.getHeaders().size();
      responseHeaderSize = size(response.getHeaders());
   }

   @Override
   public void onContent(Response response, ByteBuffer content) {
      responseChunkCount++;
      if(responseContentStartedTick == 0L) {
         responseContentStartedTick = getTick();
      }
   }

   @Override
   public void onContent(Response response, ByteBuffer content, Callback callback) {
      try {
         onContent(response, content);
         callback.succeeded();
      } catch(Throwable x) {
         callback.failed(x);
      }
   }

   @Override
   public void onContent(Response response, LongConsumer demand, ByteBuffer content, Callback callback) {
      onContent(response, content, Callback.from(() -> {
         callback.succeeded();
         demand.accept(1);
      }, callback::failed));
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
   private long firstHeaderReceivedTick = 0L;

   /**
    * The tick in nanoseconds when all the headers were received.
    */
   private long lastHeaderReceivedTick = 0L;

   /**
    * The tick in nanoseconds when the first chunk of content is received.
    */
   protected long responseContentStartedTick = 0L;

   /**
    * The tick in nanoseconds when the response is complete.
    */
   private long responseCompleteTick;

   /**
    * The number of request headers.
    */
   private int requestHeaderCount = 0;

   /**
    * The size (in bytes) of the request headers.
    */
   private long requestHeaderSize = 0;

   /**
    * The number of chunks in the request body.
    */
   private int requestChunkCount = 0;

   /**
    * The size of the request body.
    */
   private long requestBodySize = 0L;

   /**
    * The number of response headers.
    */
   private int responseHeaderCount = 0;

   /**
    * The size of the response headers.
    */
   private long responseHeaderSize = 0L;

   /**
    * The number of chunks in the response.
    */
   protected int responseChunkCount = 0;

   /**
    * The size of the response body.
    */
   protected long responseBodySize = 0L;

   /**
    * Creates the accumulated timing information.
    * @return The timing.
    */
   protected Timing timing() {
      return new Timing(
              requestSentTick - requestQueuedTick,
              requestCompleteTick - requestSentTick,
              responseStatusReceivedTick - requestSentTick,
              firstHeaderReceivedTick - requestSentTick,
              lastHeaderReceivedTick - requestSentTick,
              responseContentStartedTick - requestSentTick,
              responseCompleteTick - requestSentTick
      );
   }

   /**
    * Gets the current tick in nanoseconds.
    * @return The tick.
    */
   protected static long getTick() {
      return System.nanoTime();
   }

   /**
    * Gets the size in bytes of the headers.
    * <p>
    *    We assume that both names and values are ASCII, which is almost certainly the case
    *    in normal circumstances.
    * </p>
    * @param fields The fields.
    * @return The size.
    */
   private static int size(final HttpFields fields) {
      int size = 0;
      for(HttpField field : fields) {
         size += field.getName().length() + 2; // ': '
         size += Strings.nullToEmpty(field.getValue()).length();
      }
      return size;
   }
}
