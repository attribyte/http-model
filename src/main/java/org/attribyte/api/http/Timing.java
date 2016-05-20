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
    * @param timeToFirstResponseContent The time (nanos) before the first bye of response content is received.
    * @param timeToCompleteResponse The time (nanos) for the response to be completed, with all bytes read.
    */
   public Timing(final long timeToRequestStart, final long timeToRequestComplete,
                 final long timeToResponseStatus, final long timeToFirstResponseHeader,
                 final long timeToFirstResponseContent, final long timeToCompleteResponse) {
      this.timeToRequestStart = timeToRequestStart;
      this.timeToRequestComplete = timeToRequestComplete;
      this.timeToResponseStatus = timeToResponseStatus;
      this.timeToFirstResponseHeader = timeToFirstResponseHeader;
      this.timeToFirstResponseContent = timeToFirstResponseContent;
      this.timeToCompleteResponse = timeToCompleteResponse;
   }

   private final long timeToRequestStart;
   private final long timeToRequestComplete;
   private final long timeToResponseStatus;
   private final long timeToFirstResponseHeader;
   private final long timeToFirstResponseContent;
   private final long timeToCompleteResponse;

}
