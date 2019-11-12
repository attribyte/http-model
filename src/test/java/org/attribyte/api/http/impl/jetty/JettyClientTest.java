package org.attribyte.api.http.impl.jetty;

import org.attribyte.api.http.ClientOptions;
import org.attribyte.api.http.GetRequestBuilder;
import org.attribyte.api.http.Request;
import org.attribyte.api.http.Response;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

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
      Response response = client.send(request);
      assertEquals(200, response.statusCode);
      assertNotNull(response.timing);
      System.out.println(response.timing.toString());
   }
}
