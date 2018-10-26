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

import org.attribyte.api.InitializationException;
import org.attribyte.util.InitUtil;

import java.util.Properties;

/**
 * Configuration options for clients.
 */
public class ClientOptions {

   /**
    * Indicates that implementation defaults should be used.
    */
   public static final ClientOptions IMPLEMENTATION_DEFAULT = new ClientOptions();

   /**
    * The user agent ('Attribyte/1.0').
    */
   public final String userAgent;

   /**
    * The maximum amount of time to wait to establish a connection (5s).
    */
   public final int connectionTimeoutMillis;

   /**
    * The maximum amount of time to wait for a response after sending a request (5s).
    */
   public final int requestTimeoutMillis;

   /**
    * The maximum amount of time to wait for a (blocking) socket read to return (5s).
    */
   public final int socketTimeoutMillis;

   /**
    * A proxy server.
    */
   public final String proxyHost;

   /**
    * The proxy server port.
    */
   public final int proxyPort;

   /**
    * Should redirects be followed (true).
    */
   public final boolean followRedirects;

   /**
    * The maximum number of connections per destination (1024).
    */
   public final int maxConnectionsPerDestination;

   /**
    * The maximum number of connections for all destinations (4096).
    */
   public final int maxConnectionsTotal;

   /**
    * The request buffer size (4096).
    */
   public final int requestBufferSize;

   /**
    * The response buffer size (16384).
    */
   public final int responseBufferSize;

   /**
    * Should SSL certificates be trusted even if invalid ({code false}).
    */
   public final boolean trustAllCertificates;

   /**
    * Arbitrary, implementation-specific properties.
    */
   private final Properties props;

   /**
    * Gets a property.
    * @param name The property name.
    * @param defaultValue The default value.
    * @return The property or default value.
    */
   public String getProperty(final String name, final String defaultValue) {
      String val = props.getProperty(name);
      return val != null ? val : defaultValue;
   }

   /**
    * Gets an integer property.
    * @param name The property name.
    * @param defaultValue The default value.
    * @return The property or default value.
    */
   public int getIntProperty(final String name, final int defaultValue) {
      String val = props.getProperty(name);
      if(val != null) {
         try {
            return Integer.parseInt(val);
         } catch(NumberFormatException ne) {
            return defaultValue;
         }

      } else {
         return defaultValue;
      }
   }

   /**
    * Gets a boolean property.
    * @param name The property name.
    * @param defaultValue The default value.
    * @return The property or default value.
    */
   public boolean getBooleanProperty(final String name, final boolean defaultValue) {
      String val = props.getProperty(name);
      if(val != null) {
         return val.equalsIgnoreCase("true");
      } else {
         return defaultValue;
      }
   }

   /**
    * Gets a time (string) property as milliseconds.
    * @param name The property name.
    * @param defaultValue The default value.
    * @return The property or default value.
    */
   public int getTimeProperty(final String name, final int defaultValue) {
      String val = props.getProperty(name);
      if(val != null) {
         long millis = InitUtil.millisFromTime(val);
         return millis != Long.MIN_VALUE ? (int)millis : 0;
      } else {
         return defaultValue;
      }
   }

   static final int KEY = 0;
   static final int DEFAULT_VALUE = 1;
   static final String[] USER_AGENT = new String[]{"userAgent", "AttribyteHttp/1.0"};
   static final String[] CONNECTION_TIMEOUT = new String[]{"connectionTimeout", "5s"};
   static final String[] REQUEST_TIMEOUT = new String[]{"requestTimeout", "5s"};
   static final String[] SOCKET_TIMEOUT = new String[]{"socketTimeout", "5s"};
   static final String[] PROXY_HOST = new String[]{"proxyHost", null};
   static final String[] PROXY_PORT = new String[]{"proxyPort", "0"};
   static final String[] FOLLOW_REDIRECTS = new String[]{"followRedirects", "true"};
   static final String[] MAX_CONNECTIONS_PER_DESTINATION = new String[]{"maxConnectionsPerDestination", "1024"};
   static final String[] MAX_CONNECTIONS_TOTAL = new String[]{"maxConnectionsTotal", "4096"};
   static final String[] REQUEST_BUFFER_SIZE = new String[]{"requestBufferSize", "4096"};
   static final String[] RESPONSE_BUFFER_SIZE = new String[]{"responseBufferSize", "16384"};
   static final String[] TRUST_ALL_CERTIFICATES = new String[] {"trustAllCertificates", "false"};

