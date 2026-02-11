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

package org.attribyte.api.http.test;

import org.attribyte.api.ConsoleLogger;
import org.attribyte.api.http.Client;
import org.attribyte.api.http.ClientOptions;
import org.attribyte.api.http.GetRequestBuilder;
import org.attribyte.api.http.Response;
import org.attribyte.api.http.impl.jetty.JettyClient;
import org.attribyte.util.InitUtil;

import java.util.Properties;

public class SimpleTest {

   public static void main(String[] args) throws Exception {

      Client client = null;

      try {

         Properties props = new Properties();
         InitUtil.fromCommandLine(args, props);
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
         } else {
            System.err.println("A 'client' must be specified (jetty)");
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
