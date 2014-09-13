package com.hubspot.horizon;

import com.hubspot.horizon.internal.codec.Gzip;
import com.hubspot.horizon.internal.codec.None;
import com.hubspot.horizon.internal.codec.Snappy;

import java.util.List;
import java.util.Map;

public interface Codec {
  Codec NONE = None.INSTANCE;
  Codec GZIP = Gzip.INSTANCE;
  Codec SNAPPY = Snappy.INSTANCE;

  byte[] compress(byte[] data);
  byte[] decompress(byte[] data);
  void addHeader(Map<String, List<String>> headers);
}
