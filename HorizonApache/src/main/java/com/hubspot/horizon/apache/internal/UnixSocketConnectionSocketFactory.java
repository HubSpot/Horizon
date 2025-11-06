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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UnixSocketConnectionSocketFactory extends PlainConnectionSocketFactory {

  private static final Logger LOG = LoggerFactory.getLogger(
    UnixSocketConnectionSocketFactory.class
  );

  private final File socketFile;

  public UnixSocketConnectionSocketFactory(File socketFile) {
    this.socketFile = socketFile;
  }

  public static UnixSocketConnectionSocketFactory forPath(String socketPath) {
    return new UnixSocketConnectionSocketFactory(new File(socketPath));
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
    LOG.info("Connecting to socket: {}", sock);
    try {
      sock.connect(AFUNIXSocketAddress.of(socketFile), connectTimeout);
    } catch (final IOException ex) {
      LOG.error("Failed to connect to Unix socket", ex);
      try {
        sock.close();
      } catch (final IOException ignore) {}
      throw ex;
    }
    LOG.info("Socket connected: {}", sock);
    LOG.info("Socket channel: {}", sock.getChannel());
    return sock;
  }
}
