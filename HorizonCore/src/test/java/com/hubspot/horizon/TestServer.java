package com.hubspot.horizon;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Charsets;
import com.google.common.base.Preconditions;
import com.google.common.net.HttpHeaders;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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
import org.xerial.snappy.SnappyInputStream;
import org.xerial.snappy.SnappyOutputStream;

public class TestServer {

  private final Server server;
  private final NetworkConnector httpConnector;
  private final NetworkConnector httpsConnector;
  private final ObjectMapper mapper;
  private final ConcurrentMap<String, AtomicInteger> requestCounts;

  public TestServer() {
    this.server = new Server();
    this.httpConnector = buildHttpConnector(server);
    this.httpsConnector = buildHttpsConnector(server);
    this.mapper = new ObjectMapper();
    this.requestCounts = new ConcurrentHashMap<String, AtomicInteger>();
    server.setConnectors(new Connector[] { httpConnector, httpsConnector });
    server.setHandler(buildHandler());
  }

  public void start() throws Exception {
    server.start();
  }

  public String baseHttpUrl() {
    return "http://localhost:" + httpConnector.getLocalPort();
  }

  public String baseHttpsUrl() {
    return "https://localhost:" + httpsConnector.getLocalPort();
  }

  public void stop() throws Exception {
    server.stop();
  }

  private NetworkConnector buildHttpConnector(Server server) {
    return new ServerConnector(server);
  }

  private NetworkConnector buildHttpsConnector(Server server) {
    SslContextFactory sslContextFactory = new SslContextFactory();
    sslContextFactory.setKeyStorePath(getClass().getResource("/keystore").getPath());
    sslContextFactory.setTrustStorePath(getClass().getResource("/keystore").getPath());
    sslContextFactory.setKeyStorePassword("password");
    sslContextFactory.setTrustStorePassword("password");

    HttpConfiguration httpsConfiguration = new HttpConfiguration();
    httpsConfiguration.addCustomizer(new SecureRequestCustomizer());

    ConnectionFactory sslFactory = new SslConnectionFactory(
      sslContextFactory,
      HttpVersion.HTTP_1_1.asString()
    );
    ConnectionFactory httpsFactory = new HttpConnectionFactory(httpsConfiguration);

    return new ServerConnector(server, sslFactory, httpsFactory);
  }

  private Handler buildHandler() {
    return new AbstractHandler() {
      @Override
      public void handle(
        String target,
        Request baseRequest,
        HttpServletRequest request,
        HttpServletResponse response
      ) throws IOException {
        String inputEncoding = request.getHeader(HttpHeaders.CONTENT_ENCODING);
        final InputStream inputStream;
        if ("gzip".equals(inputEncoding)) {
          inputStream = new GZIPInputStream(request.getInputStream());
        } else if ("snappy".equals(inputEncoding)) {
          inputStream = new SnappyInputStream(request.getInputStream());
        } else {
          inputStream = request.getInputStream();
        }

        ExpectedHttpResponse expectedResponse = mapper.readValue(
          inputStream,
          ExpectedHttpResponse.class
        );

        response.setStatus(expectedResponse.getStatusCode());
        response.addHeader(
          "X-Request-Count",
          String.valueOf(incrementAndGetRequestCount(expectedResponse))
        );
        response.addHeader("Request-Content-Encoding", inputEncoding);
        response.addHeader(
          "Response-Content-Encoding",
          expectedResponse.getHeader(HttpHeaders.CONTENT_ENCODING)
        );
        for (Entry<String, List<String>> entry : expectedResponse
          .getHeaders()
          .entrySet()) {
          String name = entry.getKey();

          for (String value : entry.getValue()) {
            response.addHeader(name, value);
          }
        }
        baseRequest.setHandled(true);

        if (expectedResponse.getBody() != null) {
          String outputEncoding = expectedResponse.getHeader(
            HttpHeaders.CONTENT_ENCODING
          );
          final OutputStream outputStream;
          if ("gzip".equals(outputEncoding)) {
            outputStream = new GZIPOutputStream(response.getOutputStream());
          } else if ("snappy".equals(outputEncoding)) {
            outputStream = new SnappyOutputStream(response.getOutputStream());
          } else {
            outputStream = response.getOutputStream();
          }

          outputStream.write(expectedResponse.getBody().getBytes(Charsets.UTF_8));
          outputStream.close();
        }
      }
    };
  }

  private int incrementAndGetRequestCount(ExpectedHttpResponse request) {
    String requestId = Preconditions.checkNotNull(request.getHeader("X-Request-ID"));
    if (requestCounts.containsKey(requestId)) {
      return requestCounts.get(requestId).incrementAndGet();
    } else {
      AtomicInteger counter = requestCounts.putIfAbsent(requestId, new AtomicInteger(1));
      if (counter == null) {
        return 1;
      } else {
        return counter.incrementAndGet();
      }
    }
  }
}
