package com.hubspot.horizon.apache.internal;

import com.hubspot.horizon.SSLConfig;
import org.apache.http.conn.ssl.AllowAllHostnameVerifier;
import org.apache.http.conn.ssl.BrowserCompatHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContexts;
import org.apache.http.conn.ssl.TrustStrategy;

import javax.net.ssl.SSLContext;
import java.security.GeneralSecurityException;
import java.security.cert.X509Certificate;


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

    return new SSLConnectionSocketFactory(sslContext, new AllowAllHostnameVerifier());
  }

  private static SSLConnectionSocketFactory standardSSLSocketFactory(SSLConfig config) throws GeneralSecurityException {
    SSLContext sslContext = SSLContext.getInstance("TLS");
    sslContext.init(config.getKeyManagers(), config.getTrustManagers(), null);

    return new SSLConnectionSocketFactory(sslContext, new BrowserCompatHostnameVerifier());
  }

  private static class TrustAllTrustStrategy implements TrustStrategy {

    @Override
    public boolean isTrusted(X509Certificate[] chain, String authType) {
      return true;
    }
  }
}
