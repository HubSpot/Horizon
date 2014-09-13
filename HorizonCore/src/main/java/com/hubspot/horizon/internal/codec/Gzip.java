package com.hubspot.horizon.internal.codec;

import com.google.common.base.Optional;
import com.google.common.io.ByteStreams;
import com.hubspot.horizon.Codec;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public enum Gzip implements Codec {
  INSTANCE;

  @Override
  public byte[] compress(byte[] data) {
    try {
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      OutputStream gzip = new GZIPOutputStream(baos);

      gzip.write(data);
      gzip.close();

      return baos.toByteArray();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public byte[] decompress(byte[] bytes) {
    try {
      return ByteStreams.toByteArray(new GZIPInputStream(new ByteArrayInputStream(bytes)));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public Optional<String> getContentEncodingHeaderValue() {
    return Optional.of("gzip");
  }
}
