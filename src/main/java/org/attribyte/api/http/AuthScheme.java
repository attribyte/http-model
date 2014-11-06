/*
 * Copyright 2010,2014 Attribyte, LLC
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

import com.google.common.base.Strings;

/**
 * Defines a HTTP authentication scheme.
 */
public abstract class AuthScheme {

   /**
    * Creates an authentication scheme.
    * <p>
    *    Must not contain the quote character.
    * </p>
    * @param scheme The scheme name.
    * @param realm The realm.
    * @throws java.lang.UnsupportedOperationException if the realm contains the quote character.
    */
   protected AuthScheme(final String scheme, final String realm) {
      if(realm.contains("\"")) {
         throw new UnsupportedOperationException("The 'realm' must not contain the quote character");
      }

      this.scheme = scheme.intern();
      this.realm = Strings.nullToEmpty(realm);
      this.authenticateResponseHeader = this.scheme + " realm=\"" + this.realm + "\"";
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
    * @return The request with authentication credentials added.
    */
   public abstract Request addAuth(Request request, String userId, String secret) throws java.security.GeneralSecurityException;

   /**
    * Gets the user id from the request, if possible.
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
   public Response getUnauthorizedResponse(final String message) {
      return new ResponseBuilder(Response.Code.UNAUTHORIZED, message == null ? DEFAULT_UNAUTHORIZED_MESSAGE : message)
              .addHeader(AUTHENTICATE_RESPONSE_HEADER, authenticateResponseHeader)
              .create();
   }

   /**
    * The default message sent when unauthorized ('Authorization Required').
    */
   public static final String DEFAULT_UNAUTHORIZED_MESSAGE = "Authorization Required";

   /**
    * The authenticate response header name ('WWW-Authenticate').
    */
   public static final String AUTHENTICATE_RESPONSE_HEADER = "WWW-Authenticate";

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

   /**
    * Gets the value for the <code>WWW-Authenticate</code> header.
    * @return The header value.
    */
   public String getAuthenticateResponseHeader() {
      return authenticateResponseHeader;
   }

   @Override
   public int hashCode() {
      return authenticateResponseHeader.hashCode();
   }

   @Override
   public boolean equals(final Object o) {
      if(o == this) {
         return true;
      } else if(o instanceof AuthScheme) {
         AuthScheme other = (AuthScheme)o;
         return authenticateResponseHeader.equals(other.authenticateResponseHeader);
      } else {
         return false;
      }
   }

   public final String scheme;
   public final String realm;
   public final String authenticateResponseHeader;
}

