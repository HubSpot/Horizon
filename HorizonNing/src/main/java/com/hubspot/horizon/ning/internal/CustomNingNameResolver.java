package com.hubspot.horizon.ning.internal;

import com.hubspot.horizon.DnsResolver;
import io.netty.resolver.InetNameResolver;
import io.netty.util.concurrent.EventExecutor;
import io.netty.util.concurrent.Promise;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.List;

public class CustomNingNameResolver extends InetNameResolver {

  private final DnsResolver dnsResolver;

  public CustomNingNameResolver(EventExecutor executor, DnsResolver dnsResolver) {
    super(executor);
    this.dnsResolver = dnsResolver;
  }

  @Override
  protected void doResolve(String inetHost, Promise<InetAddress> promise) {
    try {
      dnsResolver
        .resolve(inetHost)
        .thenAccept(addresses -> {
          if (addresses == null || addresses.length == 0) {
            promise.setFailure(
              new UnknownHostException("No addresses found for host: " + inetHost)
            );
          } else {
            promise.setSuccess(addresses[0]);
          }
        })
        .exceptionally(throwable -> {
          promise.setFailure(throwable);
          return null;
        });
    } catch (UnknownHostException e) {
      promise.setFailure(e);
    }
  }

  @Override
  protected void doResolveAll(String inetHost, Promise<List<InetAddress>> promise) {
    try {
      dnsResolver
        .resolve(inetHost)
        .thenAccept(addresses -> {
          promise.setSuccess(Arrays.asList(addresses));
        })
        .exceptionally(throwable -> {
          promise.setFailure(throwable);
          return null;
        });
    } catch (UnknownHostException e) {
      promise.setFailure(e);
    }
  }
}
