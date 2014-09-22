package com.hubspot.horizon;

import com.google.common.base.Preconditions;

public class Header {
  private final String name;
  private final String value;

  public Header(String name, String value) {
    this.name = Preconditions.checkNotNull(name);
    this.value = Preconditions.checkNotNull(value);
  }

  public String getName() {
    return name;
  }

  public String getValue() {
    return value;
  }
}
