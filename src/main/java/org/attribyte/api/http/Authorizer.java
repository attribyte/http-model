/*
 * Copyright 2010, 2014 Attribyte, LLC
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

import org.attribyte.api.DatastoreException;

/**
 * Examines request headers, parameters or content
 * to determine if a request is authorized.
 */
public interface Authorizer {

   /**
    * Determine if a request is authorized.
    * @param auth The authentication scheme.
    * @param request The HTTP request.
    * @return The HTTP "Unauthorized" (or some other) response to be returned to the client if the request is not authorized. Otherwise, <code>null</code>.
    * @throws org.attribyte.api.DatastoreException on error retrieving security credentials.
    */
   public Response isAuthorized(AuthScheme auth, Request request) throws DatastoreException;
}

