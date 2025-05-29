package com.hubspot.horizon.apache.internal;

import com.hubspot.horizon.DnsResolver;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class CustomApacheDnsResolver implements org.apache.http.conn.DnsResolver {

  private final DnsResolver dnsResolver;

  public CustomApacheDnsResolver(DnsResolver dnsResolver) {
    this.dnsResolver = dnsResolver;
  }

  @Override
  public InetAddress[] resolve(String host) throws UnknownHostException {
    return dnsResolver.resolve(host).join();
  }
}
