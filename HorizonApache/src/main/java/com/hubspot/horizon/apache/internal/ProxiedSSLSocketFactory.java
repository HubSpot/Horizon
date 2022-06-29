package com.hubspot.horizon.apache.internal;

import com.hubspot.horizon.SSLConfig;
import org.apache.http.conn.ssl.DefaultHostnameVerifier;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.protocol.HttpContext;
import org.apache.http.ssl.SSLContexts;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Socket;
import java.security.GeneralSecurityException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

public class ProxiedSSLSocketFactory {

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
    SSLContext sslContext = SSLContexts.custom().loadTrustMaterial(null, new TrustStrategy() {

      @Override
      public boolean isTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
        return true;
      }
    }).build();

    return new SocksConnectionSocketFactory(sslContext, new NoopHostnameVerifier());
  }

  private static SSLConnectionSocketFactory standardSSLSocketFactory(SSLConfig config) throws GeneralSecurityException {
    SSLContext sslContext = SSLContext.getInstance("TLS");
    sslContext.init(config.getKeyManagers(), config.getTrustManagers(), null);

    return new SocksConnectionSocketFactory(sslContext, new DefaultHostnameVerifier());
  }

  private static class SocksConnectionSocketFactory extends SSLConnectionSocketFactory {
    public SocksConnectionSocketFactory(SSLContext sslContext, HostnameVerifier hostnameVerifier) {
      super(sslContext, hostnameVerifier);
    }

    @Override
    public Socket createSocket(final HttpContext context) throws IOException {
      InetSocketAddress socksaddr = (InetSocketAddress) context.getAttribute("socks.address");
      Proxy proxy = new Proxy(Proxy.Type.SOCKS, socksaddr);
      return new Socket(proxy);
    }
  }
}
