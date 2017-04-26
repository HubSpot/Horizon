package com.hubspot.horizon;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.InputStream;

public interface HttpResponse {
  HttpRequest getRequest();

  int getStatusCode();
  String getReasonPhrase();
  boolean isSuccess();
  boolean isError();
  boolean isClientError();
  boolean isServerError();

  Headers getHeaders();

  <T> T getAs(Class<T> clazz);
  <T> T getAs(TypeReference<T> type);
  <T> T getAs(JavaType type);

  JsonNode getAsJsonNode();
  String getAsString();
  byte[] getAsBytes();
  InputStream getAsInputStream();
}
