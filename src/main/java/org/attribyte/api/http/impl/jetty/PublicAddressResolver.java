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

package org.attribyte.api.http.impl.jetty;

import org.attribyte.api.http.PublicAddresses;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.util.Promise;
import org.eclipse.jetty.util.SocketAddressResolver;

import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * A Jetty client's name lookup that connects only to public addresses ({@link PublicAddresses}).
 * <p>
 *    Every connection the client makes resolves its host here, so this covers the first request, every
 *    redirect hop and every retry, at the moment of connecting: a host can't pass a check and then
 *    answer with a private address a second later. A host that resolves to any non-public address is
 *    refused with a {@link ConnectException} that says so.
 * </p>
 * <p>
 *    The lookup itself is Jetty's own ({@link SocketAddressResolver.Async}), made on first use with the
 *    client's executor, scheduler and {@code addressResolutionTimeout}, which exist only once the client
 *    has started. Hosts in {@code trustedHosts} (a proxy, say) are looked up without the check.
 * </p>
 */
public final class PublicAddressResolver implements SocketAddressResolver {

   /**
    * @param client The client this resolver is set on, before it starts.
    * @param trustedHosts Hosts connected to without the check, or an empty set.
    */
   public PublicAddressResolver(final HttpClient client, final Set<String> trustedHosts) {
      this.client = client;
      this.trustedHosts = trustedHosts.stream().map(h -> h.toLowerCase(Locale.ROOT)).collect(Collectors.toUnmodifiableSet());
   }

   @Override
   public void resolve(final String host, final int port, final Map<String, Object> context,
                       final Promise<List<InetSocketAddress>> promise) {
      if(trustedHosts.contains(host.toLowerCase(Locale.ROOT))) {
         lookup().resolve(host, port, context, promise);
         return;
      }
      lookup().resolve(host, port, context, new Promise<>() {
         @Override
         public void succeeded(final List<InetSocketAddress> addresses) {
            for(InetSocketAddress address : addresses) {
               if(!PublicAddresses.isPublic(address.getAddress())) {
                  promise.failed(new ConnectException("Refused: " + host + " resolves to a private or reserved address"));
                  return;
               }
            }
            promise.succeeded(addresses);
         }

         @Override
         public void failed(final Throwable x) {
            promise.failed(x);
         }
      });
   }

   private SocketAddressResolver lookup() {
      SocketAddressResolver lookup = this.lookup;
      if(lookup == null) {
         synchronized(this) {
            lookup = this.lookup;
            if(lookup == null) {
               lookup = new SocketAddressResolver.Async(client.getExecutor(), client.getScheduler(),
                       client.getAddressResolutionTimeout());
               this.lookup = lookup;
            }
         }
      }
      return lookup;
   }

   private final HttpClient client;
   private final Set<String> trustedHosts;
   private volatile SocketAddressResolver lookup;
}
