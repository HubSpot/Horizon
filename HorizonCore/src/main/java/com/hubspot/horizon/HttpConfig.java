package com.hubspot.horizon;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Preconditions;
import com.google.common.primitives.Ints;
import com.hubspot.horizon.HttpRequest.Options;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

public class HttpConfig {
  private final int maxConnections;
  private final int maxConnectionsPerHost;
  private final int connectTimeoutSeconds;
  private final int requestTimeoutSeconds;
  private final int defaultKeepAliveSeconds;
  private final int connectionTtlSeconds;
  private final int maxRedirects;
  private final String userAgent;
  private final boolean followRedirects;
  private final boolean rejectRelativeRedirects;
  private final int maxRetries;
  private final int initialRetryBackoffSeconds;
  private final int maxRetryBackoffSeconds;
  private final RetryStrategy retryStrategy;
  private final ObjectMapper mapper;
  private final SSLConfig sslConfig;
  private final Optional<String> socksProxyHost;
  private final int socksProxyPort;

  private HttpConfig(int maxConnections,
                     int maxConnectionsPerHost,
                     int connectTimeoutSeconds,
                     int requestTimeoutSeconds,
                     int defaultKeepAliveSeconds,
                     int connectionTtlSeconds,
                     int maxRedirects,
                     String userAgent,
                     boolean followRedirects,
                     boolean rejectRelativeRedirects,
                     int maxRetries,
                     int initialRetryBackoffSeconds,
                     int maxRetryBackoffSeconds,
                     RetryStrategy retryStrategy,
                     ObjectMapper mapper,
                     SSLConfig sslConfig,
                     Optional<String> socksProxyHost,
                     int socksProxyPort) {
    this.maxConnections = maxConnections;
    this.maxConnectionsPerHost = maxConnectionsPerHost;
    this.connectTimeoutSeconds = connectTimeoutSeconds;
    this.requestTimeoutSeconds = requestTimeoutSeconds;
    this.defaultKeepAliveSeconds = defaultKeepAliveSeconds;
    this.connectionTtlSeconds = connectionTtlSeconds;
    this.maxRedirects = maxRedirects;
    this.userAgent = userAgent;
    this.followRedirects = followRedirects;
    this.rejectRelativeRedirects = rejectRelativeRedirects;
    this.maxRetries = maxRetries;
    this.initialRetryBackoffSeconds = initialRetryBackoffSeconds;
    this.maxRetryBackoffSeconds = maxRetryBackoffSeconds;
    this.retryStrategy = retryStrategy;
    this.mapper = mapper;
    this.sslConfig = sslConfig;
    this.socksProxyHost = socksProxyHost;
    this.socksProxyPort = socksProxyPort;
  }

  public static Builder newBuilder() {
    return new Builder();
  }

  public int getMaxConnections() {
    return maxConnections;
  }

  public int getMaxConnectionsPerHost() {
    return maxConnectionsPerHost;
  }

  public int getConnectTimeoutMillis() {
    return Ints.checkedCast(TimeUnit.SECONDS.toMillis(connectTimeoutSeconds));
  }

  public int getRequestTimeoutMillis() {
    return Ints.checkedCast(TimeUnit.SECONDS.toMillis(requestTimeoutSeconds));
  }

  public int getDefaultKeepAliveMillis() {
    return Ints.checkedCast(TimeUnit.SECONDS.toMillis(defaultKeepAliveSeconds));
  }

  public int getConnectionTtlMillis() {
    return Ints.checkedCast(TimeUnit.SECONDS.toMillis(connectionTtlSeconds));
  }

  public int getMaxRedirects() {
    return maxRedirects;
  }

  public String getUserAgent() {
    return userAgent;
  }

  public boolean isFollowRedirects() {
    return followRedirects;
  }

  public boolean isRejectRelativeRedirects() {
    return rejectRelativeRedirects;
  }

  public ObjectMapper getObjectMapper() {
    return mapper;
  }

  public SSLConfig getSSLConfig() {
    return sslConfig;
  }

  public Optional<String> getSocksProxyHost() {
    return socksProxyHost;
  }

  public boolean isSocksProxied() {
    return getSocksProxyHost().isPresent();
  }

  public int getSocksProxyPort() {
    return socksProxyPort;
  }

  public Options getOptions() {
    Options options = new Options();

    options.setMaxRetries(maxRetries);
    options.setInitialRetryBackoffSeconds(initialRetryBackoffSeconds);
    options.setMaxRetryBackoffSeconds(maxRetryBackoffSeconds);
    options.setRetryStrategy(retryStrategy);

    return options;
  }

