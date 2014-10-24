package com.hubspot.horizon.apache.internal;

import com.hubspot.horizon.SSLConfig;
import org.apache.http.conn.ssl.AllowAllHostnameVerifier;
import org.apache.http.conn.ssl.BrowserCompatHostnameVerifier;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;

import javax.net.ssl.SSLContext;
import java.security.GeneralSecurityException;
import java.security.cert.X509Certificate;


public class ApacheSSLSocketFactory {

  public static SSLSocketFactory forConfig(SSLConfig config) {
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

  private static SSLSocketFactory acceptAllSSLSocketFactory() throws GeneralSecurityException {
    return new SSLSocketFactory(new TrustAllTrustStrategy(), new AllowAllHostnameVerifier());
  }

  private static SSLSocketFactory standardSSLSocketFactory(SSLConfig config) throws GeneralSecurityException {
    SSLContext sslContext = SSLContext.getInstance("TLS");
    sslContext.init(config.getKeyManagers(), config.getTrustManagers(), null);

    return new SSLSocketFactory(sslContext, new BrowserCompatHostnameVerifier());
  }

  private static class TrustAllTrustStrategy implements TrustStrategy {

    @Override
    public boolean isTrusted(X509Certificate[] chain, String authType) {
      return true;
    }
  }
}
