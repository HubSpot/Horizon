package com.hubspot.horizon.apache.internal;

import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.ByteArrayEntity;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hubspot.horizon.Header;
import com.hubspot.horizon.HttpRequest;

public final class ApacheHttpRequestConverter {
  private final ObjectMapper mapper;

  public ApacheHttpRequestConverter(ObjectMapper mapper) {
    this.mapper = mapper;
  }

  public HttpUriRequest convert(HttpRequest request) {
    final HttpUriRequest apacheRequest;

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
        throw new IllegalArgumentException("Unrecognized request method: " + request.getMethod());
    }

    byte[] body = request.getBody(mapper);
    if (body != null && apacheRequest instanceof HttpEntityEnclosingRequest) {
      ((HttpEntityEnclosingRequest) apacheRequest).setEntity(new ByteArrayEntity(body));
    }

    for (Header header : request.getHeaders()) {
      apacheRequest.addHeader(header.getName(), header.getValue());
    }

    return apacheRequest;
  }
}
