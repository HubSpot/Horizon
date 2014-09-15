package com.hubspot.horizon;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

import javax.annotation.Nullable;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ExpectedHttpResponse {
  private final int statusCode;
  private final Map<String, List<String>> headers;
  private final byte[] body;

  @JsonCreator
  private ExpectedHttpResponse(@JsonProperty("statusCode") int statusCode,
                               @JsonProperty("headers") Map<String, List<String>> headers,
                               @JsonProperty("body") @Nullable byte[] body) {
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

  public @Nullable byte[] getBody() {
    return body;
  }

  public static class Builder {
    private int statusCode = HttpServletResponse.SC_OK;
    private Map<String, List<String>> headers = new HashMap<String, List<String>>();
    private byte[] body = null;

    private Builder() { }

    public Builder setStatusCode(int statusCode) {
      this.statusCode = statusCode;
      return this;
    }

    public Builder addHeader(String name, @Nullable String value) {
      Preconditions.checkNotNull(name);
      if (headers.containsKey(name)) {
        headers.get(name).add(value);
      } else {
        headers.put(name, Lists.newArrayList(value));
      }
      return this;
    }

    public Builder setBody(byte[] body) {
      this.body = Preconditions.checkNotNull(body);
      return this;
    }

    public ExpectedHttpResponse build() {
      headers.put("X-Request-ID", Lists.newArrayList(UUID.randomUUID().toString()));
      return new ExpectedHttpResponse(statusCode, headers, body);
    }
  }
}
