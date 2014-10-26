package com.hubspot.horizon;

import com.google.common.base.Throwables;

import javax.annotation.Nullable;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import java.io.InputStream;
import java.security.KeyStore;

public class SSLConfig {
  private final KeyManager[] keyManagers;
  private final TrustManager[] trustManagers;
  private final boolean acceptAllSSL;

  private SSLConfig(@Nullable KeyManager[] keyManagers, @Nullable TrustManager[] trustManagers, boolean acceptAllSSL) {
    this.keyManagers = keyManagers;
    this.trustManagers = trustManagers;
    this.acceptAllSSL = acceptAllSSL;
  }

  public @Nullable KeyManager[] getKeyManagers() {
    return keyManagers;
  }

  public @Nullable TrustManager[] getTrustManagers() {
    return trustManagers;
  }

  public boolean isAcceptAllSSL() {
    return acceptAllSSL;
  }

  public static SSLConfig standard() {
    return new SSLConfig(null, null, false);
  }

  public static SSLConfig acceptAll() {
    return new SSLConfig(null, null, true);
  }

  public static SSLConfig forKeyStore(InputStream keyStoreStream, String password) {
    try {
      KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
      keyStore.load(keyStoreStream, password.toCharArray());

      KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
      keyManagerFactory.init(keyStore, password.toCharArray());
      KeyManager[] keyManagers = keyManagerFactory.getKeyManagers();

      TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
      trustManagerFactory.init(keyStore);
      TrustManager[] trustManagers = trustManagerFactory.getTrustManagers();

      return custom(keyManagers, trustManagers);
    } catch (Exception e) {
      throw Throwables.propagate(e);
    }
  }

  public static SSLConfig custom(@Nullable KeyManager keyManager, @Nullable TrustManager trustManager) {
    return custom(new KeyManager[]{ keyManager }, new TrustManager[]{ trustManager });
  }

  public static SSLConfig custom(@Nullable KeyManager[] keyManagers, @Nullable TrustManager[] trustManagers) {
    return new SSLConfig(keyManagers, trustManagers, false);
  }
}
