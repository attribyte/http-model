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

package org.attribyte.api.http.impl.jdk;

import org.attribyte.api.http.ClientOptions;
import org.attribyte.api.http.DeleteRequestBuilder;
import org.attribyte.api.http.FormPostRequestBuilder;
import org.attribyte.api.http.GetRequestBuilder;
import org.attribyte.api.http.HeadRequestBuilder;
import org.attribyte.api.http.OptionsRequestBuilder;
import org.attribyte.api.http.PatchRequestBuilder;
import org.attribyte.api.http.PostRequestBuilder;
import org.attribyte.api.http.PutRequestBuilder;
import org.attribyte.api.http.Request;
import org.attribyte.api.http.RequestOptions;
import org.attribyte.api.http.Response;
import org.attribyte.api.http.impl.TestHttpServer;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

public class JdkClientTest {

   private static JdkClient client;
   private static TestHttpServer server;

   @BeforeClass
   public static void setUp() throws Exception {
      server = new TestHttpServer();
      server.start();
      client = new JdkClient(ClientOptions.IMPLEMENTATION_DEFAULT);
   }

   @AfterClass
   public static void tearDown() throws Exception {
      client.shutdown();
      server.stop();
   }

   // --- External URL test ---

   @Test
   public void getExternalTest() throws Exception {
      Request request = new GetRequestBuilder("https://attribyte.com").create();
      Response response = client.send(request, RequestOptions.DEFAULT);
      assertEquals(200, response.statusCode);
      assertNotNull(response.getBody());
      assertTrue(response.getBody().size() > 0);
   }

   // --- Local server tests ---

   @Test
   public void testGet() throws Exception {
      Request request = new GetRequestBuilder(server.baseUrl() + "/ok").create();
      Response response = client.send(request, RequestOptions.DEFAULT);
      assertEquals(200, response.statusCode);
      assertEquals("OK", response.getBody().toStringUtf8());
      assertEquals("text/plain", response.getHeaderValue("content-type"));
   }

   @Test
   public void testPost() throws Exception {
      byte[] body = "Hello, World!".getBytes(StandardCharsets.UTF_8);
      Request request = new PostRequestBuilder(server.baseUrl() + "/echo", body)
              .addHeader("Content-Type", "text/plain")
              .create();
      Response response = client.send(request, RequestOptions.DEFAULT);
      assertEquals(200, response.statusCode);
      assertEquals("Hello, World!", response.getBody().toStringUtf8());
      assertEquals("POST", response.getHeaderValue("x-method"));
   }

   @Test
   public void testPostFormParameters() throws Exception {
      FormPostRequestBuilder builder = new FormPostRequestBuilder(server.baseUrl() + "/form");
      builder.addParameter("username", "testuser");
      builder.addParameter("password", "secret123");
      Request request = builder.create();
      Response response = client.send(request, RequestOptions.DEFAULT);
      assertEquals(200, response.statusCode);
      String formBody = response.getBody().toStringUtf8();
      assertTrue(formBody.contains("username=testuser"));
      assertTrue(formBody.contains("password=secret123"));
   }

   @Test
   public void testPut() throws Exception {
      byte[] body = "{\"key\":\"value\"}".getBytes(StandardCharsets.UTF_8);
      Request request = new PutRequestBuilder(server.baseUrl() + "/echo", body)
              .addHeader("Content-Type", "application/json")
              .create();
      Response response = client.send(request, RequestOptions.DEFAULT);
      assertEquals(200, response.statusCode);
      assertEquals("{\"key\":\"value\"}", response.getBody().toStringUtf8());
      assertEquals("PUT", response.getHeaderValue("x-method"));
   }

   @Test
   public void testPatch() throws Exception {
      byte[] body = "patch-data".getBytes(StandardCharsets.UTF_8);
      Request request = new PatchRequestBuilder(server.baseUrl() + "/echo", body)
              .addHeader("Content-Type", "text/plain")
              .create();
      Response response = client.send(request, RequestOptions.DEFAULT);
      assertEquals(200, response.statusCode);
      assertEquals("patch-data", response.getBody().toStringUtf8());
      assertEquals("PATCH", response.getHeaderValue("x-method"));
   }

