/*
 * Copyright 2016 Attribyte, LLC
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

/**
 * Transaction timing.
 */
public class Timing {

   /**
    * Creates timing.
    *
    * @param timeToRequestStart The time (nanos) the request is waiting to be processed.
    * @param timeToRequestComplete The time (nanos) for the request to complete after it is started.
    * @param timeToResponseStatus The time (nanos) before the response status line is received.
    * @param timeToFirstResponseHeader The time (nanos) before the first response header is received.
    * @param timeToLastResponseHeader The time (nanos) before the last response header is received.
    * @param timeToFirstResponseContent The time (nanos) before the first bye of response content is received.
    * @param timeToCompleteResponse The time (nanos) for the response to be completed, with all bytes read.
    */
   public Timing(final long timeToRequestStart, final long timeToRequestComplete,
                 final long timeToResponseStatus,
                 final long timeToFirstResponseHeader, final long timeToLastResponseHeader,
                 final long timeToFirstResponseContent, final long timeToCompleteResponse) {
      this.timeToRequestStart = timeToRequestStart;
      this.timeToRequestComplete = timeToRequestComplete;
      this.timeToResponseStatus = timeToResponseStatus;
      this.timeToFirstResponseHeader = timeToFirstResponseHeader;
      this.timeToLastResponseHeader = timeToLastResponseHeader;
      this.timeToFirstResponseContent = timeToFirstResponseContent > 0 ? timeToFirstResponseContent : 0;
      this.timeToCompleteResponse = timeToCompleteResponse > 0 ? timeToCompleteResponse : 0;
   }

   /**
    * Gets the time to request start.
    * @param timeUnit The desired time unit.
    * @return The time in the requested units.
    */
   public long timeToRequestStart(final TimeUnit timeUnit) {
      return timeUnit.convert(timeToRequestStart, TimeUnit.NANOSECONDS);
   }

   /**
    * Gets the time to request complete.
    * @param timeUnit The desired time unit.
    * @return The time in the requested units.
    */
   public long timeToRequestComplete(final TimeUnit timeUnit) {
      return timeUnit.convert(timeToRequestComplete, TimeUnit.NANOSECONDS);
   }

   /**
    * Gets the time to response status.
    * @param timeUnit The desired time unit.
    * @return The time in the requested units.
    */
   public long timeToResponseStatus(final TimeUnit timeUnit) {
      return timeUnit.convert(timeToResponseStatus, TimeUnit.NANOSECONDS);
   }

   /**
    * Gets the time to the first response header.
    * @param timeUnit The desired time unit.
    * @return The time in the requested units.
    */
   public long timeToFirstResponseHeader(final TimeUnit timeUnit) {
      return timeUnit.convert(timeToFirstResponseHeader, TimeUnit.NANOSECONDS);
   }

   /**
    * Gets the time to the last response header.
    * @param timeUnit The desired time unit.
    * @return The time in the requested units.
    */
   public long timeToLastResponseHeader(final TimeUnit timeUnit) {
      return timeUnit.convert(timeToLastResponseHeader, TimeUnit.NANOSECONDS);
   }

   /**
    * Gets the time to the first response content.
    * @param timeUnit The desired time unit.
    * @return The time in the requested units.
    */
   public long timeToFirstResponseContent(final TimeUnit timeUnit) {
      return timeUnit.convert(timeToFirstResponseContent, TimeUnit.NANOSECONDS);
   }

   /**
    * Gets the time to response complete.
    * @param timeUnit The desired time unit.
    * @return The time in the requested units.
    */
   public long timeToCompleteResponse(final TimeUnit timeUnit) {
      return timeUnit.convert(timeToCompleteResponse, TimeUnit.NANOSECONDS);
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
              .add("units", "microsecond")
              .toString();
   }

   protected final long timeToRequestStart;
   protected final long timeToRequestComplete;
   protected final long timeToResponseStatus;
   protected final long timeToFirstResponseHeader;
   protected final long timeToLastResponseHeader;
   protected final long timeToFirstResponseContent;
   protected final long timeToCompleteResponse;
}
