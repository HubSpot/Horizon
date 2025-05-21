package com.hubspot.horizon.ning.internal;

import com.hubspot.horizon.CustomDnsResolver;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.List;
import org.asynchttpclient.shaded.io.netty.resolver.InetNameResolver;
import org.asynchttpclient.shaded.io.netty.util.concurrent.EventExecutor;
import org.asynchttpclient.shaded.io.netty.util.concurrent.Promise;

public class CustomNingNameResolver extends InetNameResolver {

  private final CustomDnsResolver customDnsResolver;

  public CustomNingNameResolver(
    EventExecutor executor,
    CustomDnsResolver customDnsResolver
  ) {
    super(executor);
    this.customDnsResolver = customDnsResolver;
  }

  @Override
  protected void doResolve(String inetHost, Promise<InetAddress> promise) {
    try {
      customDnsResolver
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
      customDnsResolver
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
