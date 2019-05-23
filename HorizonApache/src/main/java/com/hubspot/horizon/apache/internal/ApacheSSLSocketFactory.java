package com.hubspot.horizon.apache.internal;

import java.security.GeneralSecurityException;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;

import org.apache.http.conn.ssl.DefaultHostnameVerifier;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.ssl.SSLContexts;

import com.hubspot.horizon.SSLConfig;

public class ApacheSSLSocketFactory {

  public static SSLConnectionSocketFactory forConfig(SSLConfig config) {
    try {
      if (config.isAcceptAllSSL()) {
        return acceptAllSSLSocketFactory();
      } else {
        return standardSSLSocketFactory(config);
      }
    } catch (GeneralSecurityException e) {
      throw new RuntimeException(e);
    }
  }

  private static SSLConnectionSocketFactory acceptAllSSLSocketFactory() throws GeneralSecurityException {
    SSLContext sslContext = SSLContexts.custom().loadTrustMaterial(null, new TrustAllTrustStrategy()).build();

    return new SSLConnectionSocketFactory(sslContext, new NoopHostnameVerifier());
  }

  private static SSLConnectionSocketFactory standardSSLSocketFactory(SSLConfig config) throws GeneralSecurityException {
    SSLContext sslContext = SSLContext.getInstance("TLS");
    sslContext.init(config.getKeyManagers(), config.getTrustManagers(), null);

    return new SSLConnectionSocketFactory(sslContext, new DefaultHostnameVerifier());
  }

  private static class TrustAllTrustStrategy implements TrustStrategy {

    @Override
    public boolean isTrusted(X509Certificate[] chain, String authType) {
      return true;
    }
  }
}
