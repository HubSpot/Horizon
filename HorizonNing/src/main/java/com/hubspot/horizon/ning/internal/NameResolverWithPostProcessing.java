package com.hubspot.horizon.ning.internal;

import com.hubspot.horizon.DnsResolutionPostProcessor;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.List;
import org.asynchttpclient.shaded.io.netty.resolver.InetNameResolver;
import org.asynchttpclient.shaded.io.netty.util.concurrent.EventExecutor;
import org.asynchttpclient.shaded.io.netty.util.concurrent.Promise;
import org.asynchttpclient.shaded.io.netty.util.internal.SocketUtils;

public class NameResolverWithPostProcessing extends InetNameResolver {

  private final DnsResolutionPostProcessor dnsResolutionPostProcessor;

  public NameResolverWithPostProcessing(
    EventExecutor executor,
    DnsResolutionPostProcessor dnsResolutionPostProcessor
  ) {
    super(executor);
    this.dnsResolutionPostProcessor = dnsResolutionPostProcessor;
  }

  @Override
  protected void doResolve(String inetHost, Promise<InetAddress> promise)
    throws Exception {
    try {
      promise.setSuccess(
        dnsResolutionPostProcessor.postResolve(SocketUtils.addressByName(inetHost))
      );
    } catch (UnknownHostException e) {
      promise.setFailure(e);
    }
  }

  @Override
  protected void doResolveAll(String inetHost, Promise<List<InetAddress>> promise)
    throws Exception {
    try {
      promise.setSuccess(
        dnsResolutionPostProcessor.postResolveAll(
          Arrays.asList(SocketUtils.allAddressesByName(inetHost))
        )
      );
    } catch (UnknownHostException e) {
      promise.setFailure(e);
    }
  }
}
