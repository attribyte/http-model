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

package org.attribyte.api.http.impl;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

/**
 * A local HTTP server for testing client implementations.
 * Uses the JDK built-in {@code com.sun.net.httpserver.HttpServer}.
 */
public class TestHttpServer {

   private final HttpServer server;
   private final int port;

   public TestHttpServer() throws IOException {
      server = HttpServer.create(new InetSocketAddress(0), 0);
      port = server.getAddress().getPort();

      server.createContext("/ok", this::handleOk);
      server.createContext("/echo", this::handleEcho);
      server.createContext("/not-found", this::handleNotFound);
      server.createContext("/server-error", this::handleServerError);
      server.createContext("/headers", this::handleHeaders);
      server.createContext("/form", this::handleForm);
   }

   public void start() {
      server.start();
   }

   public void stop() {
      server.stop(0);
   }

   public int getPort() {
      return port;
   }

   public String baseUrl() {
      return "http://localhost:" + port;
   }

   private void handleOk(HttpExchange exchange) throws IOException {
      String method = exchange.getRequestMethod();
      exchange.getRequestBody().readAllBytes(); // drain

      switch(method) {
         case "GET": {
            byte[] body = "OK".getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "text/plain");
            exchange.sendResponseHeaders(200, body.length);
            try(OutputStream os = exchange.getResponseBody()) {
               os.write(body);
            }
            break;
         }
         case "DELETE": {
            byte[] body = "DELETED".getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "text/plain");
            exchange.getResponseHeaders().add("X-Method", "DELETE");
            exchange.sendResponseHeaders(200, body.length);
            try(OutputStream os = exchange.getResponseBody()) {
               os.write(body);
            }
            break;
         }
         case "HEAD": {
            exchange.getResponseHeaders().add("Content-Type", "text/plain");
            exchange.getResponseHeaders().add("X-Custom", "head-value");
            exchange.sendResponseHeaders(200, -1);
            break;
         }
         case "OPTIONS": {
            exchange.getResponseHeaders().add("Allow", "GET, POST, PUT, DELETE, HEAD, OPTIONS, PATCH");
            exchange.sendResponseHeaders(200, -1);
            break;
         }
         default: {
            exchange.sendResponseHeaders(405, -1);
            break;
         }
      }
   }

   private void handleEcho(HttpExchange exchange) throws IOException {
      String method = exchange.getRequestMethod();
      byte[] requestBody = exchange.getRequestBody().readAllBytes();

      exchange.getResponseHeaders().add("X-Method", method);
      exchange.getResponseHeaders().add("Content-Type", "application/octet-stream");

      if(requestBody.length > 0) {
         exchange.sendResponseHeaders(200, requestBody.length);
         try(OutputStream os = exchange.getResponseBody()) {
            os.write(requestBody);
         }
      } else {
         byte[] body = method.getBytes(StandardCharsets.UTF_8);
         exchange.sendResponseHeaders(200, body.length);
         try(OutputStream os = exchange.getResponseBody()) {
            os.write(body);
         }
      }
   }

   private void handleNotFound(HttpExchange exchange) throws IOException {
      exchange.getRequestBody().readAllBytes(); // drain
      byte[] body = "Not Found".getBytes(StandardCharsets.UTF_8);
      exchange.sendResponseHeaders(404, body.length);
      try(OutputStream os = exchange.getResponseBody()) {
         os.write(body);
      }
   }

   private void handleServerError(HttpExchange exchange) throws IOException {
      exchange.getRequestBody().readAllBytes(); // drain
      byte[] body = "Internal Server Error".getBytes(StandardCharsets.UTF_8);
      exchange.sendResponseHeaders(500, body.length);
      try(OutputStream os = exchange.getResponseBody()) {
         os.write(body);
      }
   }

   private void handleHeaders(HttpExchange exchange) throws IOException {
      exchange.getRequestBody().readAllBytes(); // drain

      // Echo back each request header as X-Echo-{name}: {value}
      for(Map.Entry<String, List<String>> entry : exchange.getRequestHeaders().entrySet()) {
         String name = entry.getKey();
         for(String value : entry.getValue()) {
            exchange.getResponseHeaders().add("X-Echo-" + name, value);
         }
      }

      byte[] body = "HEADERS".getBytes(StandardCharsets.UTF_8);
      exchange.sendResponseHeaders(200, body.length);
      try(OutputStream os = exchange.getResponseBody()) {
         os.write(body);
      }
   }

   private void handleForm(HttpExchange exchange) throws IOException {
      byte[] requestBody = exchange.getRequestBody().readAllBytes();
      String formData = new String(requestBody, StandardCharsets.UTF_8);

      // Also include query string parameters (some clients send form params as query params)
      String query = exchange.getRequestURI().getRawQuery();
      if(query != null && !query.isEmpty()) {
         if(!formData.isEmpty()) {
            formData = formData + "&" + query;
         } else {
            formData = query;
         }
      }

      // Echo back all form data so tests can verify parameter encoding
      exchange.getResponseHeaders().add("Content-Type", "text/plain");
      exchange.getResponseHeaders().add("X-Method", exchange.getRequestMethod());
      byte[] body = formData.getBytes(StandardCharsets.UTF_8);
      exchange.sendResponseHeaders(200, body.length);
      try(OutputStream os = exchange.getResponseBody()) {
         os.write(body);
      }
   }
}
