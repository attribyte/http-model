/*
 * Copyright 2010 Attribyte, LLC 
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

import org.attribyte.util.StringUtil;

/**
 * Defines a HTTP authentication scheme.
 */
public abstract class AuthScheme {

   /**
    * Creates an authentication scheme.
    * @param scheme The scheme name.
    * @param realm The realm.
    */
   protected AuthScheme(final String scheme, final String realm) {
      this.scheme = scheme.intern();
      this.realm = realm;
      int h = 17;
      h = 31 * h + scheme.hashCode();
      if(realm != null) {
         h = 31 * h + realm.hashCode();
      }
      this.hashCode = h;
   }

   /**
    * Determines if the request has any (invalid or valid) credentials for this scheme.
    * @param request The request.
    * @return Does the request appear to have appropriate credentials?
    */
   public abstract boolean hasCredentials(Request request);

   /**
    * Adds authentication credentials to a request.
    * @param request The request to which credentials are added.
    * @param userId An id that uniquely identifies the user (e.g. 'username').
    * @param secret The authentication secret for the username.
    */
   public abstract Request addAuth(Request request, String userId, String secret) throws java.security.GeneralSecurityException;

   /**
    * Gets the user id from the request.
    * @param request The request.
    * @return The user id, or <code>null</code> if none.
    */
   public abstract String getUserId(Request request) throws java.security.GeneralSecurityException;

   /**
    * Authenticates the request.
    * @param request The request.
    * @param userId The user id for the secret.
    * @param secret The secret data.
    * @return The HTTP "Unauthorized" response if request is not authorized, otherwise <code>null</code>.
    */
   public abstract Response authenticate(Request request, String userId, String secret) throws java.security.GeneralSecurityException;

   /**
    * Creates a challenge response.
    * <p>
    * By default, returns the standard HTTP challenge response.
    * </p>
    * @param message A message returned with the response. If <code>null</code>, a standard message is returned.
    * @return The response.
    */
   public Response getUnauthorizedResponse(String message) {
      StringBuilder buf = new StringBuilder();
      buf.append(scheme);
      if(realm != null) {
         buf.append(" realm=").append(realm);
      }

      Response response = new ResponseBuilder(Response.Code.UNAUTHORIZED, message == null ? "Authorization Required" : message).create();
      response.setHeader("WWW-Authenticate", buf.toString());
      return response;
   }

   /**
    * Gets the scheme.
    * @return The scheme.
    */
   public String getScheme() {
      return scheme;
   }

   /**
    * Gets the realm.
    * @return The realm.
    */
   public String getRealm() {
      return realm;
   }

   @Override
   public int hashCode() {
      return hashCode;
   }

   @Override
   public boolean equals(Object o) {
      if(o == this) {
         return true;
      } else if(o instanceof AuthScheme) {
         AuthScheme other = (AuthScheme)o;
         return StringUtil.equals(scheme, other.scheme) && StringUtil.equals(realm, other.realm);
      } else {
         return false;
      }
   }

   private final String scheme;
   private final String realm;
   private final int hashCode;
}

