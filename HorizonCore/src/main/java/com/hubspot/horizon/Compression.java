package com.hubspot.horizon;

import com.google.common.base.Optional;
import com.hubspot.horizon.internal.codec.Gzip;
import com.hubspot.horizon.internal.codec.None;
import com.hubspot.horizon.internal.codec.Snappy;

public interface Compression {
  Compression NONE = None.INSTANCE;
  Compression GZIP = Gzip.INSTANCE;
  Compression SNAPPY = Snappy.INSTANCE;

  byte[] compress(byte[] data);
  Optional<String> getContentEncodingHeaderValue();
}
