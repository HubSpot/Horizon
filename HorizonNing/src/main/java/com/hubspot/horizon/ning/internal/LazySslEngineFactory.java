package com.hubspot.horizon.ning.internal;

import com.hubspot.horizon.SSLConfig;
import io.netty.buffer.ByteBufAllocator;
import io.netty.handler.ssl.SslContext;
import io.netty.util.ReferenceCountUtil;
import javax.net.ssl.SSLEngine;
import org.asynchttpclient.AsyncHttpClientConfig;
import org.asynchttpclient.netty.ssl.SslEngineFactoryBase;

/**
 * Builds the SslContext on the first https connection instead of at client construction,
 * so http-only clients never pay for JSSE/Netty SSL initialization. Engines match AHC's
 * DefaultSslEngineFactory given a preset SslContext.
 */
public final class LazySslEngineFactory extends SslEngineFactoryBase {

  private final SSLConfig sslConfig;
  private final Object lock = new Object();
  private volatile SslContext sslContext;
  // Memoized so every https request fails with the same error instead of rebuilding.
  private volatile RuntimeException failure;

  public LazySslEngineFactory(SSLConfig sslConfig) {
    this.sslConfig = sslConfig;
  }

  @Override
  public SSLEngine newSslEngine(
    AsyncHttpClientConfig config,
    String peerHost,
    int peerPort
  ) {
    SslContext context = sslContext();
    SSLEngine sslEngine = config.isDisableHttpsEndpointIdentificationAlgorithm()
      ? context.newEngine(ByteBufAllocator.DEFAULT)
      : context.newEngine(ByteBufAllocator.DEFAULT, domain(peerHost), peerPort);
    configureSslEngine(sslEngine, config);
    return sslEngine;
  }

  SslContext sslContext() {
    SslContext context = sslContext;
    if (context != null) {
      return context;
    }
    synchronized (lock) {
      if (sslContext == null && failure == null) {
        try {
          sslContext = NingSSLContext.forConfig(sslConfig);
        } catch (RuntimeException e) {
          failure = e;
        }
      }
      if (failure != null) {
        throw failure;
      }
      return sslContext;
    }
  }

  @Override
  public void destroy() {
    ReferenceCountUtil.release(sslContext);
  }
}
