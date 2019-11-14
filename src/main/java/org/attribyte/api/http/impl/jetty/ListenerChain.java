/*
 * Copyright 2019 Attribyte, LLC
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

import com.google.common.collect.ImmutableList;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.api.Response;
import org.eclipse.jetty.client.api.Result;
import org.eclipse.jetty.http.HttpField;
import org.eclipse.jetty.util.Callback;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.function.LongConsumer;

/**
 * A chain of listeners.
 */
public class ListenerChain implements Listener {

   /**
    * Creates a response listener chain.
    * @param requestListeners The request listeners.
    * @param responseListeners The response listeners.
    */
   public ListenerChain(final List<Request.Listener> requestListeners, final List<Response.Listener> responseListeners) {
      this.requestListeners = requestListeners != null ? ImmutableList.copyOf(requestListeners) : ImmutableList.of();
      this.responseListeners = responseListeners != null ? ImmutableList.copyOf(responseListeners) : ImmutableList.of();
   }

   @Override
   public void onQueued(Request request) {
      requestListeners.forEach(l -> l.onQueued(request));
   }

   @Override
   public void onBegin(Request request) {
      requestListeners.forEach(l -> l.onBegin(request));
   }

   @Override
   public void onHeaders(Request request) {
      requestListeners.forEach(l -> l.onHeaders(request));
   }

   @Override
   public void onCommit(Request request) {
      requestListeners.forEach(l -> l.onCommit(request));
   }

   @Override
   public void onContent(Request request, ByteBuffer content) {
      requestListeners.forEach(l -> l.onContent(request, content));
   }

   @Override
   public void onSuccess(Request request) {
      requestListeners.forEach(l -> l.onSuccess(request));
   }

   @Override
   public void onFailure(Request request, Throwable failure) {
      requestListeners.forEach(l -> l.onFailure(request, failure));
   }

   @Override
   public void onContent(final Response response, final ByteBuffer byteBuffer, final Callback callback) {
      responseListeners.forEach(l -> l.onContent(response, byteBuffer, callback));
   }

   @Override
   public void onBegin(final Response response) {
      responseListeners.forEach(l -> l.onBegin(response));
   }

   @Override
   public void onComplete(final Result result) {
      responseListeners.forEach(l -> l.onComplete(result));
   }

   @Override
   public void onContent(final Response response, final ByteBuffer byteBuffer) {
      responseListeners.forEach(l -> l.onContent(response, byteBuffer));
   }

   @Override
   public void onContent(final Response response, final LongConsumer longConsumer, final ByteBuffer byteBuffer, final Callback callback) {
      responseListeners.forEach(l -> l.onContent(response, longConsumer, byteBuffer, callback));
   }

   @Override
   public boolean onHeader(final Response response, final HttpField httpField) {
      boolean res = true;
      for(final Response.Listener listener : responseListeners) {
         if(!listener.onHeader(response, httpField)) {
            res = false;
         }
      }
      return res;
   }

   @Override
   public void onFailure(final Response response, final Throwable throwable) {
      responseListeners.forEach(l -> l.onFailure(response, throwable));
   }

   @Override
   public void onHeaders(final Response response) {
      responseListeners.forEach(l -> l.onHeaders(response));
   }

   @Override
   public void onSuccess(final Response response) {
      responseListeners.forEach(l -> l.onSuccess(response));
   }

   /**
    * The sequence of request listeners.
    */
   private final ImmutableList<Request.Listener> requestListeners;

   /**
    * The sequence of resposne listeners.
    */
   private final ImmutableList<Response.Listener> responseListeners;
}