   /**
    * Creates options from properties.
    * @param prefix A prefix applied to all property names.
    * @param props The properties.
    * @throws InitializationException on invalid property.
    */
   public ClientOptions(final String prefix, final Properties props) throws InitializationException {
      InitUtil init = new InitUtil(prefix, props, false);
      this.userAgent = init.getProperty(USER_AGENT[KEY], USER_AGENT[DEFAULT_VALUE]);
      this.connectionTimeoutMillis = (int)InitUtil.millisFromTime(init.getProperty(CONNECTION_TIMEOUT[KEY], CONNECTION_TIMEOUT[DEFAULT_VALUE]));
      this.requestTimeoutMillis = (int)InitUtil.millisFromTime(init.getProperty(REQUEST_TIMEOUT[KEY], REQUEST_TIMEOUT[DEFAULT_VALUE]));
      this.socketTimeoutMillis = (int)InitUtil.millisFromTime(init.getProperty(SOCKET_TIMEOUT[KEY], SOCKET_TIMEOUT[DEFAULT_VALUE]));
      this.proxyHost = init.getProperty(PROXY_HOST[KEY], PROXY_HOST[DEFAULT_VALUE]);
      this.proxyPort = init.getIntProperty(PROXY_PORT[KEY], Integer.parseInt(PROXY_PORT[DEFAULT_VALUE]));
      this.followRedirects = init.getProperty(FOLLOW_REDIRECTS[KEY], FOLLOW_REDIRECTS[DEFAULT_VALUE]).equalsIgnoreCase("true");
      this.maxConnectionsPerDestination = init.getIntProperty(MAX_CONNECTIONS_PER_DESTINATION[KEY], Integer.parseInt(MAX_CONNECTIONS_PER_DESTINATION[DEFAULT_VALUE]));
      this.maxConnectionsTotal = init.getIntProperty(MAX_CONNECTIONS_TOTAL[KEY], Integer.parseInt(MAX_CONNECTIONS_TOTAL[DEFAULT_VALUE]));
      this.requestBufferSize = init.getIntProperty(REQUEST_BUFFER_SIZE[KEY], Integer.parseInt(REQUEST_BUFFER_SIZE[DEFAULT_VALUE]));
      this.responseBufferSize = init.getIntProperty(RESPONSE_BUFFER_SIZE[KEY], Integer.parseInt(RESPONSE_BUFFER_SIZE[DEFAULT_VALUE]));
      this.trustAllCertificates = init.getProperty(TRUST_ALL_CERTIFICATES[KEY], TRUST_ALL_CERTIFICATES[DEFAULT_VALUE]).equalsIgnoreCase("true");
      this.props = init.getProperties();

   }

   ClientOptions(final String userAgent, final int connectionTimeoutMillis,
                 final int requestTimeoutMillis,
                 final int socketTimeoutMillis, final String proxyHost, final int proxyPort,
                 final boolean followRedirects,
                 final int maxConnectionsPerDestination,
                 final int maxConnectionsTotal,
                 final int requestBufferSize, final int responseBufferSize,
                 final boolean trustAllCertificates,
                 final Properties props) {
      this.userAgent = userAgent;
      this.connectionTimeoutMillis = connectionTimeoutMillis;
      this.requestTimeoutMillis = requestTimeoutMillis;
      this.socketTimeoutMillis = socketTimeoutMillis;
      this.proxyHost = proxyHost;
      this.proxyPort = proxyPort;
      this.followRedirects = followRedirects;
      this.maxConnectionsPerDestination = maxConnectionsPerDestination;
      this.maxConnectionsTotal = maxConnectionsTotal;
      this.requestBufferSize = requestBufferSize;
      this.responseBufferSize = responseBufferSize;
      this.trustAllCertificates = trustAllCertificates;
      this.props = props;
   }

