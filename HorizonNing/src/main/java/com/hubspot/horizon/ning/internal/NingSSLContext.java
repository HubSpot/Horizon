package com.hubspot.horizon.ning.internal;

import javax.net.ssl.SSLException;

import org.asynchttpclient.shaded.io.netty.handler.ssl.SslContext;
import org.asynchttpclient.shaded.io.netty.handler.ssl.SslContextBuilder;
import org.asynchttpclient.shaded.io.netty.handler.ssl.SslProvider;
import org.asynchttpclient.shaded.io.netty.handler.ssl.util.InsecureTrustManagerFactory;

import com.hubspot.horizon.SSLConfig;

public final class NingSSLContext {
  // TLS 1.0 and TLS 1.1 are deprecated
  private static final String[] TLS_VERSIONS = new String[] {
    "TLSv1.2",
    "TLSv1.3"
  };

  private NingSSLContext() {
    throw new AssertionError();
  }

  public static SslContext forConfig(SSLConfig config) {
    try {
      SslContextBuilder builder = SslContextBuilder.forClient().sslProvider(SslProvider.JDK).protocols(TLS_VERSIONS);

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
