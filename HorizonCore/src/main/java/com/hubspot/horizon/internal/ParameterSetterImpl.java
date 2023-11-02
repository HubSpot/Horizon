package com.hubspot.horizon.internal;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.hubspot.horizon.HttpRequest.Builder;
import com.hubspot.horizon.HttpRequest.ParameterSetter;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;

public class ParameterSetterImpl implements ParameterSetter {

  private final String name;
  private final Builder builder;
  private final Map<String, List<String>> parameters;

  public ParameterSetterImpl(
    String name,
    Builder builder,
    Map<String, List<String>> parameters
  ) {
    this.name = Preconditions.checkNotNull(name);
    this.builder = Preconditions.checkNotNull(builder);
    this.parameters = Preconditions.checkNotNull(parameters);
  }

  @Override
  public Builder to(Iterable<String> values) {
    for (String value : values) {
      add(name, value);
    }
    return builder;
  }

  @Override
  public Builder to(String... values) {
    for (String value : values) {
      add(name, value);
    }
    return builder;
  }

  @Override
  public Builder to(boolean... values) {
    for (boolean value : values) {
      add(name, String.valueOf(value));
    }
    return builder;
  }

  @Override
  public Builder to(int... values) {
    for (int value : values) {
      add(name, String.valueOf(value));
    }
    return builder;
  }

  @Override
  public Builder to(long... values) {
    for (long value : values) {
      add(name, String.valueOf(value));
    }
    return builder;
  }

  private void add(String name, @Nullable String value) {
    if (parameters.containsKey(name)) {
      parameters.get(name).add(value);
    } else {
      parameters.put(name, Lists.newArrayList(value));
    }
  }
}
