package org.attribyte.api.http.impl.jetty;

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
   public void testGetMulti() throws Exception {
      for(int i = 0; i < 10; i++) {
         testGet();
      }
   }

   @Test
   public void testGet() throws Exception {
      Request request = new GetRequestBuilder("https://attribyte.com")
              .create();
      Response response = client.send(request, RequestOptions.DEFAULT.truncateOnLimit());
      assertEquals(200, response.statusCode);
      assertNotNull(response.stats);
      System.out.println(response.stats.toString());
   }

   @Test
   public void testGetStreamMulti() throws Exception {
      for(int i = 0; i < 10; i++) {
         testGetStream();
      }
   }

   @Test
   public void testGetStream() throws Exception {
      Request request = new GetRequestBuilder("https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/Content-Type")
              .create();
      StreamedResponse response = client.stream(request, 20L, TimeUnit.SECONDS);
      try(InputStream is = response.getBodySource().openStream()) {
         ByteStreams.exhaust(is);
      }
      assertEquals(200, response.statusCode);
      assertNotNull(response.stats);
      System.out.println(response.stats.toString());
   }
}
