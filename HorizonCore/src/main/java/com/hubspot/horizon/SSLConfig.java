package com.hubspot.horizon;

import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import javax.annotation.Nullable;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

public class SSLConfig {

  private final KeyManagerFactory keyManagerFactory;
  private final TrustManagerFactory trustManagerFactory;
  private final boolean acceptAllSSL;

  private SSLConfig(
    @Nullable KeyManagerFactory keyManagerFactory,
    @Nullable TrustManagerFactory trustManagerFactory,
    boolean acceptAllSSL
  ) {
    this.keyManagerFactory = keyManagerFactory;
    this.trustManagerFactory = trustManagerFactory;
    this.acceptAllSSL = acceptAllSSL;
  }

  @Nullable
  public KeyManagerFactory getKeyManagerFactory() {
    return keyManagerFactory;
  }

  @Nullable
  public KeyManager[] getKeyManagers() {
    return keyManagerFactory == null ? null : keyManagerFactory.getKeyManagers();
  }

  @Nullable
  public TrustManagerFactory getTrustManagerFactory() {
    return trustManagerFactory;
  }

  @Nullable
  public TrustManager[] getTrustManagers() {
    return trustManagerFactory == null ? null : trustManagerFactory.getTrustManagers();
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

      KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(
        KeyManagerFactory.getDefaultAlgorithm()
      );
      keyManagerFactory.init(keyStore, password.toCharArray());

      TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(
        TrustManagerFactory.getDefaultAlgorithm()
      );
      trustManagerFactory.init(keyStore);

      return custom(keyManagerFactory, trustManagerFactory);
    } catch (GeneralSecurityException | IOException e) {
      throw new RuntimeException("Error initializing custom keystore", e);
    }
  }

  public static SSLConfig custom(
    KeyManagerFactory keyManagerFactory,
    TrustManagerFactory trustManagerFactory
  ) {
    return new SSLConfig(keyManagerFactory, trustManagerFactory, false);
  }
}
