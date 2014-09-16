package com.hubspot.horizon;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.net.HttpHeaders;
import com.google.common.primitives.Ints;
import org.apache.http.Header;
import org.apache.http.NameValuePair;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.message.BasicNameValuePair;

import javax.annotation.Nullable;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import static com.google.common.base.Charsets.UTF_8;

public class HttpRequest {

  public enum Method {
    GET(false), POST(true), PUT(true), DELETE(false), PATCH(true), HEAD(false);

    private final boolean allowsBody;

    private Method(boolean allowsBody) {
      this.allowsBody = allowsBody;
    }

    public boolean allowsBody() {
      return allowsBody;
    }
  }

  public enum ContentType {
    TEXT("text/plain; charset=UTF-8"),
    JSON("application/json"),
    XML("text/xml"),
    PROTOBUF("application/x-protobuf"),
    FORM("application/x-www-form-urlencoded"),
    CSV("text/csv; charset=UTF-8");

    private final String headerValue;

    ContentType(String headerValue) {
      this.headerValue = headerValue;
    }

    public String getHeaderValue() {
      return headerValue;
    }
  }

  private final Method method;
  private final URI url;
  private final Map<String, List<String>> headers;
  private final byte[] body;
  private final Options options;

  private HttpRequest(Method method, URI url, Map<String, List<String>> headers, @Nullable byte[] body, Options options) {
    this.method = Preconditions.checkNotNull(method);
    this.url = Preconditions.checkNotNull(url);
    this.headers = Preconditions.checkNotNull(headers);
    this.body = body;
    this.options = Preconditions.checkNotNull(options);
  }

  public static Builder newBuilder() {
    return new Builder();
  }

  public Method getMethod() {
    return method;
  }

  public URI getUrl() {
    return url;
  }

  public Map<String, List<String>> getHeaders() {
    return headers;
  }

  public @Nullable byte[] getBody() {
    return body;
  }

  public Options getOptions() {
    return options;
  }

  public static class Options {
    public static Options DEFAULT = new Options();

    private Optional<Integer> maxRetries = Optional.absent();
    private Optional<Integer> initialRetryBackoffSeconds = Optional.absent();
    private Optional<Integer> maxRetryBackoffSeconds = Optional.absent();
    private Optional<RetryStrategy> retryStrategy = Optional.absent();

    public int getMaxRetries() {
      return maxRetries.or(0);
    }

    public void setMaxRetries(int maxRetries) {
      this.maxRetries = Optional.of(maxRetries);
    }

    public int getInitialRetryBackoffMillis() {
      return Ints.checkedCast(TimeUnit.SECONDS.toMillis(initialRetryBackoffSeconds.or(1)));
    }

    public void setInitialRetryBackoffSeconds(int initialRetryBackoffSeconds) {
      this.initialRetryBackoffSeconds = Optional.of(initialRetryBackoffSeconds);
    }

    public int getMaxRetryBackoffMillis() {
      return Ints.checkedCast(TimeUnit.SECONDS.toMillis(maxRetryBackoffSeconds.or(30)));
    }

    public void setMaxRetryBackoffSeconds(int maxRetryBackoffSeconds) {
      this.maxRetryBackoffSeconds = Optional.of(maxRetryBackoffSeconds);
    }

    public RetryStrategy getRetryStrategy() {
      return retryStrategy.or(RetryStrategy.NEVER_RETRY);
    }

    public void setRetryStrategy(RetryStrategy retryStrategy) {
      this.retryStrategy = Optional.of(retryStrategy);
    }

    public Options mergeFrom(Options other) {
      Preconditions.checkNotNull(other);
      Options merged = new Options();

      merged.maxRetries = other.maxRetries.or(maxRetries);
      merged.initialRetryBackoffSeconds = other.initialRetryBackoffSeconds.or(initialRetryBackoffSeconds);
      merged.maxRetryBackoffSeconds = other.maxRetryBackoffSeconds.or(maxRetryBackoffSeconds);
      merged.retryStrategy = other.retryStrategy.or(retryStrategy);

      return merged;
    }
  }

  public static class Builder {
    private String url = null;
    private Method method = Method.GET;
    private final Map<String, List<String>> queryParams = new LinkedHashMap<String, List<String>>();
    private final Map<String, List<String>> headers = new LinkedHashMap<String, List<String>>();
    private byte[] body = null;
    private final Map<String, List<String>> formParams = new LinkedHashMap<String, List<String>>();
    private Compression compression = Compression.NONE;
    private ContentType contentType = null;
    private ContentType accept = null;
    private Options options = new Options();

    private Builder() { }

    public Builder setUrl(String url) {
      this.url = Preconditions.checkNotNull(url);
      return this;
    }

    public Builder setMethod(Method method) {
      this.method = Preconditions.checkNotNull(method);
      return this;
    }

    public Builder addQueryParam(String name, boolean value) {
      return addQueryParam(Preconditions.checkNotNull(name), String.valueOf(value));
    }