   private ClientOptions() {
      this.userAgent = null;
      this.connectionTimeoutMillis = 0;
      this.requestTimeoutMillis = 0;
      this.socketTimeoutMillis = 0;
      this.proxyHost = null;
      this.proxyPort = 0;
      this.followRedirects = false;
      this.maxConnectionsPerDestination = 0;
      this.maxConnectionsTotal = 0;
      this.requestBufferSize = 0;
      this.responseBufferSize = 0;
      this.trustAllCertificates = false;
      this.props = null;
   }

   /**
    * Builds immutable instances of client options.
    */
   public static class Builder {

      /**
       * Gets the user agent.
       * @return The user agent.
       */
      public String getUserAgent() {
         return userAgent;
      }

      /**
       * Sets the user agent.
       * @param userAgent The user agent.
       * @return A self-reference.
       */
      public Builder setUserAgent(final String userAgent) {
         this.userAgent = userAgent;
         return this;
      }

      /**
       * Gets the connection timeout in milliseconds.
       * @return The timeout in milliseconds.
       */
      public int getConnectionTimeoutMillis() {
         return connectionTimeoutMillis;
      }

      /**
       * Sets the connection timeout in milliseconds.
       * @param connectionTimeoutMillis The connection timeout in milliseconds.
       * @return A self-reference.
       */
      public Builder setConnectionTimeoutMillis(final int connectionTimeoutMillis) {
         this.connectionTimeoutMillis = connectionTimeoutMillis;
         return this;
      }

      /**
       * Gets the request timeout in milliseconds.
       * @return The request timeout in milliseconds.
       */
      public int getRequestTimeoutMillis() {
         return requestTimeoutMillis;
      }

      /**
       * Sets the request timeout in milliseconds.
       * @param requestTimeoutMillis The request timeout in milliseconds.
       * @return A self-reference.
       */
      public Builder setRequestTimeoutMillis(final int requestTimeoutMillis) {
         this.requestTimeoutMillis = requestTimeoutMillis;
         return this;
      }

      /**
       * Gets the socket timeout in milliseconds.
       * @return The socket timeout in milliseconds.
       */
      public int getSocketTimeoutMillis() {
         return socketTimeoutMillis;
      }

      /**
       * Sets the socket timeout in milliseconds.
       * @param socketTimeoutMillis  The socket timeout in milliseconds.
       * @return A self-reference.
       */
      public Builder setSocketTimeoutMillis(final int socketTimeoutMillis) {
         this.socketTimeoutMillis = socketTimeoutMillis;
         return this;
      }

      /**
       * Gets the proxy host.
       * @return The proxy host.
       */
      public String getProxyHost() {
         return proxyHost;
      }

      /**
       * Sets the proxy host.
       * @param proxyHost The proxy host.
       * @return A self-reference.
       */
      public Builder setProxyHost(final String proxyHost) {
         this.proxyHost = proxyHost;
         return this;
      }

      /**
       * Gets the proxy port.
       * @return The proxy port.
       */
      public int getProxyPort() {
         return proxyPort;
      }

      /**
       * Sets the proxy port.
       * @param proxyPort The proxy port.
       * @return A self-reference.
       */
      public Builder setProxyPort(final int proxyPort) {
         this.proxyPort = proxyPort;
         return this;
      }

      /**
       * Gets the follow redirects setting.
       * @return Are redirects followed?
       */
      public boolean getFollowRedirects() {
         return followRedirects;
      }

      /**
       * Sets if redirects are followed.
       * @param followRedirects Are redirects followed?
       * @return A self-reference.
       */
      public Builder setFollowRedirects(final boolean followRedirects) {
         this.followRedirects = followRedirects;
         return this;
      }

      /**
       * Gets the maximum number of connections per destination.
       * @return The number of connections.
       */
      public int getMaxConnectionsPerDestination() {
         return maxConnectionsPerDestination;
      }

      /**
       * Sets the maximum number of connections per destination.
       * @param maxConnectionsPerDestination The number of connections.
       * @return A self-reference.
       */
      public Builder setMaxConnectionsPerDestination(final int maxConnectionsPerDestination) {
         this.maxConnectionsPerDestination = maxConnectionsPerDestination;
         return this;
      }

