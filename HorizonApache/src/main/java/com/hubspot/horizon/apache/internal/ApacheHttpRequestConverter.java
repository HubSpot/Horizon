package com.hubspot.horizon.apache.internal;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.primitives.Ints;
import com.hubspot.horizon.Header;
import com.hubspot.horizon.HttpRequest;
import com.hubspot.horizon.HttpRequest.Options;
import java.util.concurrent.TimeUnit;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.ByteArrayEntity;

public final class ApacheHttpRequestConverter {

  private final ObjectMapper mapper;
  private final RequestConfig requestConfig;

  public ApacheHttpRequestConverter(ObjectMapper mapper) {
    this(mapper, RequestConfig.DEFAULT);
  }

  public ApacheHttpRequestConverter(ObjectMapper mapper, RequestConfig requestConfig) {
    this.mapper = mapper;
    this.requestConfig = requestConfig;
  }

  public HttpUriRequest convert(HttpRequest request) {
    return convert(request, Options.DEFAULT);
  }

  public HttpUriRequest convert(HttpRequest request, Options options) {
    final HttpRequestBase apacheRequest;

    switch (request.getMethod()) {
      case GET:
        apacheRequest = new HttpGet(request.getUrl());
        break;
      case POST:
        apacheRequest = new HttpPost(request.getUrl());
        break;
      case PUT:
        apacheRequest = new HttpPut(request.getUrl());
        break;
      case DELETE:
        apacheRequest = new HttpDeleteWithBody(request.getUrl());
        break;
      case PATCH:
        apacheRequest = new HttpPatch(request.getUrl());
        break;
      case HEAD:
        apacheRequest = new HttpHead(request.getUrl());
        break;
      default:
        throw new IllegalArgumentException(
          "Unrecognized request method: " + request.getMethod()
        );
    }

    byte[] body = request.getBody(mapper);
    if (body != null && apacheRequest instanceof HttpEntityEnclosingRequest) {
      ((HttpEntityEnclosingRequest) apacheRequest).setEntity(new ByteArrayEntity(body));
    }

    for (Header header : request.getHeaders()) {
      apacheRequest.addHeader(header.getName(), header.getValue());
    }

    options
      .getRequestTimeoutSeconds()
      .ifPresent(requestTimeoutSeconds -> {
        apacheRequest.setConfig(
          RequestConfig
            .copy(requestConfig)
            .setSocketTimeout(
              Ints.checkedCast(TimeUnit.SECONDS.toMillis(requestTimeoutSeconds))
            )
            .build()
        );
      });

    return apacheRequest;
  }
}
