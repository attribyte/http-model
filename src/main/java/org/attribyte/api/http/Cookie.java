package org.attribyte.api.http;

import com.google.common.base.MoreObjects;

/**
 * An immutable HTTP cookie.
 */
public class Cookie {

   /**
    * Creates a request cookie.
    * @param name The cookie name.
    * @param value The cookie value.
    * @return The cookie.
    */
   public static Cookie requestCookie(final String name, final String value) {
      return new Cookie(name, value);
   }

   Cookie(final String name, final String value) {
      this(name, value, "", "", -1, false, false);
   }


   /**
    * Creates a response cookie.
    * @param name The name.
    * @param value The value.
    * @param domain The domain.
    * @param path The path.
    * @param maxAgeSeconds The maximum age in seconds.
    * @param secure Is the cookie sent only if connection is secure?
    * @param httpOnly Is the cookie available only through HTTP?
    */
   public Cookie(final String name, final String value, final String domain,
                 final String path, final int maxAgeSeconds,
                 final boolean secure, final boolean httpOnly) {
      this.name = name;
      this.value = value;
      this.domain = domain;
      this.path = path;
      this.maxAgeSeconds = maxAgeSeconds;
      this.secure = secure;
      this.httpOnly = httpOnly;
   }

   @Override
   public String toString() {
      return MoreObjects.toStringHelper(this)
              .add("name", name)
              .add("value", value)
              .add("domain", domain)
              .add("path", path)
              .add("maxAgeSeconds", maxAgeSeconds)
              .add("secure", secure)
              .add("httpOnly", httpOnly)
              .toString();
   }

   /**
    * The name.
    */
   public final String name;

   /**
    * The value.
    */
   public final String value;

   /**
    * The domain.
    */
   public final String domain;

   /**
    * The path.
    */
   public final String path;

   /**
    * The maximum age in seconds.
    */
   public final int maxAgeSeconds;

   /**
    * Is this cookie sent only if connection is secure?
    */
   public final boolean secure;

   /**
    * Is this an http-only cookie?
    */
   public final boolean httpOnly;
}
