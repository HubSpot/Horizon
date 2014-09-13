package com.hubspot.horizon;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;

import javax.annotation.Nullable;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

public interface HttpResponse {
  HttpRequest getRequest();

  int getStatusCode();
  boolean isSuccess();
  boolean isError();
  boolean isClientError();
  boolean isServerError();

  Map<String, List<String>> getHeaders();
  @Nullable String getHeader(String name);

  <T> T getAs(Class<T> clazz);
  <T> T getAs(TypeReference<T> type);
  <T> T getAs(JavaType type);

  JsonNode getAsJsonNode();
  String getAsString();
  byte[] getAsBytes();
  InputStream getAsInputStream();
}
