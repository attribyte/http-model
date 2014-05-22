package org.attribyte.api.http.impl.test;

import com.google.common.collect.Lists;
import com.google.common.hash.HashCode;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import com.google.common.io.Files;
import com.google.protobuf.ByteString;
import org.attribyte.api.ConsoleLogger;
import org.attribyte.api.http.Client;
import org.attribyte.api.http.GetRequestBuilder;
import org.attribyte.api.http.PostRequestBuilder;
import org.attribyte.api.http.Request;
import org.attribyte.api.http.Response;
import org.attribyte.util.InitUtil;

import java.io.File;
import java.util.List;
import java.util.Properties;

public class PerfTest {

   public static void main(String[] args) throws Exception {
      Properties props = new Properties();
      args = InitUtil.fromCommandLine(args, props);
      InitUtil init = new InitUtil("", props, false);
      Client client = (Client)init.initClass("client", Client.class);
      client.init("", props, new ConsoleLogger());

      String url = init.getProperty("url", null);
      if(url == null) {
         System.err.println("A 'url' must be specified");
         return;
      }

      int numThreads = init.getIntProperty("numThreads", 1);
      int warmupSeconds = init.getIntProperty("warmupSeconds", 10);
      int testSeconds = init.getIntProperty("testSeconds", 20);
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

      for(TestRunner runner : runners) {
         Thread thread = new Thread(runner);
         thread.setDaemon(true);
         thread.start();
      }

      long stopTime = System.currentTimeMillis() + warmupSeconds * 1000L;
      while(System.currentTimeMillis() < stopTime) {
         Thread.sleep(10L);
      }

      System.out.println("Starting warm-up...");

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

      System.out.println("Executed " + requestCount + " requests with " + errorCount + " failures (" + rate + "/s)");
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
