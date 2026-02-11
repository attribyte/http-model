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

import org.attribyte.api.InitializationException;
import org.attribyte.api.Logger;

import java.io.IOException;
import java.util.Properties;

/**
 * Defines the HTTP client interface.
 * <p>
 * Implementation must support concurrent use.
 * </p>
 */
public interface Client {

   /**
    * Initializes the client. Must be called before first use.
    * @param prefix A prefix applied to all property names.
    * @param props A properties file.
    * @param logger A logger for messages.
    * @throws InitializationException on initialization error.
    */
   public void init(String prefix, Properties props, Logger logger) throws InitializationException;

   /**
    * Sends a request with default options.
    * @param request The request.
    * @return The response.
    * @throws java.io.IOException on I/O error.
    */
   public Response send(Request request) throws IOException;

   /**
    * Sends a request with specified options.
    * @param request The request.
    * @param options The request options.
    * @return The response.
    * @throws java.io.IOException on I/O error.
    */
   public Response send(Request request, RequestOptions options) throws IOException;

   /**
    * Releases resources, interrupts threads, etc. Called when application using the client is terminated.
    * @throws java.lang.Exception on shutdown exception.
    */
   public void shutdown() throws Exception;

}

