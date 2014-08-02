package org.attribyte.api.http.impl.test;

import org.attribyte.api.ConsoleLogger;
import org.attribyte.api.http.Client;
import org.attribyte.api.http.ClientOptions;
import org.attribyte.api.http.GetRequestBuilder;
import org.attribyte.api.http.Response;
import org.attribyte.api.http.impl.commons.Commons3Client;
import org.attribyte.api.http.impl.commons.Commons4Client;
import org.attribyte.api.http.impl.jetty.JettyClient;
import org.attribyte.api.http.impl.ning.NingClient;
import org.attribyte.util.InitUtil;

import java.util.Properties;

public class SimpleTest {

   public static void main(String[] args) throws Exception {

      Client client = null;

      try {

         Properties props = new Properties();
         args = InitUtil.fromCommandLine(args, props);
         InitUtil init = new InitUtil("", props, false);
         boolean implementationDefaults = init.getProperty("implDefaults", "false").equalsIgnoreCase("true");

         String clientName = init.getProperty("client", "");
         if(clientName.equalsIgnoreCase("jetty")) {
            if(implementationDefaults) {
               client = new JettyClient(ClientOptions.IMPLEMENTATION_DEFAULT);
            } else {
               client = new JettyClient();
               client.init("", props, new ConsoleLogger());
            }
         } else if(clientName.equalsIgnoreCase("ning")) {
            if(implementationDefaults) {
               client = new NingClient(ClientOptions.IMPLEMENTATION_DEFAULT);
            } else {
               client = new NingClient();
               client.init("", props, new ConsoleLogger());
            }
         } else if(clientName.equalsIgnoreCase("commons3")) {
            if(implementationDefaults) {
               client = new Commons3Client(ClientOptions.IMPLEMENTATION_DEFAULT);
            } else {
               client = new Commons3Client();
               client.init("", props, new ConsoleLogger());
            }
         } else if(clientName.equalsIgnoreCase("commons4")) {
            if(implementationDefaults) {
               client = new Commons4Client(ClientOptions.IMPLEMENTATION_DEFAULT);
            } else {
               client = new Commons4Client();
               client.init("", props, new ConsoleLogger());
            }
         } else {
            System.err.println("A 'client' must be specified");
            return;
         }

         if(init.getProperty("clientProps") != null) {
            Properties clientProps = new Properties();

         }

         System.out.println("Client: " + client.getClass().getName());

         String url = init.getProperty("url", null);
         if(url == null) {
            System.err.println("A 'url' must be specified");
         } else {
            Response response = client.send(new GetRequestBuilder(url).create());
            System.out.println(response.toString());
         }

      } finally {
         if(client != null) client.shutdown();
      }
   }


}
