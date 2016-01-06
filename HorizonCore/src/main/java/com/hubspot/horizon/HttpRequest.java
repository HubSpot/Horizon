package com.hubspot.horizon;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.net.HttpHeaders;
import com.google.common.primitives.Ints;
import com.hubspot.horizon.internal.ParameterSetterImpl;
import org.apache.http.NameValuePair;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.message.BasicNameValuePair;

import javax.annotation.Nullable;
import java.net.URI;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import static com.google.common.base.Charsets.UTF_8;

public class HttpRequest {

  public interface ParameterSetter {
    Builder to(Iterable<String> value);
    Builder to(String... value);
    Builder to(boolean... value);
    Builder to(int... value);
    Builder to(long... value);
  }

  public enum Method {
    GET(false), POST(true), PUT(true), DELETE(true), PATCH(true), HEAD(false);

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
    CSV("text/csv; charset=UTF-8"),
    OCTET_STREAM("application/octet-stream");

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
  private final Headers headers;
  private final Compression compression;
  private final byte[] body;
  private final Object jsonBody;
  private final Options options;

  private HttpRequest(Method method,
                      URI url,
                      Headers headers,
                      Compression compression,
                      @Nullable byte[] body,
                      @Nullable Object jsonBody,
                      Options options) {
    this.method = Preconditions.checkNotNull(method);
    this.url = Preconditions.checkNotNull(url);
    this.headers = Preconditions.checkNotNull(headers);
    this.compression = compression;
    this.body = body;
    this.jsonBody = jsonBody;
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

  public Headers getHeaders() {
    return headers;
  }

  @Nullable
  public byte[] getBody(ObjectMapper mapper) {
    if (body != null) {
      return compression.compress(body);
    } else if (jsonBody != null) {
      try {
        return compression.compress(mapper.writeValueAsBytes(jsonBody));
      } catch (JsonProcessingException e) {
        throw new RuntimeException(e);
      }
    } else {
      return null;
    }
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
    private final Map<String, List<String>> queryParams = new LinkedHashMap<>();
    private final List<Header> headers = new ArrayList<>();
    private byte[] body = null;
    private Object jsonBody = null;
    private final Map<String, List<String>> formParams = new LinkedHashMap<>();
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

    public Builder addHeader(String name, String value) {
      headers.add(new Header(name, value));
      return this;
    }

    public ParameterSetter setQueryParam(String name) {
      return new ParameterSetterImpl(Preconditions.checkNotNull(name), this, queryParams);
    }

    public ParameterSetter setFormParam(String name) {
      setContentType(ContentType.FORM);
      return new ParameterSetterImpl(Preconditions.checkNotNull(name), this, formParams);
    }

    public Builder setBody(Object jsonBody) {
      this.jsonBody = Preconditions.checkNotNull(jsonBody);
      setContentType(ContentType.JSON);
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
      Credentials credentials = new UsernamePasswordCredentials(user, password);
      org.apache.http.Header header = BasicScheme.authenticate(credentials, UTF_8.name(), false);
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
      Headers headers = buildHeaders();
      validateBodyState();

      return new HttpRequest(method, url, headers, compression, body, jsonBody, options);
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

    private void validateBodyState() {
      if (body == null && jsonBody == null && formParams.isEmpty()) {
        return;
      }

      Preconditions.checkState(method.allowsBody(), "Cannot set body with method " + method);

      if (body != null) {
        Preconditions.checkState(jsonBody == null && formParams.isEmpty(), "Cannot set more than one body");
      } else if (jsonBody != null) {
        Preconditions.checkState(formParams.isEmpty(), "Cannot set more than one body");
      } else {
        body = urlEncode(formParams).getBytes(UTF_8);
      }
    }

    private Headers buildHeaders() {
      Optional<String> contentEncodingHeaderValue = compression.getContentEncodingHeaderValue();
      if (contentEncodingHeaderValue.isPresent() && !headerPresent(HttpHeaders.CONTENT_ENCODING)) {
        headers.add(new Header(HttpHeaders.CONTENT_ENCODING, contentEncodingHeaderValue.get()));
      }
      if (contentType != null && !headerPresent(HttpHeaders.CONTENT_TYPE)) {
        headers.add(new Header(HttpHeaders.CONTENT_TYPE, contentType.getHeaderValue()));
      }
      if (accept != null && !headerPresent(HttpHeaders.ACCEPT)) {
        headers.add(new Header(HttpHeaders.ACCEPT, accept.getHeaderValue()));
      }

      return new Headers(headers);
    }

    private boolean headerPresent(String headerName) {
      for (Header header : headers) {
        if (header.getName().equalsIgnoreCase(headerName)) {
          return true;
        }
      }

      return false;
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
