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

package org.attribyte.api.http;

import org.junit.Test;

import java.net.InetAddress;

import static org.junit.Assert.*;

public class PublicAddressesTest {

   private static final String[] REFUSED = {
           "127.0.0.1", "127.255.255.254", "10.1.2.3", "172.16.0.1", "172.31.255.255", "192.168.1.1",
           "169.254.169.254", "100.64.0.1", "100.127.255.255", "0.0.0.0", "0.1.2.3", "198.18.0.1",
           "198.19.255.255", "192.0.0.1", "224.0.0.1", "240.0.0.1", "255.255.255.255",
           "::", "::1", "fe80::1", "fc00::1", "fd12:3456::1", "ff02::1",
           "::7f00:1", "::a9fe:a9fe", "::ffff:127.0.0.1", "::ffff:169.254.169.254",
           "64:ff9b::7f00:1", "64:ff9b::a9fe:a9fe", "64:ff9b:1::1",
           "2002:7f00:1::", "2002:a9fe:a9fe::1", "2002:c0a8:101::", "2001:0:4136:e378::1"
   };

   private static final String[] PUBLIC = {
           "8.8.8.8", "1.1.1.1", "172.32.0.1", "172.15.255.255", "100.128.0.1", "100.63.255.255",
           "198.20.0.1", "192.0.1.1", "223.255.255.255",
           "2606:4700:4700::1111", "2001:4860:4860::8888", "64:ff9b::808:808", "2002:808:808::1"
   };

   @Test
   public void refusesWhatIsntPublic() throws Exception {
      for(String address : REFUSED) {
         assertFalse(address + " must be refused", PublicAddresses.isPublic(InetAddress.getByName(address)));
      }
   }

   @Test
   public void allowsWhatIsPublic() throws Exception {
      for(String address : PUBLIC) {
         assertTrue(address + " is public", PublicAddresses.isPublic(InetAddress.getByName(address)));
      }
   }

   @Test
   public void checkUrlRefuses() {
      String[] urls = {
              null, "", "not a url", "file:///etc/passwd", "ftp://example.com/", "http:///path",
              "http://localhost/", "http://LOCALHOST:8080/", "http://printer.local/", "http://a.localhost/",
              "http://127.0.0.1:8080/health", "http://[::1]/", "http://169.254.169.254/latest/meta-data/",
              "http://[::ffff:10.0.0.1]/", "http://[64:ff9b::a9fe:a9fe]/"
      };
      for(String url : urls) {
         try {
            PublicAddresses.checkUrl(url);
            fail(url + " must be refused");
         } catch(IllegalArgumentException expected) {
            // refused
         }
      }
   }

   @Test
   public void checkUrlAllowsPublic() {
      PublicAddresses.checkUrl("https://8.8.8.8/");
      PublicAddresses.checkUrl("http://[2606:4700:4700::1111]/");
      PublicAddresses.checkUrl("https://attribyte.com/");
   }
}
