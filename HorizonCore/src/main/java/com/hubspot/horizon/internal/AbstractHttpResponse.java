package com.hubspot.horizon.internal;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Charsets;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.io.ByteStreams;
import com.google.common.io.CharStreams;
import com.hubspot.horizon.HttpRequest.ContentType;
import com.hubspot.horizon.HttpResponse;
import com.hubspot.horizon.ObjectMapperHolder;
import org.apache.http.HttpHeaders;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

@ParametersAreNonnullByDefault
public abstract class AbstractHttpResponse implements HttpResponse {

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
  public @Nullable String getHeader(String name) {
    List<String> values = getHeaders().get(name);
    return values == null || values.isEmpty() ? null : values.get(0);
  }

  @Override
  public @Nonnull <T> T getAs(Class<T> clazz) {
    try {
      return getObjectMapper().readValue(getAsInputStream(), Preconditions.checkNotNull(clazz));
    } catch (IOException e) {
      throw handleJsonParseException(e);
    }
  }

  @Override
  public @Nonnull <T> T getAs(TypeReference<T> type) {
    try {
      return getObjectMapper().readValue(getAsInputStream(), Preconditions.checkNotNull(type));
    } catch (IOException e) {
      throw handleJsonParseException(e);
    }
  }

  @Override
  public @Nonnull <T> T getAs(JavaType type) {
    try {
      return getObjectMapper().readValue(getAsInputStream(), Preconditions.checkNotNull(type));
    } catch (IOException e) {
      throw handleJsonParseException(e);
    }
  }

  @Override
  public @Nonnull JsonNode getAsJsonNode() {
    try {
      return getObjectMapper().readTree(getAsInputStream());
    } catch (IOException e) {
      throw handleJsonParseException(e);
    }
  }

  @Override
  public @Nonnull String getAsString() {
    InputStreamReader inputStreamReader = new InputStreamReader(getAsInputStream(), Charsets.UTF_8);

    try {
      return CharStreams.toString(inputStreamReader);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public @Nonnull byte[] getAsBytes() {
    InputStream inputStream = getAsInputStream();

    try {
      return ByteStreams.toByteArray(inputStream);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private ObjectMapper getObjectMapper() {
    return ObjectMapperHolder.INSTANCE.get();
  }

  private RuntimeException handleJsonParseException(IOException e) {
    if (!isValidJson(getAsString())) {
      checkHeader(HttpHeaders.CONTENT_TYPE, ContentType.JSON.getHeaderValue());
    }

    throw new RuntimeException(e);
  }

  private boolean isValidJson(String maybeJson) {
    try {
      getObjectMapper().readTree(maybeJson);
      return true;
    } catch(JsonProcessingException jpe) {
      return false;
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private void checkHeader(String header, String expectedValue) {
    String actualValue = Strings.nullToEmpty(getHeader(header)).split(";")[0];
    if (!actualValue.equals(expectedValue)) {
      String message = String.format("Header '%s' has invalid value, expected '%s' but found '%s'", header, expectedValue, actualValue);
      throw new IllegalStateException(message);
    }
  }
}
