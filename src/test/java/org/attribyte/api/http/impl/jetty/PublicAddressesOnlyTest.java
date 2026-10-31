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

package org.attribyte.api.http.impl.jetty;

import org.attribyte.api.http.ClientOptions;
import org.attribyte.api.http.GetRequestBuilder;
import org.attribyte.api.http.RequestOptions;
import org.attribyte.api.http.Response;
import org.attribyte.api.http.impl.TestHttpServer;
import org.eclipse.jetty.client.HttpClient;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.util.Properties;
import java.util.Set;

import static org.junit.Assert.*;

/**
 * A client with {@code publicAddressesOnly} connects to nothing private: not the local test server, not
 * by literal address, and not by a redirect from a host it was allowed to reach.
 */
public class PublicAddressesOnlyTest {

   private static TestHttpServer server;
   private static JettyClient publicOnly;
   private static JettyClient trustsLocalhost;
   private static final RequestOptions FOLLOW = new RequestOptions(true, RequestOptions.DEFAULT_MAX_RESPONSE_BYTES, 15);

   @BeforeClass
   public static void setUp() throws Exception {
      server = new TestHttpServer();
      server.start();

      Properties props = new Properties();
      props.setProperty("test.publicAddressesOnly", "true");
      publicOnly = new JettyClient(new ClientOptions("test.", props));

      // localhost is trusted, as a proxy would be; anything it redirects to is checked.
      HttpClient httpClient = new HttpClient();
      httpClient.setFollowRedirects(true);
      httpClient.setSocketAddressResolver(new PublicAddressResolver(httpClient, Set.of("localhost")));
      trustsLocalhost = new JettyClient(httpClient);
   }

   @AfterClass
   public static void tearDown() throws Exception {
      publicOnly.shutdown();
      trustsLocalhost.shutdown();
      server.stop();
   }

   private static void assertRefused(final JettyClient client, final String url) {
      try {
         Response response = client.send(new GetRequestBuilder(url).create(), FOLLOW);
         fail(url + " must be refused, got " + response.statusCode);
      } catch(IOException e) {
         assertTrue(url + ": " + e, chain(e).contains("Refused"));
      }
   }

   private static String chain(Throwable t) {
      StringBuilder sb = new StringBuilder();
      for(Throwable c = t; c != null; c = c.getCause()) {
         sb.append(c.getMessage()).append(" | ");
      }
      return sb.toString();
   }

   @Test
   public void refusesTheLocalServer() {
      assertRefused(publicOnly, server.baseUrl() + "/ok");
      assertRefused(publicOnly, "http://127.0.0.1:" + server.getPort() + "/ok");
      assertRefused(publicOnly, "http://[::1]:" + server.getPort() + "/ok");
      assertRefused(publicOnly, "http://169.254.169.254/latest/meta-data/");
   }

   @Test
   public void refusesARedirectToAPrivateAddress() throws Exception {
      Response ok = trustsLocalhost.send(new GetRequestBuilder(server.baseUrl() + "/ok").create(), FOLLOW);
      assertEquals("a trusted host is reached", 200, ok.statusCode);
      assertRefused(trustsLocalhost, server.baseUrl() + "/redirect?http://127.0.0.1:" + server.getPort() + "/ok");
   }

   @Test
   public void reachesThePublicInternet() throws Exception {
      Response response = publicOnly.send(new GetRequestBuilder("https://attribyte.com").create(), FOLLOW.truncateOnLimit());
      assertEquals(200, response.statusCode);
   }
}
