package com.hubspot.horizon.internal.codec;

import com.hubspot.horizon.Codec;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;
import java.util.Map;

@ParametersAreNonnullByDefault
public enum None implements Codec {
  INSTANCE;

  @Override
  public @Nonnull byte[] compress(byte[] data) {
    return data;
  }

  @Override
  public @Nonnull byte[] decompress(byte[] data) {
    return data;
  }

  @Override
  public void addHeader(Map<String, List<String>> headers) { }
}