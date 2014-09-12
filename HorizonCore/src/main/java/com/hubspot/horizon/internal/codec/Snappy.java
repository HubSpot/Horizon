package com.hubspot.zenith.internal.codec;

import com.google.common.net.HttpHeaders;
import com.hubspot.horizon.Codec;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@ParametersAreNonnullByDefault
public enum Snappy implements Codec {
  INSTANCE;

  @Override
  public @Nonnull byte[] compress(byte[] data) {
    try {
      return org.xerial.snappy.Snappy.compress(data);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public @Nonnull byte[] decompress(byte[] data) {
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
