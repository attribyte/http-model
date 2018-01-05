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

package org.attribyte.api.http.test;

import com.google.common.collect.Lists;
import com.google.common.hash.HashCode;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import com.google.common.io.Files;
import com.google.protobuf.ByteString;
import org.attribyte.api.http.Client;
import org.attribyte.api.http.ClientOptions;
import org.attribyte.api.http.GetRequestBuilder;
import org.attribyte.api.http.PostRequestBuilder;
import org.attribyte.api.http.Request;
import org.attribyte.api.http.Response;
import org.attribyte.api.http.impl.commons.Commons3Client;
import org.attribyte.api.http.impl.commons.Commons4Client;
import org.attribyte.api.http.impl.jetty.JettyClient;
import org.attribyte.api.http.impl.ning.NingClient;
import org.attribyte.util.InitUtil;

import java.io.File;
import java.text.NumberFormat;
import java.util.List;
import java.util.Properties;

public class PerfTest {

   public static void main(String[] args) throws Exception {

      Client client = null;

      try {

         Properties props = new Properties();
         InitUtil.fromCommandLine(args, props);
         InitUtil init = new InitUtil("", props, false);

         String clientName = init.getProperty("client", "");
         if(clientName.equalsIgnoreCase("jetty")) {
            client = new JettyClient(ClientOptions.IMPLEMENTATION_DEFAULT);
         } else if(clientName.equalsIgnoreCase("ning")) {
            client = new NingClient(ClientOptions.IMPLEMENTATION_DEFAULT);
         } else if(clientName.equalsIgnoreCase("commons3")) {
            client = new Commons3Client(ClientOptions.IMPLEMENTATION_DEFAULT);
         } else if(clientName.equalsIgnoreCase("commons4")) {
            client = new Commons4Client(ClientOptions.IMPLEMENTATION_DEFAULT);
         } else {
            System.err.println("A 'client' must be specified");
            return;
         }

         System.out.println("Client: " + client.getClass().getName());

         String url = init.getProperty("url", null);
         if(url == null) {
            System.err.println("A 'url' must be specified");
            return;
         }

         System.out.println("URL: " + url);

         System.out.println(props.getProperty("numThreads"));

         int numThreads = init.getIntProperty("numThreads", 1);

         System.out.println("Number of Threads: " + numThreads);

         int warmupSeconds = init.getIntProperty("warmupSeconds", 10);

         System.out.println("Warmup seconds: " + warmupSeconds);

         int testSeconds = init.getIntProperty("testSeconds", 20);

         System.out.println("Test seconds: " + testSeconds);

         String postBodyFile = init.getProperty("postBodyFile", null);
         byte[] postBody = null;
         if(postBodyFile != null) {
            postBody = Files.toByteArray(new File(postBodyFile));
         }

         Request request = postBody == null ? new GetRequestBuilder(url).create() :
                 new PostRequestBuilder(url, postBody).create();

         List<TestRunner> runners = Lists.newArrayListWithCapacity(numThreads);
         for(int i = 0; i < numThreads; i++) {
            runners.add(new TestRunner(client, request, null)); //TODO
         }

         System.out.println("Starting warm-up...");

         for(TestRunner runner : runners) {
            Thread thread = new Thread(runner);
            thread.setDaemon(true);
            thread.start();
         }

         long stopTime = System.currentTimeMillis() + warmupSeconds * 1000L;
         while(System.currentTimeMillis() < stopTime) {
            Thread.sleep(10L);
         }

         for(TestRunner runner : runners) {
            runner.running = false;
            while(runner.elapsedTime == 0L) {
               Thread.sleep(10L);
            }
            runner.reset();
         }

         System.out.println("Starting test...");

         for(TestRunner runner : runners) {
            Thread thread = new Thread(runner);
            thread.setDaemon(true);
            thread.start();
         }

         stopTime = System.currentTimeMillis() + testSeconds * 1000L;
         while(System.currentTimeMillis() < stopTime) {
            Thread.sleep(10L);
         }

         System.out.println("Test complete!");

         int requestCount = 0;
         int errorCount = 0;
         long totalTime = 0L;

         for(TestRunner runner : runners) {
            runner.running = false;
            while(runner.elapsedTime == 0L) {
               Thread.sleep(10L);
            }
            requestCount += runner.count;
            errorCount += runner.errorCount;
            totalTime += runner.elapsedTime;
         }

         double avgTimeSeconds = (double)totalTime / (double)numThreads / 1000.0;
         double rate = (double)requestCount / avgTimeSeconds;
         NumberFormat formatter = NumberFormat.getNumberInstance();
         formatter.setMaximumFractionDigits(3);
         formatter.setMinimumFractionDigits(3);

         System.out.println("Executed " + requestCount + " requests with " + errorCount + " failures (" + formatter.format(rate) + "/s)");
      } finally {
         if(client != null) client.shutdown();
      }
   }

   private static final class TestRunner implements Runnable {

      TestRunner(final Client client, final Request request, final HashCode expectedResponseHash) {
         this.client = client;
         this.request = request;
         this.expectedResponseHash = expectedResponseHash;
      }

      public void run() {
         startTimestamp = System.currentTimeMillis();
         while(running) {
            count++;
            try {
               Response response = client.send(request);
               if(response.getStatusCode() == 200) {
                  ByteString body = response.getBody();
                  if(body != null) {
                     byte[] responseBytes = body.toByteArray();
                     if(expectedResponseHash != null && !hashFunction.hashBytes(responseBytes).equals(expectedResponseHash)) {
                        System.err.println("Error (Hash Mismatch)");
                        errorCount++;
                     }
                  } else {
                     System.err.println("Error (No Response)");
                     errorCount++;
                  }
               } else {
                  System.err.println("Error (" + response.getStatusCode() + ")");
                  errorCount++;
               }
            } catch(Exception e) {
               e.printStackTrace();
               errorCount++;
               System.exit(1);
            }
         }

         this.elapsedTime = System.currentTimeMillis() - startTimestamp;
      }

      void reset() {
         count = 0;
         errorCount = 0;
         elapsedTime = 0;
         startTimestamp = 0;
         running = true;
      }

      private final Client client;
      private final Request request;
      private final HashCode expectedResponseHash;
      int count;
      int errorCount;
      volatile boolean running = true;
      private final HashFunction hashFunction = Hashing.md5();
      long startTimestamp = 0L;
      long elapsedTime = 0L;

   }


}
