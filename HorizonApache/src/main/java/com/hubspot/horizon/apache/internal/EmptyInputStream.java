package com.hubspot.horizon.apache.internal;

import java.io.InputStream;

public final class EmptyInputStream extends InputStream {
  private static final InputStream INSTANCE = new EmptyInputStream();

  private EmptyInputStream() { }

  public static InputStream getInstance() {
    return INSTANCE;
  }

  @Override
  public int read() {
    return -1;
  }

  @Override
  public int available() {
    return 0;
  }
}
