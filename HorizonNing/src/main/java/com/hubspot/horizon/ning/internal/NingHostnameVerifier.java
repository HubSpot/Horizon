package com.hubspot.horizon.ning.internal;

import com.hubspot.horizon.HttpConfig;
import org.apache.http.conn.ssl.AllowAllHostnameVerifier;
import org.apache.http.conn.ssl.BrowserCompatHostnameVerifier;

import javax.net.ssl.HostnameVerifier;

public final class NingHostnameVerifier {

  private NingHostnameVerifier() {
    throw new AssertionError();
  }

  public static HostnameVerifier forConfig(HttpConfig config) {
    return config.isAcceptAllSSL() ? new AllowAllHostnameVerifier() : new BrowserCompatHostnameVerifier();
  }
}
