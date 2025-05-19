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
      InetAddress[] addresses = customDnsResolver.resolve(inetHost);
      if (addresses.length == 0) {
        promise.setFailure(new UnknownHostException());
        return;
      }
      promise.setSuccess(addresses[0]);
    } catch (UnknownHostException e) {
      promise.setFailure(e);
    }
  }

  @Override
  protected void doResolveAll(String inetHost, Promise<List<InetAddress>> promise) {
    try {
      promise.setSuccess(Arrays.asList(customDnsResolver.resolve(inetHost)));
    } catch (UnknownHostException e) {
      promise.setFailure(e);
    }
  }
}
