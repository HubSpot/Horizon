package com.hubspot.horizon;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;

public class Headers implements Iterable<Header> {
  private final List<Header> headerList;
  private final LinkedHashMap<String, List<String>> headerMap;

  public Headers(List<Header> headers) {
    this.headerList = headers;
    this.headerMap = new LinkedHashMap<String, List<String>>();

    for (Header header : headers) {
      String key = header.getName().toLowerCase();

      if (headerMap.containsKey(key)) {
        headerMap.get(key).add(header.getValue());
      } else {
        headerMap.put(key, Lists.newArrayList(header.getValue()));
      }
    }
  }

  @Override
  public Iterator<Header> iterator() {
    return headerList.iterator();
  }

  public List<String> get(String name) {
    List<String> headers = headerMap.get(Preconditions.checkNotNull(name).toLowerCase());
    return headers == null ? Collections.<String>emptyList() : headers;
  }

  public @Nullable String getFirst(String name) {
    List<String> headers = get(Preconditions.checkNotNull(name).toLowerCase());
    return headers.isEmpty() ? null : headers.get(0);
  }
}
