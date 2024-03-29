package com.hubspot.horizon.apache.internal;

import java.io.IOException;
import java.io.InputStream;
import org.apache.http.HttpEntity;
import org.apache.http.client.entity.DecompressingEntity;
import org.apache.http.client.entity.InputStreamFactory;
import org.xerial.snappy.SnappyInputStream;

public class SnappyDecompressingEntity extends DecompressingEntity {

  public SnappyDecompressingEntity(HttpEntity wrapped) {
    super(
      wrapped,
      new InputStreamFactory() {
        @Override
        public InputStream create(InputStream input) throws IOException {
          return new SnappyInputStream(input);
        }
      }
    );
  }
}
