package com.hubspot.horizon.apache.internal;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hubspot.horizon.Header;
import com.hubspot.horizon.HttpRequest;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.ByteArrayEntity;

public final class ApacheHttpRequestConverter {
  private final ObjectMapper mapper;

  public ApacheHttpRequestConverter(ObjectMapper mapper) {
    this.mapper = mapper;
  }

  public HttpUriRequest convert(HttpRequest request) {
    final HttpUriRequest apacheRequest;

    switch (request.getMethod()) {
      case GET:
        apacheRequest = new GenericHttpRequestWithBody(request.getUrl(), HttpGet.METHOD_NAME);
        break;
      case POST:
        apacheRequest = new HttpPost(request.getUrl());
        break;
      case PUT:
        apacheRequest = new HttpPut(request.getUrl());
        break;
      case DELETE:
        apacheRequest = new GenericHttpRequestWithBody(request.getUrl(), HttpDelete.METHOD_NAME);
        break;
      case PATCH:
        apacheRequest = new HttpPatch(request.getUrl());
        break;
      case HEAD:
        apacheRequest = new GenericHttpRequestWithBody(request.getUrl(), HttpHead.METHOD_NAME);
        break;
      default:
        throw new IllegalArgumentException("Unrecognized request method: " + request.getMethod());
    }

    byte[] body = request.getBody(mapper);
    if (body != null) {
      ((HttpEntityEnclosingRequest) apacheRequest).setEntity(new ByteArrayEntity(body));
    }

    for (Header header : request.getHeaders()) {
      apacheRequest.addHeader(header.getName(), header.getValue());
    }

    return apacheRequest;
  }
}
