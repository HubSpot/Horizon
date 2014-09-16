package com.hubspot.horizon.internal.codec;

import com.google.common.base.Optional;
import com.hubspot.horizon.Compression;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.GZIPOutputStream;

public enum Gzip implements Compression {
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
  public Optional<String> getContentEncodingHeaderValue() {
    return Optional.of("gzip");
  }
}
