package com.hubspot.horizon;

import com.google.common.base.Optional;
import com.hubspot.horizon.internal.codec.Gzip;
import com.hubspot.horizon.internal.codec.None;
import com.hubspot.horizon.internal.codec.Snappy;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@SuppressFBWarnings("IC_SUPERCLASS_USES_SUBCLASS_DURING_INITIALIZATION")
public interface Compression {
  Compression NONE = None.INSTANCE;
  Compression GZIP = Gzip.INSTANCE;
  Compression SNAPPY = Snappy.INSTANCE;

  byte[] compress(byte[] data);
  Optional<String> getContentEncodingHeaderValue();
}
