package com.hubspot.horizon;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.base.Charsets;
import org.assertj.core.api.AbstractAssert;

public class HttpResponseAssert extends AbstractAssert<HttpResponseAssert, HttpResponse> {

  public HttpResponseAssert(HttpResponse response) {
    super(response, HttpResponseAssert.class);
  }

  public HttpResponseAssert hasStatusCode(int expected) {
    assertThat(actual.getStatusCode()).isEqualTo(expected);
    return this;
  }

  public HttpResponseAssert isSuccess() {
    assertThat(actual.isSuccess()).isTrue();
    return this;
  }

  public HttpResponseAssert isError() {
    assertThat(actual.isError()).isTrue();
    return this;
  }

  public HttpResponseAssert isClientError() {
    assertThat(actual.isClientError()).isTrue();
    return this;
  }

  public HttpResponseAssert isServerError() {
    assertThat(actual.isServerError()).isTrue();
    return this;
  }

  public HttpResponseAssert hasBody(String expected) {
    assertThat(actual.getAsBytes()).isEqualTo(expected.getBytes(Charsets.UTF_8));
    return this;
  }

  public HttpResponseAssert hasRetries(int expected) {
    assertThat(retryCount(actual)).isEqualTo(expected);
    return this;
  }

  public HttpResponseAssert wasGzipCompressed() {
    assertThat(actual.getHeaders().getFirst("Request-Content-Encoding"))
      .isEqualTo("gzip");
    return this;
  }

  public HttpResponseAssert wasSnappyCompressed() {
    assertThat(actual.getHeaders().getFirst("Request-Content-Encoding"))
      .isEqualTo("snappy");
    return this;
  }

  public HttpResponseAssert isGzipCompressed() {
    assertThat(actual.getHeaders().getFirst("Response-Content-Encoding"))
      .isEqualTo("gzip");
    return this;
  }

  public HttpResponseAssert isSnappyCompressed() {
    assertThat(actual.getHeaders().getFirst("Response-Content-Encoding"))
      .isEqualTo("snappy");
    return this;
  }

  private int retryCount(HttpResponse response) {
    return Integer.parseInt(response.getHeaders().getFirst("X-Request-Count")) - 1;
  }
}
