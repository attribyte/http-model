package org.attribyte.api.http.impl.jetty;

import com.google.common.base.Charsets;
import com.google.common.hash.Hashing;
import com.google.common.io.ByteStreams;
import org.attribyte.api.http.ClientOptions;
import org.attribyte.api.http.GetRequestBuilder;
import org.attribyte.api.http.Request;
import org.attribyte.api.http.RequestOptions;
import org.attribyte.api.http.Response;
import org.attribyte.api.http.StreamedResponse;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.InputStream;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class JettyClientTest {

   private static JettyClient client;

   @BeforeClass
   public static void initClient() throws Exception {
      System.out.println("Starting client...");
      client = new JettyClient(ClientOptions.IMPLEMENTATION_DEFAULT);
   }

   @AfterClass
   public static void stopClient() throws Exception {
      System.out.println("Stopping client...");
      client.shutdown();
   }

   @Test
   public void getMultiTest() throws Exception {
      for(int i = 0; i < 10; i++) {
         getTest();
      }
   }

   @Test
   public void getTest() throws Exception {
      Request request = new GetRequestBuilder("https://attribyte.com")
              .create();
      Response response = client.send(request, RequestOptions.DEFAULT.truncateOnLimit());
      assertEquals(200, response.statusCode);
      assertNotNull(response.stats);
      System.out.println(response.stats.toString());
      System.out.println(response.getBody().size());
      System.out.println("HASH: " + Hashing.sha256().hashBytes(response.getBody().toByteArray()));

   }

   @Test
   public void testGetStreamMulti() throws Exception {
      for(int i = 0; i < 10; i++) {
         getStreamTest();
      }
   }

   @Test
   public void getStreamTest() throws Exception {
      Request request = new GetRequestBuilder("https://attribyte.com")
              .create();
      StreamedResponse response = client.stream(request, 20L, TimeUnit.SECONDS);
      try(InputStream is = response.getBodySource().openStream()) {
         System.out.println(new String(ByteStreams.toByteArray(is), Charsets.UTF_8));
      }
      assertEquals(200, response.statusCode);
   }

   @Test
   public void testTest() throws Exception {
      Request request = new GetRequestBuilder("https://attribyte.com")
              .create();
      Response response = client.test(request, RequestOptions.DEFAULT).get(10, TimeUnit.SECONDS);
      assertEquals(200, response.statusCode);
      System.out.println(response.toString());
      assertNotNull(response.stats);
      System.out.println(response.stats.toString());
   }
}
