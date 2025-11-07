package com.hubspot.horizon.apache.internal;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import org.apache.http.HttpHost;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.protocol.HttpContext;
import org.newsclub.net.unix.AFUNIXSocket;
import org.newsclub.net.unix.AFUNIXSocketAddress;

public class UnixSocketConnectionSocketFactory extends PlainConnectionSocketFactory {

  public static final UnixSocketConnectionSocketFactory INSTANCE =
    new UnixSocketConnectionSocketFactory();

  public static UnixSocketConnectionSocketFactory getSocketFactory() {
    return INSTANCE;
  }

  @Override
  public Socket createSocket(final HttpContext context) throws IOException {
    return AFUNIXSocket.newInstance();
  }

  @Override
  public Socket connectSocket(
    int connectTimeout,
    Socket socket,
    HttpHost host,
    InetSocketAddress remoteAddress,
    InetSocketAddress localAddress,
    HttpContext context
  ) throws IOException {
    final Socket sock = socket != null ? socket : createSocket(context);
    try {
      File socketFile = (File) context.getAttribute("unix.socket.file");
      sock.connect(AFUNIXSocketAddress.of(socketFile), connectTimeout);
    } catch (final IOException ex) {
      try {
        sock.close();
      } catch (final IOException ignore) {}
      throw ex;
    }
    return sock;
  }
}
