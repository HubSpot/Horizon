package com.hubspot.horizon.internal.codec;

import com.hubspot.horizon.Codec;

import java.util.List;
import java.util.Map;

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
  public void addHeader(Map<String, List<String>> headers) { }
}