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

package org.attribyte.api.http.impl;

import com.google.common.base.Charsets;
import com.google.common.hash.HashCode;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import org.apache.commons.codec.binary.Base64;
import org.attribyte.api.http.AuthScheme;
import org.attribyte.api.http.Header;
import org.attribyte.api.http.Request;
import org.attribyte.api.http.Response;
import org.attribyte.util.StringUtil;

import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;

/**
 * The Standard (insecure) "Basic" scheme.
 */
public class BasicAuthScheme extends AuthScheme {

   private final Response INVALID_FORMAT_RESPONSE = getUnauthorizedResponse("Invalid format");

   private final Response INVALID_SCHEME_RESPONSE = getUnauthorizedResponse("Invalid scheme");

   private final Response UNAUTHORIZED_RESPONSE = getUnauthorizedResponse(null);

   public static final String AUTH_HEADER = "Authorization";

   private static final HashFunction HASH_FUNCTION = Hashing.sha1();

   /**
    * Create a scheme with no realm.
    */
   public BasicAuthScheme() {
      super("Basic", "");
   }

   /**
    * Create a scheme with a realm.
    * @param realm The realm.
    */
   public BasicAuthScheme(final String realm) {
      super("Basic", realm == null ? "" : realm);
   }

   @Override
   public Request addAuth(final Request request, final String id, final String secret) throws GeneralSecurityException {
      return request.addHeaders(buildAuthHeaders(request, id, secret));
   }

   @Override
   public boolean hasCredentials(Request request) {
      String auth = request.getHeaderValue(AUTH_HEADER);
      if(auth == null || !auth.toLowerCase().startsWith("basic ")) {
         return false;
      } else {
         return true;
      }
   }

   @Override
   public String getUserId(final Request request) throws GeneralSecurityException {
      String authorization = request.getHeaderValue(AUTH_HEADER);
      if(!StringUtil.hasContent(authorization)) {
         return null;
      }

      if(!authorization.toLowerCase().startsWith("basic ")) {
         return null;
      }

      authorization = authorization.substring(6).trim();

      String upass = new String(Base64.decodeBase64(authorization), Charsets.US_ASCII).trim();
      int index = upass.indexOf(':');
      if(index < 1) {
         return null;
      } else {
         return upass.substring(0, index);
      }
   }

   @Override
   public Response authenticate(Request request, String userId, String secret) throws GeneralSecurityException {

      String authorization = request.getHeaderValue(AUTH_HEADER);
      if(!StringUtil.hasContent(authorization)) {
         return UNAUTHORIZED_RESPONSE;
      }

      if(!authorization.toLowerCase().startsWith("basic ")) {
         return INVALID_SCHEME_RESPONSE;
      }

      authorization = authorization.substring(6).trim();

      String upass = new String(Base64.decodeBase64(authorization), Charsets.US_ASCII).trim();

      int index = upass.indexOf(':');
      if(index < 1) {
         return INVALID_FORMAT_RESPONSE;
      } else {
         //We're going to hash both values and compare the hashes to
         //thwart timing attacks.
         HashCode hash0 = HASH_FUNCTION.hashString(upass, Charsets.UTF_8);
         HashCode hash1 = HASH_FUNCTION.newHasher()
                 .putString(userId, Charsets.UTF_8)
                 .putString(":", Charsets.UTF_8)
                 .putString(secret, Charsets.UTF_8).hash();
         return hash0.equals(hash1) ? null : UNAUTHORIZED_RESPONSE;
      }
   }

   /**
    * Builds an auth header.
    * @param username The username.
    * @param password The password.
    * @return The auth header value.
    */
   public static final String buildAuthHeaderValue(final String username, final String password) {
      StringBuilder buf = new StringBuilder(username.trim());
      buf.append(":");
      buf.append(password.trim());
      String up = buf.toString();
      buf.setLength(0);

      byte[] bytes = Base64.encodeBase64(up.getBytes());
      buf.append("Basic ");
      buf.append(new String(bytes, Charsets.UTF_8));
      return buf.toString();
   }

   /**
    * Create the base 64 encoding 'Authorization' header.
    * @param request The original request.
    * @param username The username.
    * @param password The password.
    */
   private static List<Header> buildAuthHeaders(Request request, String username, String password) {
      Header header = new Header(AUTH_HEADER, buildAuthHeaderValue(username, password));
      return Collections.singletonList(header);
   }
}