  public static class Builder {
    private int maxConnections = 100;
    private int maxConnectionsPerHost = 25;
    private int connectTimeoutSeconds = 1;
    private int requestTimeoutSeconds = 30;
    private int defaultKeepAliveSeconds = 10;
    private int connectionTtlSeconds = -1;
    private int maxRedirects = 10;
    private String userAgent = "Horizon/0.0.1";
    private boolean followRedirects = true;
    private boolean rejectRelativeRedirects;
    private int maxRetries = 3;
    private int initialRetryBackoffSeconds = 1;
    private int maxRetryBackoffSeconds = 30;
    private RetryStrategy retryStrategy = RetryStrategy.DEFAULT;
    private ObjectMapper mapper = new ObjectMapper();
    private SSLConfig sslConfig = SSLConfig.standard();
    private Optional<String> socksProxyHost = Optional.empty();
    private int socksProxyPort = 1080;

    private Builder() { }

    public Builder setMaxConnections(int maxConnections) {
      this.maxConnections = maxConnections;
      return this;
    }

    public Builder setMaxConnectionsPerHost(int maxConnectionsPerHost) {
      this.maxConnectionsPerHost = maxConnectionsPerHost;
      return this;
    }

    public Builder setConnectTimeoutSeconds(int connectTimeoutSeconds) {
      this.connectTimeoutSeconds = connectTimeoutSeconds;
      return this;
    }

    public Builder setRequestTimeoutSeconds(int requestTimeoutSeconds) {
      this.requestTimeoutSeconds = requestTimeoutSeconds;
      return this;
    }

    public Builder setDefaultKeepAliveSeconds(int defaultKeepAliveSeconds) {
      this.defaultKeepAliveSeconds = defaultKeepAliveSeconds;
      return this;
    }

    public Builder setConnectionTtlSeconds(int connectionTtlSeconds) {
      this.connectionTtlSeconds = connectionTtlSeconds;
      return this;
    }

    public Builder setMaxRedirects(int maxRedirects) {
      this.maxRedirects = maxRedirects;
      return this;
    }

    public Builder setUserAgent(String userAgent) {
      this.userAgent = Preconditions.checkNotNull(userAgent);
      return this;
    }

    public Builder setFollowRedirects(boolean followRedirects) {
      this.followRedirects = followRedirects;
      return this;
    }

    public Builder setRejectRelativeRedirects(boolean rejectRelativeRedirects) {
      this.rejectRelativeRedirects = rejectRelativeRedirects;
      return this;
    }

    public Builder setMaxRetries(int maxRetries) {
      this.maxRetries = maxRetries;
      return this;
    }

    public Builder setInitialRetryBackoffSeconds(int initialRetryBackoffSeconds) {
      this.initialRetryBackoffSeconds = initialRetryBackoffSeconds;
      return this;
    }

    public Builder setMaxRetryBackoffSeconds(int maxRetryBackoffSeconds) {
      this.maxRetryBackoffSeconds = maxRetryBackoffSeconds;
      return this;
    }

    public Builder setRetryStrategy(RetryStrategy retryStrategy) {
      this.retryStrategy = Preconditions.checkNotNull(retryStrategy);
      return this;
    }

    public Builder setObjectMapper(ObjectMapper mapper) {
      this.mapper = Preconditions.checkNotNull(mapper);
      return this;
    }

    public Builder setSSLConfig(SSLConfig sslConfig) {
      this.sslConfig = Preconditions.checkNotNull(sslConfig);
      return this;
    }

    public Builder setSocksProxyHost(String socksProxyHost) {
      this.socksProxyHost = Optional.of(socksProxyHost);
      return this;
    }

    public Builder setSocksProxyPort(int socksProxyPort) {
      this.socksProxyPort = socksProxyPort;
      return this;
    }

    public HttpConfig build() {
      return new HttpConfig(maxConnections,
              maxConnectionsPerHost,
              connectTimeoutSeconds,
              requestTimeoutSeconds,
              defaultKeepAliveSeconds,
              connectionTtlSeconds,
              maxRedirects,
              userAgent,
              followRedirects,
              rejectRelativeRedirects,
              maxRetries,
              initialRetryBackoffSeconds,
              maxRetryBackoffSeconds,
              retryStrategy,
              mapper,
              sslConfig,
              socksProxyHost,
              socksProxyPort);
    }
  }
}
