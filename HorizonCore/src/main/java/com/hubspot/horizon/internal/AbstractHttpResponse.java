package com.hubspot.horizon.internal;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Charsets;
import com.google.common.base.Preconditions;
import com.google.common.io.ByteStreams;
import com.google.common.io.CharStreams;
import com.hubspot.horizon.HttpResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public abstract class AbstractHttpResponse implements HttpResponse {

  public abstract ObjectMapper getObjectMapper();

  @Override
  public boolean isSuccess() {
    int statusCode = getStatusCode();
    return statusCode >= 200 && statusCode < 400;
  }

  @Override
  public boolean isError() {
    return isClientError() || isServerError();
  }

  @Override
  public boolean isClientError() {
    int statusCode = getStatusCode();
    return statusCode >= 400 && statusCode < 500;
  }

  @Override
  public boolean isServerError() {
    int statusCode = getStatusCode();
    return statusCode >= 500 && statusCode < 600;
  }

  @Override
  public <T> T getAs(Class<T> clazz) {
    try {
      return getObjectMapper()
        .readValue(getAsInputStream(), Preconditions.checkNotNull(clazz));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public <T> T getAs(TypeReference<T> type) {
    try {
      return getObjectMapper()
        .readValue(getAsInputStream(), Preconditions.checkNotNull(type));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public <T> T getAs(JavaType type) {
    try {
      return getObjectMapper()
        .readValue(getAsInputStream(), Preconditions.checkNotNull(type));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public JsonNode getAsJsonNode() {
    try {
      return getObjectMapper().readTree(getAsInputStream());
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public String getAsString() {
    InputStreamReader inputStreamReader = new InputStreamReader(
      getAsInputStream(),
      Charsets.UTF_8
    );

    try {
      return CharStreams.toString(inputStreamReader);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public byte[] getAsBytes() {
    InputStream inputStream = getAsInputStream();

    try {
      return ByteStreams.toByteArray(inputStream);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
