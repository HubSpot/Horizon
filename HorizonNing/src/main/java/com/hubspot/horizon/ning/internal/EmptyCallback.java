package com.hubspot.horizon.ning.internal;

import com.hubspot.horizon.AsyncHttpClient.Callback;
import com.hubspot.horizon.HttpResponse;

public enum EmptyCallback implements Callback {
  INSTANCE;

  @Override
  public void completed(HttpResponse response) { }

  @Override
  public void failed(Exception e) { }

  @Override
  public void cancelled() { }
}