   @Test
   public void testDelete() throws Exception {
      Request request = new DeleteRequestBuilder(server.baseUrl() + "/ok").create();
      Response response = client.send(request, RequestOptions.DEFAULT);
      assertEquals(200, response.statusCode);
      assertEquals("DELETED", response.getBody().toStringUtf8());
      assertEquals("DELETE", response.getHeaderValue("x-method"));
   }

   @Test
   public void testHead() throws Exception {
      Request request = new HeadRequestBuilder(server.baseUrl() + "/ok").create();
      Response response = client.send(request, RequestOptions.DEFAULT);
      assertEquals(200, response.statusCode);
      // HEAD responses should have no body
      assertTrue(response.getBody() == null || response.getBody().isEmpty());
   }

   @Test
   public void testOptions() throws Exception {
      Request request = new OptionsRequestBuilder(server.baseUrl() + "/ok").create();
      Response response = client.send(request, RequestOptions.DEFAULT);
      assertEquals(200, response.statusCode);
      String allow = response.getHeaderValue("allow");
      assertNotNull(allow);
      assertTrue(allow.contains("GET"));
      assertTrue(allow.contains("POST"));
      assertTrue(allow.contains("DELETE"));
   }

   @Test
   public void testNotFound() throws Exception {
      Request request = new GetRequestBuilder(server.baseUrl() + "/not-found").create();
      Response response = client.send(request, RequestOptions.DEFAULT);
      assertEquals(404, response.statusCode);
   }

   @Test
   public void testServerError() throws Exception {
      Request request = new GetRequestBuilder(server.baseUrl() + "/server-error").create();
      Response response = client.send(request, RequestOptions.DEFAULT);
      assertEquals(500, response.statusCode);
   }

   @Test
   public void testRequestHeaders() throws Exception {
      Request request = new GetRequestBuilder(server.baseUrl() + "/headers")
              .addHeader("X-Custom-Header", "custom-value")
              .addHeader("X-Another", "another-value")
              .create();
      Response response = client.send(request, RequestOptions.DEFAULT);
      assertEquals(200, response.statusCode);
      assertEquals("custom-value", response.getHeaderValue("x-echo-x-custom-header"));
      assertEquals("another-value", response.getHeaderValue("x-echo-x-another"));
   }

   @Test
   public void testAsyncGet() throws Exception {
      Request request = new GetRequestBuilder(server.baseUrl() + "/ok").create();
      Response response = client.asyncSend(request, RequestOptions.DEFAULT).get(5, TimeUnit.SECONDS);
      assertEquals(200, response.statusCode);
      assertEquals("OK", response.getBody().toStringUtf8());
   }

   @Test
   public void testCompletableGet() throws Exception {
      Request request = new GetRequestBuilder(server.baseUrl() + "/ok").create();
      CompletableFuture<Response> future = client.completableSend(request, RequestOptions.DEFAULT);
      Response response = future.get(5, TimeUnit.SECONDS);
      assertEquals(200, response.statusCode);
      assertEquals("OK", response.getBody().toStringUtf8());
   }

   @Test
   public void testPostLargeBody() throws Exception {
      byte[] body = new byte[64 * 1024]; // 64KB
      for(int i = 0; i < body.length; i++) {
         body[i] = (byte)(i % 256);
      }
      Request request = new PostRequestBuilder(server.baseUrl() + "/echo", body)
              .addHeader("Content-Type", "application/octet-stream")
              .create();
      Response response = client.send(request, RequestOptions.DEFAULT);
      assertEquals(200, response.statusCode);
      assertArrayEquals(body, response.getBody().toByteArray());
   }

   @Test
   public void testMultipleSequentialRequests() throws Exception {
      for(int i = 0; i < 10; i++) {
         Request request = new GetRequestBuilder(server.baseUrl() + "/ok").create();
         Response response = client.send(request, RequestOptions.DEFAULT);
         assertEquals(200, response.statusCode);
         assertEquals("OK", response.getBody().toStringUtf8());
      }
   }
}
