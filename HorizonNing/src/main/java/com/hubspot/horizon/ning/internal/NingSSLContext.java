package com.hubspot.horizon.ning.internal;

import com.hubspot.horizon.SSLConfig;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;

public final class NingSSLContext {

  private NingSSLContext() {
    throw new AssertionError();
  }

  public static SSLContext forConfig(SSLConfig config) {
    if (config.isAcceptAllSSL()) {
      return acceptAllSSLContext();
    } else {
      try {
        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(config.getKeyManagers(), config.getTrustManagers(), null);

        return sslContext;
      } catch (GeneralSecurityException e) {
        throw new RuntimeException(e);
      }
    }
  }

  private static SSLContext acceptAllSSLContext() {
    try {
      SSLContext sslcontext = SSLContext.getInstance("SSL");
      sslcontext.init(null, new TrustManager[]{ new AcceptAllTrustManager() }, new SecureRandom());

      return sslcontext;
    } catch (GeneralSecurityException e) {
      throw new RuntimeException(e);
    }
  }

  private static class AcceptAllTrustManager implements X509TrustManager {

    @Override
    public void checkClientTrusted(X509Certificate[] x509Certificates, String s) {
      // do nothing
    }

    @Override
    public void checkServerTrusted(X509Certificate[] x509Certificates, String s) {
      // do nothing
    }

    @Override
    public X509Certificate[] getAcceptedIssuers() {
      return new X509Certificate[0];
    }
  }
}
