package com.hubspot.horizon.apache.internal;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableSet;
import com.google.common.net.HttpHeaders;
import org.apache.http.Header;
import org.apache.http.HttpException;
import org.apache.http.HttpResponse;
import org.apache.http.client.entity.SnappyDecompressingEntity;
import org.apache.http.client.protocol.ResponseContentEncoding;
import org.apache.http.protocol.HttpContext;

import java.io.IOException;
import java.util.Locale;
import java.util.Set;

public class SnappyContentEncodingResponseInterceptor extends ResponseContentEncoding {
  private static final Set<String> IGNORED_ENCODINGS = ImmutableSet.of("none", "utf8", "utf-8", "binary");

  @Override
  public void process(HttpResponse response, HttpContext context) throws HttpException, IOException {
    Header encodingHeader = response.getFirstHeader(HttpHeaders.CONTENT_ENCODING);
    if (encodingHeader == null) {
      return;
    }

    String encoding = Strings.nullToEmpty(encodingHeader.getValue()).toLowerCase(Locale.ENGLISH);
    if ("snappy".equals(encoding) && response.getEntity() != null) {
      response.setEntity(new SnappyDecompressingEntity(response.getEntity()));
    } else if (!IGNORED_ENCODINGS.contains(encoding)) {
      super.process(response, context);
    }
  }
}
