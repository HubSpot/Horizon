package com.hubspot.horizon;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.net.HttpHeaders;

import javax.annotation.Nullable;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ExpectedHttpResponse {
  private final int statusCode;
  private final Map<String, List<String>> headers;
  private final String body;

  @JsonCreator
  private ExpectedHttpResponse(@JsonProperty("statusCode") int statusCode,
                               @JsonProperty("headers") Map<String, List<String>> headers,
                               @JsonProperty("body") @Nullable String body) {
    this.statusCode = statusCode;
    this.headers = headers;
    this.body = body;
  }

  public static Builder newBuilder() {
    return new Builder();
  }

  public int getStatusCode() {
    return statusCode;
  }

  public Map<String, List<String>> getHeaders() {
    return headers;
  }

  public @Nullable String getHeader(String name) {
    List<String> values = getHeaders().get(name);
    return values == null || values.isEmpty() ? null : values.get(0);
  }

  public @Nullable String getBody() {
    return body;
  }

  public static class Builder {
    private int statusCode = HttpServletResponse.SC_OK;
    private Map<String, List<String>> headers = new HashMap<String, List<String>>();
    private Compression compression = Compression.NONE;
    private String body = null;

    private Builder() { }

    public Builder setStatusCode(int statusCode) {
      this.statusCode = statusCode;
      return this;
    }

    public Builder setCompression(Compression compression) {
      this.compression = Preconditions.checkNotNull(compression);
      return this;
    }

    public Builder setBody(String body) {
      this.body = Preconditions.checkNotNull(body);
      return this;
    }

    public ExpectedHttpResponse build() {
      headers.put("X-Request-ID", Lists.newArrayList(UUID.randomUUID().toString()));

      Optional<String> contentEncoding = compression.getContentEncodingHeaderValue();
      if (contentEncoding.isPresent()) {
        headers.put(HttpHeaders.CONTENT_ENCODING, Lists.newArrayList(contentEncoding.get()));
      }

      return new ExpectedHttpResponse(statusCode, headers, body);
    }
  }
}