    public Builder addQueryParam(String name, int value) {
      return addQueryParam(Preconditions.checkNotNull(name), String.valueOf(value));
    }

    public Builder addQueryParam(String name, long value) {
      return addQueryParam(Preconditions.checkNotNull(name), String.valueOf(value));
    }

    public Builder addQueryParam(String name, @Nullable String value) {
      add(queryParams, name, value);
      return this;
    }

    public Builder addHeader(String name, @Nullable String value) {
      add(headers, name, value);
      return this;
    }

    public Builder setBody(Object body) {
      try {
        setBody(ObjectMapperHolder.INSTANCE.get().writeValueAsBytes(body));
        setContentType(ContentType.JSON);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
      return this;
    }

    public Builder setBody(String body) {
      return setBody(Preconditions.checkNotNull(body).getBytes(UTF_8));
    }

    public Builder setBody(byte[] body) {
      this.body = Preconditions.checkNotNull(body);
      return this;
    }

    public Builder setCompression(Compression compression) {
      this.compression = Preconditions.checkNotNull(compression);
      return this;
    }

    public Builder setContentType(ContentType contentType) {
      this.contentType = Preconditions.checkNotNull(contentType);
      return this;
    }

    public Builder setAccept(ContentType accept) {
      this.accept = Preconditions.checkNotNull(accept);
      return this;
    }

    public Builder addBasicAuth(String user, @Nullable String password) {
      Preconditions.checkNotNull(user);
      Header header = BasicScheme.authenticate(new UsernamePasswordCredentials(user, password), UTF_8.name(), false);
      addHeader(header.getName(), header.getValue());
      return this;
    }

    public Builder setMaxRetries(int maxRetries) {
      options.setMaxRetries(maxRetries);
      return this;
    }

    public Builder setInitialRetryBackoffSeconds(int initialRetryBackoffSeconds) {
      options.setInitialRetryBackoffSeconds(initialRetryBackoffSeconds);
      return this;
    }

    public Builder setMaxRetryBackoffSeconds(int maxRetryBackoffSeconds) {
      options.setMaxRetryBackoffSeconds(maxRetryBackoffSeconds);
      return this;
    }

    public Builder setRetryStrategy(RetryStrategy retryStrategy) {
      options.setRetryStrategy(Preconditions.checkNotNull(retryStrategy));
      return this;
    }

    public HttpRequest build() {
      URI url = buildUrl();
      byte[] body = buildBody();
      Map<String, List<String>> headers = buildHeaders();

      return new HttpRequest(method, url, headers, body, options);
    }

    private URI buildUrl() {
      Preconditions.checkNotNull(url, "URL is not set");

      if (queryParams.isEmpty()) {
        return URI.create(url);
      } else {
        char separator = url.contains("?") ? '&' : '?';
        return URI.create(url + separator + urlEncode(queryParams));
      }
    }

    private @Nullable byte[] buildBody() {
      if (body == null && formParams.isEmpty()) {
        return null;
      }

      Preconditions.checkArgument(body == null || formParams.isEmpty(), "Cannot set body and form params");
      Preconditions.checkArgument(method.allowsBody(), "Cannot set body with method " + method);

      final byte[] body;
      if (formParams.isEmpty()) {
        body = this.body;
      } else {
        body = urlEncode(formParams).getBytes(UTF_8);
        if (contentType == null) {
          contentType = ContentType.FORM;
        }
      }

      return compression.compress(body);
    }

    private Map<String, List<String>> buildHeaders() {
      Optional<String> contentEncodingHeaderValue = compression.getContentEncodingHeaderValue();
      if (contentEncodingHeaderValue.isPresent()) {
        headers.put(HttpHeaders.CONTENT_ENCODING, Collections.singletonList(contentEncodingHeaderValue.get()));
      }
      if (contentType != null) {
        headers.put(HttpHeaders.CONTENT_TYPE, Collections.singletonList(contentType.getHeaderValue()));
      }
      if (accept != null) {
        headers.put(HttpHeaders.ACCEPT, Collections.singletonList(accept.getHeaderValue()));
      }

      return headers;
    }

    private static void add(Map<String, List<String>> parameters, String name, @Nullable String value) {
      Preconditions.checkNotNull(name);
      if (parameters.containsKey(name)) {
        parameters.get(name).add(value);
      } else {
        parameters.put(name, Lists.newArrayList(value));
      }
    }

    private static String urlEncode(Map<String, List<String>> parameters) {
      return URLEncodedUtils.format(toNameValuePairs(parameters), UTF_8);
    }

    private static List<NameValuePair> toNameValuePairs(Map<String, List<String>> parameters) {
      List<NameValuePair> pairs = new ArrayList<NameValuePair>();
      for (Entry<String, List<String>> entry : parameters.entrySet()) {
        String name = entry.getKey();
        for (String value : entry.getValue()) {
          pairs.add(new BasicNameValuePair(name, value));
        }
      }

      return pairs;
    }
  }
}
