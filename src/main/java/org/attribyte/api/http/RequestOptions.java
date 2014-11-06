/*
 * Copyright 2014 Attribyte, LLC
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
 * Request-specific options.
 */
public class RequestOptions {

   /**
    * The default follow redirects (true).
    */
   public static final boolean DEFAULT_FOLLOW_REDIRECTS = true;

   /**
    * The maximum response size (1 MB).
    */
   public static final int DEFAULT_MAX_RESPONSE_BYTES = 1024 * 1024;

   /**
    * The maximum time to wait for a response (5 seconds).
    */
   public static final int DEFAULT_TIMEOUT_SECONDS = 5;

   /**
    * The default request options.
    */
   public static final RequestOptions DEFAULT = new RequestOptions(
           DEFAULT_FOLLOW_REDIRECTS,
           DEFAULT_MAX_RESPONSE_BYTES, DEFAULT_TIMEOUT_SECONDS);

   /**
    * Creates request options.
    * @param followRedirects Should redirects be followed?
    * @param maxResponseBytes The maximum allowed response size in bytes.
    * @param timeoutSeconds The maximum time to wait for a response in seconds.
    */
   public RequestOptions(final boolean followRedirects,
                         final int maxResponseBytes,
                         final int timeoutSeconds) {
      this.followRedirects = followRedirects;
      this.maxResponseBytes = maxResponseBytes;
      this.timeoutSeconds = timeoutSeconds;
   }

   /**
    * Are redirects followed?
    */
   public final boolean followRedirects;

   /**
    * The maximum size of a response in bytes.
    */
   public final int maxResponseBytes;

   /**
    * The maximum time to wait for a response in seconds.
    */
   public final int timeoutSeconds;

}