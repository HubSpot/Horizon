package com.hubspot.horizon;

import java.net.InetAddress;
import java.net.UnknownHostException;

public interface CustomDnsResolver {
  InetAddress[] resolve(String host) throws UnknownHostException;
}
