package com.hubspot.horizon.apache.internal;

import org.apache.http.conn.ssl.AllowAllHostnameVerifier;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;

import java.security.GeneralSecurityException;
import java.security.cert.X509Certificate;

public class AcceptAllSSLSocketFactory extends SSLSocketFactory {

  private AcceptAllSSLSocketFactory() throws GeneralSecurityException {
    super(new TrustAllTrustStrategy(), new AllowAllHostnameVerifier());
  }

  public static AcceptAllSSLSocketFactory newInstance() {
    try {
      return new AcceptAllSSLSocketFactory();
    } catch (GeneralSecurityException e) {
      throw new RuntimeException(e);
    }
  }

  private static class TrustAllTrustStrategy implements TrustStrategy {

    @Override
    public boolean isTrusted(X509Certificate[] chain, String authType) {
      return true;
    }
  }
}
