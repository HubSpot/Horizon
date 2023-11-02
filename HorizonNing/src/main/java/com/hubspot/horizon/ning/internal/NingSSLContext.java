package com.hubspot.horizon.ning.internal;

import com.hubspot.horizon.SSLConfig;
import javax.net.ssl.SSLException;
import org.asynchttpclient.shaded.io.netty.handler.ssl.SslContext;
import org.asynchttpclient.shaded.io.netty.handler.ssl.SslContextBuilder;
import org.asynchttpclient.shaded.io.netty.handler.ssl.SslProvider;
import org.asynchttpclient.shaded.io.netty.handler.ssl.util.InsecureTrustManagerFactory;

public final class NingSSLContext {

  private NingSSLContext() {
    throw new AssertionError();
  }

  public static SslContext forConfig(SSLConfig config) {
    try {
      SslContextBuilder builder = SslContextBuilder
        .forClient()
        .sslProvider(SslProvider.JDK);

      if (config.isAcceptAllSSL()) {
        builder.trustManager(InsecureTrustManagerFactory.INSTANCE);
      } else {
        builder.keyManager(config.getKeyManagerFactory());
        builder.trustManager(config.getTrustManagerFactory());
      }

      return builder.build();
    } catch (SSLException e) {
      throw new RuntimeException(e);
    }
  }
}
