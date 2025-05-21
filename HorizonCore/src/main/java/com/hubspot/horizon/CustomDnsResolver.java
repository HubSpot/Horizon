package com.hubspot.horizon;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.CompletableFuture;

public interface CustomDnsResolver {
  CompletableFuture<InetAddress[]> resolve(String host) throws UnknownHostException;
}
