package com.hubspot.horizon;

import com.google.common.base.Throwables;
import com.hubspot.horizon.HttpRequest.ContentType;
import org.assertj.core.util.Closeables;
import org.eclipse.jetty.http.HttpVersion;
import org.eclipse.jetty.server.ConnectionFactory;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.NetworkConnector;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.SecureRequestCustomizer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.SslConnectionFactory;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jetty.util.ssl.SslContextFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;

public class TestServer {
  private final Server server;
  private final NetworkConnector httpConnector;
  private final NetworkConnector httpsConnector;

  public TestServer() {
    this.server = new Server();
    this.httpConnector = buildHttpConnector(server);
    this.httpsConnector = buildHttpsConnector(server);
    server.setConnectors(new Connector[] { httpConnector, httpsConnector });
    server.setHandler(buildHandler());
  }

  public void start() throws Exception {
    server.start();
  }

  public int getHttpPort() {
    return httpConnector.getLocalPort();
  }

  public int getHttpsPort() {
    return httpsConnector.getLocalPort();
  }

  public void stop() throws Exception {
    server.stop();
  }

  private NetworkConnector buildHttpConnector(Server server) {
    return new ServerConnector(server);
  }

  private NetworkConnector buildHttpsConnector(Server server) {
    SslContextFactory sslContextFactory = new SslContextFactory();
    sslContextFactory.setKeyStore(buildKeyOrTrustStore());
    sslContextFactory.setTrustStore(buildKeyOrTrustStore());
    sslContextFactory.setKeyStorePassword("password");
    sslContextFactory.setTrustStorePassword("password");

    HttpConfiguration httpsConfiguration = new HttpConfiguration();
    httpsConfiguration.addCustomizer(new SecureRequestCustomizer());

    ConnectionFactory sslFactory = new SslConnectionFactory(sslContextFactory, HttpVersion.HTTP_1_1.asString());
    ConnectionFactory httpsFactory = new HttpConnectionFactory(httpsConfiguration);

    return new ServerConnector(server, sslFactory, httpsFactory);
  }

  private KeyStore buildKeyOrTrustStore() {
    InputStream inputStream = getClass().getResourceAsStream("/keystore");
    try {
      KeyStore keyStore = KeyStore.getInstance("JKS");
      keyStore.load(inputStream, "password".toCharArray());

      return keyStore;
    } catch (Exception e) {
      throw Throwables.propagate(e);
    } finally {
      Closeables.closeQuietly(inputStream);
    }
  }

  private Handler buildHandler() {
    return new AbstractHandler() {
      @Override
      public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType(ContentType.TEXT.getHeaderValue());
        response.setStatus(HttpServletResponse.SC_OK);
        baseRequest.setHandled(true);

        response.getWriter().print("<h1>Hello World</h1>");
      }
    };
  }
}
