package com.hubspot.horizon;

import org.assertj.core.api.AbstractAssert;

import static org.assertj.core.api.Assertions.assertThat;

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

  public HttpResponseAssert hasBody(byte[] expected) {
    assertThat(actual.getAsBytes()).isEqualTo(expected);
    return this;
  }

  public HttpResponseAssert hasRetries(int expected) {
    assertThat(retryCount(actual)).isEqualTo(expected);
    return this;
  }

  private int retryCount(HttpResponse response) {
    return Integer.parseInt(response.getHeader("X-Request-Count")) - 1;
  }
}
