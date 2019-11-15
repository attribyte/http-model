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

import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.api.Response;
import org.eclipse.jetty.client.api.Result;
import org.eclipse.jetty.http.HttpField;
import org.eclipse.jetty.util.Callback;

import java.io.PrintStream;
import java.nio.ByteBuffer;
import java.util.function.LongConsumer;

public class DebugListener implements Listener {

   public DebugListener() {
      this.out = System.out;
   }

   @Override
   public void onQueued(Request request) {
      println("Request Queued");
   }

   @Override
   public void onBegin(Request request) {
      println("Request Begin");
   }

   @Override
   public void onHeaders(Request request) {
      println("Request Headers");
   }

   @Override
   public void onContent(Request request, ByteBuffer content) {
      println(String.format("Request Content (%d)", content.remaining()));
   }

   @Override
   public void onCommit(Request request) {
      println("Request Commit");
   }

   @Override
   public void onSuccess(Request request) {
      println("Request Success");
   }

   @Override
   public void onFailure(Request request, Throwable failure) {
      println(String.format("Request Failure (%s)", failure.getMessage()));
   }

   @Override
   public void onContent(final Response response, final ByteBuffer byteBuffer, final Callback callback) {
      println(String.format("Response Content (%d)", byteBuffer.remaining()));
   }

   @Override
   public void onBegin(final Response response) {
      println("Response Begin");
   }

   @Override
   public void onComplete(final Result result) {
      println("Response Complete");
   }

   @Override
   public void onContent(final Response response, final ByteBuffer byteBuffer) {
      println(String.format("onContent - %d bytes", byteBuffer.remaining()));
   }

   @Override
   public boolean onHeader(final Response response, final HttpField httpField) {
      println(String.format("Response Header %s: %s", httpField.getName(), httpField.getValue()));
      return true;
   }

   @Override
   public void onFailure(final Response response, final Throwable throwable) {
      println(String.format("Response Failure: %s", throwable.getMessage()));
   }

   @Override
   public void onHeaders(final Response response) {
      println("Response Headers");
   }

   @Override
   public void onSuccess(final Response response) {
      println("Response Success");
   }

   private void println(final String str) {
      out.println(String.format(str + " %d", (System.currentTimeMillis() - startMillis)));
   }

   private final PrintStream out;
   private final long startMillis = System.currentTimeMillis();
}
