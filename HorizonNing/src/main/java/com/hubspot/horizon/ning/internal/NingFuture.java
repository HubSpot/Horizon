package com.hubspot.horizon.ning.internal;

import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.AbstractFuture;
import com.hubspot.horizon.AsyncHttpClient.Callback;
import com.hubspot.horizon.HttpResponse;

import javax.annotation.Nonnull;

public class NingFuture extends AbstractFuture<HttpResponse> {
  private final Callback callback;

  public NingFuture(Callback callback) {
    this.callback = callback;
  }

  @Override
  protected boolean set(HttpResponse response) {
    Preconditions.checkNotNull(response);
    if (super.set(response)) {
      callback.completed(response);
      return true;
    } else {
      return false;
    }
  }

  @Override
  protected boolean setException(@Nonnull Throwable t) {
    Preconditions.checkNotNull(t);
    if (super.setException(t)) {
      callback.failed(t instanceof Exception ? (Exception) t : new Exception(t));
      return true;
    } else {
      return false;
    }
  }

  @Override
  protected void interruptTask() {
    callback.cancelled();
  }
}
