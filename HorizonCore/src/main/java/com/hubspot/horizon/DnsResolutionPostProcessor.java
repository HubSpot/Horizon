package com.hubspot.horizon;

import java.net.InetAddress;
import java.util.List;

public interface DnsResolutionPostProcessor {
  InetAddress postResolve(InetAddress address);

  List<InetAddress> postResolveAll(List<InetAddress> addresses);
}
