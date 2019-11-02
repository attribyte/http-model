package org.attribyte.api.http.impl.jetty;

import org.eclipse.jetty.client.api.Response;
import org.eclipse.jetty.client.api.Result;
import org.eclipse.jetty.util.Callback;

import java.nio.ByteBuffer;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class InputStreamResponseListener extends  BaseResponseListener {

   /**
    * Creates an instance with the given maximum length
    * @param maxLength the maximum length of the content
    * @param truncateOnLimit If {@code true}, content will be truncated if maximum length is reached without error.
    */
   public InputStreamResponseListener(final int maxLength,
                               final boolean truncateOnLimit) {
      super(maxLength, truncateOnLimit);
   }

   @Override
   public void onHeaders(Response response) {
      forwardToListener.onHeaders(response);
      super.onHeaders(response);
   }

   @Override
   public void onContent(Response response, ByteBuffer content, Callback callback) {
      synchronized(statsLock) {

         if(responseContentStartedTick == 0L) {
            responseContentStartedTick = getTick();
         }

         responseChunkCount++;

         if(content.hasRemaining()) {
            responseBodySize += content.remaining();
         }
      }
      forwardToListener.onContent(response, content, callback);
   }

   @Override
   public void onSuccess(Response response) {
      forwardToListener.onSuccess(response);
      super.onSuccess(response);
   }

   @Override
   public void onFailure(Response response, Throwable failure) {
      forwardToListener.onFailure(response, failure);
      super.onFailure(response, failure);
   }

   @Override
   public void onComplete(Result result) {
      forwardToListener.onComplete(result);
      super.onComplete(result);
   }

   private final org.eclipse.jetty.client.util.InputStreamResponseListener forwardToListener = new
           org.eclipse.jetty.client.util.InputStreamResponseListener();

   /**
    * Lock when writing stats.
    */
   private final Object statsLock = new Object();

}
