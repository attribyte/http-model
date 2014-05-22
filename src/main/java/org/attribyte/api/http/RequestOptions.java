package org.attribyte.api.http;

/**
 * Encapsulates all request options.
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

   public static final RequestOptions DEFAULT = new RequestOptions(
           DEFAULT_FOLLOW_REDIRECTS,
           DEFAULT_MAX_RESPONSE_BYTES, 5);


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
