package com.hubspot.horizon.apache.internal;

import com.hubspot.horizon.CustomDnsResolver;
import java.net.InetAddress;
import java.net.UnknownHostException;
import org.apache.http.conn.DnsResolver;

public class CustomApacheDnsResolver implements DnsResolver {

  private final CustomDnsResolver customDnsResolver;

  public CustomApacheDnsResolver(CustomDnsResolver customDnsResolver) {
    this.customDnsResolver = customDnsResolver;
  }

  @Override
  public InetAddress[] resolve(String host) throws UnknownHostException {
    return customDnsResolver.resolve(host).join();
  }
}
