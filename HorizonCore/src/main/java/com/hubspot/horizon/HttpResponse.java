package com.hubspot.horizon;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

@ParametersAreNonnullByDefault
public interface HttpResponse {
  @Nonnull HttpRequest getRequest();

  int getStatusCode();
  boolean isSuccess();
  boolean isError();
  boolean isClientError();
  boolean isServerError();

  @Nonnull Map<String, List<String>> getHeaders();
  @Nullable String getHeader(String name);

  @Nonnull <T> T getAs(Class<T> clazz);
  @Nonnull <T> T getAs(TypeReference<T> type);
  @Nonnull <T> T getAs(JavaType type);

  @Nonnull JsonNode getAsJsonNode();
  @Nonnull String getAsString();
  @Nonnull byte[] getAsBytes();
  @Nonnull InputStream getAsInputStream();
}
