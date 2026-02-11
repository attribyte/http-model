/*
 * Copyright 2026 Attribyte Labs, LLC
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

import com.google.common.base.MoreObjects;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Extended request/response statistics including header and body size metrics.
 */
public class Stats extends Timing {

   /**
    * Creates stats with all timing and size metrics.
    * @param timeToRequestStart Time to start the request (nanoseconds).
    * @param timeToRequestComplete Time to complete sending the request (nanoseconds).
    * @param timeToResponseStatus Time to receive the response status (nanoseconds).
    * @param timeToFirstResponseHeader Time to receive the first response header (nanoseconds).
    * @param timeToLastResponseHeader Time to receive the last response header (nanoseconds).
    * @param timeToFirstResponseContent Time to receive the first response content (nanoseconds).
    * @param timeToCompleteResponse Time to complete the response (nanoseconds).
    * @param requestHeaderCount The number of request headers.
    * @param requestHeaderSize The size of request headers in bytes.
    * @param requestChunkCount The number of request body chunks.
    * @param requestBodySize The size of the request body in bytes.
    * @param responseHeaderCount The number of response headers.
    * @param responseHeaderSize The size of response headers in bytes.
    * @param responseChunkCount The number of response body chunks.
    * @param responseBodySize The size of the response body in bytes.
    */
   public Stats(final long timeToRequestStart, final long timeToRequestComplete, final long timeToResponseStatus,
                final long timeToFirstResponseHeader, final long timeToLastResponseHeader,
                final long timeToFirstResponseContent, final long timeToCompleteResponse, final int requestHeaderCount,
                final long requestHeaderSize, final int requestChunkCount, final long requestBodySize, final int responseHeaderCount,
                final long responseHeaderSize, final AtomicInteger responseChunkCount, final AtomicLong responseBodySize) {
      super(timeToRequestStart, timeToRequestComplete, timeToResponseStatus, timeToFirstResponseHeader,
              timeToLastResponseHeader, timeToFirstResponseContent, timeToCompleteResponse);
      this.requestHeaderCount = requestHeaderCount;
      this.requestHeaderSize = requestHeaderSize;
      this.requestChunkCount = requestChunkCount;
      this.requestBodySize = requestBodySize;
      this.responseHeaderCount = responseHeaderCount;
      this.responseHeaderSize = responseHeaderSize;
      this.responseChunkCount = responseChunkCount;
      this.responseBodySize = responseBodySize;
   }

   @Override
   public String toString() {
      return MoreObjects.toStringHelper(this)
              .add("timeToRequestStart", timeToRequestStart(TimeUnit.MICROSECONDS))
              .add("timeToRequestComplete", timeToRequestComplete(TimeUnit.MICROSECONDS))
              .add("timeToResponseStatus", timeToResponseStatus(TimeUnit.MICROSECONDS))
              .add("timeToFirstResponseHeader", timeToFirstResponseHeader(TimeUnit.MICROSECONDS))
              .add("timeToLastResponseHeader", timeToLastResponseHeader(TimeUnit.MICROSECONDS))
              .add("timeToFirstResponseContent", timeToFirstResponseContent(TimeUnit.MICROSECONDS))
              .add("timeToCompleteResponse", timeToCompleteResponse(TimeUnit.MICROSECONDS))
              .add("requestHeaderCount", requestHeaderCount)
              .add("requestHeaderSize", requestHeaderSize)
              .add("requestChunkCount", requestChunkCount)
              .add("requestBodySize", requestBodySize)
              .add("responseHeaderCount", responseHeaderCount)
              .add("responseHeaderSize", responseHeaderSize)
              .add("responseChunkCount", responseChunkCount)
              .add("responseBodySize", responseBodySize)
              .add("units", "microsecond")
              .toString();
   }

   /**
    * The number of request headers.
    */
   public final int requestHeaderCount;

   /**
    * The size (in bytes) of the request headers.
    */
   public final long requestHeaderSize;

   /**
    * The number of chunks in the request body.
    */
   public final int requestChunkCount;

   /**
    * The size of the request body.
    */
   public final long requestBodySize;

   /**
    * The number of response headers.
    */
   public final int responseHeaderCount;

   /**
    * The size of the response headers.
    */
   public final long responseHeaderSize;

   /**
    * The number of chunks in the response.
    */
   public final AtomicInteger responseChunkCount;

   /**
    * The size of the response body.
    */
   public final AtomicLong responseBodySize;
}
