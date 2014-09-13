package com.hubspot.horizon.internal.codec;

import com.google.common.net.HttpHeaders;
import com.hubspot.horizon.Codec;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public enum Snappy implements Codec {
  INSTANCE;

  @Override
  public byte[] compress(byte[] data) {
    try {
      return org.xerial.snappy.Snappy.compress(data);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public byte[] decompress(byte[] data) {
    try {
      return org.xerial.snappy.Snappy.uncompress(data);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void addHeader(Map<String, List<String>> headers) {
    headers.put(HttpHeaders.CONTENT_ENCODING, Collections.singletonList("snappy"));
  }
}
