package com.hubspot.horizon.apache.internal;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Socket;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.protocol.HttpContext;

public class ProxiedPlainConnectionSocketFactory extends PlainConnectionSocketFactory {

  public static final ProxiedPlainConnectionSocketFactory INSTANCE = new ProxiedPlainConnectionSocketFactory();

  public static ProxiedPlainConnectionSocketFactory getSocketFactory() {
    return INSTANCE;
  }

  @Override
  public Socket createSocket(final HttpContext context) throws IOException {
    InetSocketAddress socksaddr = (InetSocketAddress) context.getAttribute(
      "socks.address"
    );
    Proxy proxy = new Proxy(Proxy.Type.SOCKS, socksaddr);
    return new Socket(proxy);
  }
}
