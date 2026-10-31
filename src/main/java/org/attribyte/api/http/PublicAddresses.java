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

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.Locale;

/**
 * Whether an address is on the public internet: the one check for anything that fetches a URL someone
 * else chose (a feed, a link, a page to save), so it can't be aimed at the machine it runs on or the
 * network around it (server-side request forgery).
 * <p>
 *    Refused: loopback, private (RFC 1918, IPv6 unique local), link-local (including the cloud metadata
 *    service at 169.254.169.254), carrier-grade NAT, multicast, "this network", benchmarking and reserved
 *    ranges, and IPv6 forms that carry an IPv4 address inside (IPv4-compatible, NAT64, 6to4), which are
 *    judged by the address they carry.
 * </p>
 * <p>
 *    {@link #checkUrl(String)} checks a URL before a fetch. A check before a fetch can't see where a
 *    redirect goes, or a host that answers differently a second later; a Jetty client built with
 *    {@code publicAddressesOnly} checks every connection as it is made
 *    ({@link org.attribyte.api.http.impl.jetty.PublicAddressResolver}).
 * </p>
 */
public final class PublicAddresses {

   private PublicAddresses() {
   }

   /**
    * Checks that a URL is http or https and that every address its host resolves to is public.
    * @param url The URL.
    * @throws IllegalArgumentException if it isn't, saying why.
    */
   public static void checkUrl(final String url) throws IllegalArgumentException {
      if(url == null || url.isBlank()) {
         throw new IllegalArgumentException("URL is required");
      }

      final URI uri;
      try {
         uri = URI.create(url.strip());
      } catch(IllegalArgumentException e) {
         throw new IllegalArgumentException("Invalid URL: " + url);
      }

      String scheme = uri.getScheme();
      if(scheme == null || (!scheme.equalsIgnoreCase("http") && !scheme.equalsIgnoreCase("https"))) {
         throw new IllegalArgumentException("Only http and https URLs are supported");
      }

      String host = uri.getHost();
      if(host == null || host.isBlank()) {
         throw new IllegalArgumentException("URL must have a hostname");
      }

      // Names that are local by definition, refused before asking DNS.
      String hostLower = host.toLowerCase(Locale.ROOT);
      if(hostLower.equals("localhost") || hostLower.startsWith("localhost.") || hostLower.endsWith(".localhost") ||
         hostLower.endsWith(".local")) {
         throw new IllegalArgumentException("Local addresses are not allowed");
      }

      // Every address the host maps to: a host with one public and one private record must not pass
      // because the public one happened to be checked.
      final InetAddress[] addrs;
      try {
         addrs = InetAddress.getAllByName(host);
      } catch(UnknownHostException e) {
         throw new IllegalArgumentException("Cannot resolve hostname: " + host);
      }
      for(InetAddress addr : addrs) {
         if(!isPublic(addr)) {
            throw new IllegalArgumentException("Private or reserved addresses are not allowed");
         }
      }
   }

   /**
    * @param addr An address.
    * @return Whether it is on the public internet.
    */
   public static boolean isPublic(final InetAddress addr) {
      if(addr == null) {
         return false;
      }
      if(addr.isLoopbackAddress() || addr.isSiteLocalAddress() || addr.isLinkLocalAddress() ||
         addr.isAnyLocalAddress() || addr.isMulticastAddress()) {
         return false;
      }
      byte[] b = addr.getAddress();
      if(addr instanceof Inet4Address) {
         return isPublicV4(b);
      }
      if(addr instanceof Inet6Address) {
         return isPublicV6(b);
      }
      return false;
   }

   private static boolean isPublicV4(final byte[] b) {
      int first = b[0] & 0xFF, second = b[1] & 0xFF;
      if(first == 0) return false;                                  // 0.0.0.0/8, "this network"
      if(first == 10 || first == 127) return false;                 // private, loopback
      if(first == 100 && second >= 64 && second <= 127) return false; // 100.64.0.0/10, carrier-grade NAT
      if(first == 169 && second == 254) return false;               // link-local, cloud metadata
      if(first == 172 && second >= 16 && second <= 31) return false; // private
      if(first == 192 && second == 168) return false;               // private
      if(first == 192 && second == 0 && (b[2] & 0xFF) == 0) return false; // 192.0.0.0/24, protocol assignments
      if(first == 198 && (second == 18 || second == 19)) return false; // 198.18.0.0/15, benchmarking
      if(first >= 224) return false;                                // multicast, 240/4 reserved, broadcast
      return true;
   }

   private static boolean isPublicV6(final byte[] b) {
      if((b[0] & 0xFE) == 0xFC) return false;                       // fc00::/7, unique local
      if((b[0] & 0xFF) == 0xFE && (b[1] & 0xC0) == 0x80) return false; // fe80::/10, link-local
      if((b[0] & 0xFF) == 0xFF) return false;                       // multicast
      if(allZero(b, 0, 12)) {                                       // ::/96, IPv4-compatible (::7f00:1)
         return isPublicV4(new byte[] {b[12], b[13], b[14], b[15]});
      }
      if(allZero(b, 0, 10) && (b[10] & 0xFF) == 0xFF && (b[11] & 0xFF) == 0xFF) { // ::ffff:0:0/96, IPv4-mapped
         return isPublicV4(new byte[] {b[12], b[13], b[14], b[15]});
      }
      if((b[0] & 0xFF) == 0x00 && (b[1] & 0xFF) == 0x64 && (b[2] & 0xFF) == 0xFF && (b[3] & 0xFF) == 0x9B) {
         if(allZero(b, 4, 12)) {                                    // 64:ff9b::/96, NAT64
            return isPublicV4(new byte[] {b[12], b[13], b[14], b[15]});
         }
         return false;                                              // 64:ff9b:1::/48, local-use NAT64
      }
      if((b[0] & 0xFF) == 0x20 && (b[1] & 0xFF) == 0x02) {          // 2002::/16, 6to4
         return isPublicV4(new byte[] {b[2], b[3], b[4], b[5]});
      }
      if((b[0] & 0xFF) == 0x20 && (b[1] & 0xFF) == 0x01 && b[2] == 0 && b[3] == 0) {
         return false;                                              // 2001::/32, Teredo: the IPv4 inside is obscured
      }
      return true;
   }

   private static boolean allZero(final byte[] b, final int from, final int to) {
      for(int i = from; i < to; i++) {
         if(b[i] != 0) return false;
      }
      return true;
   }
}
