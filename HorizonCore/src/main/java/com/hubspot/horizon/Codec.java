package com.hubspot.horizon;

import com.hubspot.zenith.internal.codec.Gzip;
import com.hubspot.zenith.internal.codec.None;
import com.hubspot.zenith.internal.codec.Snappy;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;
import java.util.Map;

@ParametersAreNonnullByDefault
public interface Codec {
  Codec NONE = None.INSTANCE;
  Codec GZIP = Gzip.INSTANCE;
  Codec SNAPPY = Snappy.INSTANCE;

  @Nonnull byte[] compress(byte[] data);
  @Nonnull byte[] decompress(byte[] data);
  void addHeader(Map<String, List<String>> headers);
}
