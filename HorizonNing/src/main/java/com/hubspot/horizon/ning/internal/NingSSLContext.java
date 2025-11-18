package com.hubspot.horizon.ning.internal;

import com.hubspot.horizon.SSLConfig;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.SslProvider;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import javax.net.ssl.SSLException;

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
