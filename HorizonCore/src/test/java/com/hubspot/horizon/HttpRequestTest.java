package com.hubspot.horizon;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.net.HttpHeaders;
import com.hubspot.horizon.HttpRequest.ContentType;
import com.hubspot.horizon.HttpRequest.Method;
import java.nio.charset.StandardCharsets;
import org.junit.Test;

public class HttpRequestTest {

  @Test
  public void itDoesNotAccumulateContentTypeHeaderOnRebuild() {
    HttpRequest.Builder builder = HttpRequest
      .newBuilder()
      .setUrl("http://example.com")
      .setMethod(Method.POST)
      .setContentType(ContentType.JSON);

    HttpRequest first = builder.build();
    HttpRequest second = builder.build().toBuilder().build();

    assertThat(first.getHeaders().get(HttpHeaders.CONTENT_TYPE)).hasSize(1);
    assertThat(second.getHeaders().get(HttpHeaders.CONTENT_TYPE))
      .isEqualTo(first.getHeaders().get(HttpHeaders.CONTENT_TYPE));
  }

  @Test
  public void itDoesNotAccumulateAcceptHeaderOnRebuild() {
    HttpRequest.Builder builder = HttpRequest
      .newBuilder()
      .setUrl("http://example.com")
      .setAccept(ContentType.JSON);

    HttpRequest first = builder.build();
    HttpRequest second = builder.build().toBuilder().build();

    assertThat(first.getHeaders().get(HttpHeaders.ACCEPT)).hasSize(1);
    assertThat(second.getHeaders().get(HttpHeaders.ACCEPT))
      .isEqualTo(first.getHeaders().get(HttpHeaders.ACCEPT));
  }

  @Test
  public void itDoesNotAccumulateContentEncodingHeaderOnRebuild() {
    HttpRequest.Builder builder = HttpRequest
      .newBuilder()
      .setUrl("http://example.com")
      .setMethod(Method.POST)
      .setBody("body")
      .setCompression(Compression.GZIP);

    HttpRequest first = builder.build();
    HttpRequest second = builder.build().toBuilder().build();

    assertThat(first.getHeaders().get(HttpHeaders.CONTENT_ENCODING)).hasSize(1);
    assertThat(second.getHeaders().get(HttpHeaders.CONTENT_ENCODING))
      .isEqualTo(first.getHeaders().get(HttpHeaders.CONTENT_ENCODING));
  }

  @Test
  public void itDoesNotAccumulateJsonBodyContentTypeHeaderOnRebuild() {
    // setBody(Object) internally calls setContentType(JSON)
    HttpRequest.Builder builder = HttpRequest
      .newBuilder()
      .setUrl("http://example.com")
      .setMethod(Method.POST)
      .setBody(new Object());

    HttpRequest first = builder.build();
    HttpRequest second = builder.build().toBuilder().build();

    assertThat(first.getHeaders().get(HttpHeaders.CONTENT_TYPE)).hasSize(1);
    assertThat(second.getHeaders().get(HttpHeaders.CONTENT_TYPE))
      .isEqualTo(first.getHeaders().get(HttpHeaders.CONTENT_TYPE));
  }

  @Test
  public void itPreservesUrlOnRebuild() {
    HttpRequest request = HttpRequest
      .newBuilder()
      .setUrl("http://example.com/path")
      .setQueryParam("foo")
      .to("bar")
      .setQueryParam("baz")
      .to("1", "2")
      .build();

    assertThat(request.toBuilder().build().getUrl()).isEqualTo(request.getUrl());
  }

  @Test
  public void itPreservesMethodOnRebuild() {
    HttpRequest request = HttpRequest
      .newBuilder()
      .setUrl("http://example.com")
      .setMethod(Method.DELETE)
      .build();

    assertThat(request.toBuilder().build().getMethod()).isEqualTo(request.getMethod());
  }

  @Test
  public void itDoesNotAccumulateFormContentTypeHeaderOnRebuild() {
    // setFormParam internally calls setContentType(FORM)
    HttpRequest.Builder builder = HttpRequest
      .newBuilder()
      .setUrl("http://example.com")
      .setMethod(Method.POST)
      .setFormParam("key")
      .to("value");

    HttpRequest first = builder.build();
    HttpRequest second = builder.build().toBuilder().build();

    assertThat(first.getHeaders().get(HttpHeaders.CONTENT_TYPE)).hasSize(1);
    assertThat(second.getHeaders().get(HttpHeaders.CONTENT_TYPE))
      .isEqualTo(first.getHeaders().get(HttpHeaders.CONTENT_TYPE));
  }

  @Test
  public void itPreservesFormBodyOnRebuild() {
    ObjectMapper mapper = new ObjectMapper();
    HttpRequest request = HttpRequest
      .newBuilder()
      .setUrl("http://example.com")
      .setMethod(Method.POST)
      .setFormParam("foo")
      .to("bar")
      .setFormParam("baz")
      .to("1", "2")
      .build();

    byte[] firstBody = request.getBody(mapper);
    byte[] secondBody = request.toBuilder().build().getBody(mapper);

    assertThat(new String(secondBody, StandardCharsets.UTF_8))
      .isEqualTo(new String(firstBody, StandardCharsets.UTF_8));
  }

  @Test
  public void itPreservesExplicitHeadersOnRebuild() {
    HttpRequest request = HttpRequest
      .newBuilder()
      .setUrl("http://example.com")
      .addHeader("X-Custom", "value")
      .build();

    assertThat(request.toBuilder().build().getHeaders().get("X-Custom"))
      .isEqualTo(request.getHeaders().get("X-Custom"))
      .containsExactly("value");
  }
}