      /**
       * Gets the maximum total number of connections.
       * @return The number of connections.
       */
      public int getMaxConnectionsTotal() {
         return maxConnectionsTotal;
      }

      /**
       * Sets the maximum total number of connections.
       * @param maxConnectionsTotal The number of connections.
       * @return A self-reference.
       */
      public Builder setMaxConnectionsTotal(final int maxConnectionsTotal) {
         this.maxConnectionsTotal = maxConnectionsTotal;
         return this;
      }

      /**
       * Gets the request buffer size.
       * @return The request buffer size.
       */
      public int getRequestBufferSize() {
         return requestBufferSize;
      }

      /**
       * Sets the request buffer size.
       * @param requestBufferSize The request buffer size.
       * @return A self-reference.
       */
      public Builder setRequestBufferSize(final int requestBufferSize) {
         this.requestBufferSize = requestBufferSize;
         return this;
      }

      /**
       * Gets the response buffer size.
       * @return The response buffer size.
       */
      public int getResponseBufferSize() {
         return responseBufferSize;
      }

      /**
       * Sets the response buffer size.
       * @param responseBufferSize The response buffer size.
       * @return A self-reference.
       */
      public Builder setResponseBufferSize(final int responseBufferSize) {
         this.responseBufferSize = responseBufferSize;
         return this;
      }

      /**
       * Gets the extra properties.
       * @return The properties.
       */
      public Properties getProps() {
         return props;
      }

      /**
       * Sets the extra properties.
       * @param props The properties.
       * @return A self-reference.
       */
      public Builder setProps(final Properties props) {
         this.props = props != null ? props : new Properties();
         return this;
      }

      /**
       * Are all (even invalid) certificates trusted?
       * @return Are all certificates trusted.
       */
      public boolean getTrustAllCertificates() {
         return trustAllCertificates;
      }

      /**
       * Sets if all (even invalid) certificates are trusted.
       * @param trustAllCertificates Are all certificates trusted?
       * @return A self-reference.
       */
      public Builder setTrustAllCertificates(final boolean trustAllCertificates) {
         this.trustAllCertificates = trustAllCertificates;
         return this;
      }

      /**
       * Creates the options.
       * @return The options.
       */
      public ClientOptions create() {
         return new ClientOptions(userAgent, connectionTimeoutMillis, requestTimeoutMillis, socketTimeoutMillis,
                 proxyHost, proxyPort,
                 followRedirects,
                 maxConnectionsPerDestination, maxConnectionsTotal,
                 requestBufferSize, responseBufferSize, trustAllCertificates, props);
      }

      /**
       * Convert a time string to an integer number of milliseconds.
       * @param timeString The time string.
       * @return The number of milliseconds as an integer.
       */
      private int fromTime(final String timeString) {
         long millis = InitUtil.millisFromTime(timeString);
         return millis != Long.MIN_VALUE ? (int)millis : 0;
      }

      String userAgent = USER_AGENT[DEFAULT_VALUE];
      int connectionTimeoutMillis = fromTime(CONNECTION_TIMEOUT[DEFAULT_VALUE]);
      int requestTimeoutMillis = fromTime(REQUEST_TIMEOUT[DEFAULT_VALUE]);
      int socketTimeoutMillis = fromTime(SOCKET_TIMEOUT[DEFAULT_VALUE]);
      String proxyHost = PROXY_HOST[DEFAULT_VALUE];
      int proxyPort = Integer.parseInt(PROXY_PORT[DEFAULT_VALUE]);
      boolean followRedirects = FOLLOW_REDIRECTS[DEFAULT_VALUE].equalsIgnoreCase("true");
      int maxConnectionsPerDestination = Integer.parseInt(MAX_CONNECTIONS_PER_DESTINATION[DEFAULT_VALUE]);
      int maxConnectionsTotal = Integer.parseInt(MAX_CONNECTIONS_TOTAL[DEFAULT_VALUE]);
      int requestBufferSize = Integer.parseInt(REQUEST_BUFFER_SIZE[DEFAULT_VALUE]);
      int responseBufferSize = Integer.parseInt(RESPONSE_BUFFER_SIZE[DEFAULT_VALUE]);
      boolean trustAllCertificates = false;
      Properties props = new Properties();
   }
}
