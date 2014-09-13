package com.hubspot.horizon.internal.codec;

import com.google.common.base.Optional;
import com.hubspot.horizon.Codec;

public enum None implements Codec {
  INSTANCE;

  @Override
  public byte[] compress(byte[] data) {
    return data;
  }

  @Override
  public byte[] decompress(byte[] data) {
    return data;
  }

  @Override
  public Optional<String> getContentEncodingHeaderValue() {
    return Optional.absent();
  }
}