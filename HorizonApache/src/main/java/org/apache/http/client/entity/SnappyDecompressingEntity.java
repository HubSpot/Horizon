package org.apache.http.client.entity;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.xerial.snappy.SnappyInputStream;

import java.io.IOException;
import java.io.InputStream;

public class SnappyDecompressingEntity extends DecompressingEntity {

  public SnappyDecompressingEntity(HttpEntity wrapped) {
    super(wrapped);
  }

  @Override
  InputStream decorate(InputStream wrapped) throws IOException {
    return new SnappyInputStream(wrapped);
  }

  @Override
  public Header getContentEncoding() {
    return null;
  }

  @Override
  public long getContentLength() {
    return -1;
  }
}
