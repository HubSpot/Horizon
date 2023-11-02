package com.hubspot.horizon.ning.internal;

import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.AbstractFuture;
import com.hubspot.horizon.AsyncHttpClient.Callback;
import com.hubspot.horizon.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NingFuture extends AbstractFuture<HttpResponse> {

  private final Callback callback;

  public NingFuture(Callback callback) {
    this.callback = new CallbackWrapper(Preconditions.checkNotNull(callback));
  }

  // superclass method has @Nullable on the argument so make a separate method
  public boolean setNonnull(HttpResponse response) {
    Preconditions.checkNotNull(response);
    if (set(response)) {
      callback.completed(response);
      return true;
    } else {
      return false;
    }
  }

  @Override
  public boolean setException(Throwable t) {
    Preconditions.checkNotNull(t);
    if (super.setException(t)) {
      callback.failed(t instanceof Exception ? (Exception) t : new Exception(t));
      return true;
    } else {
      return false;
    }
  }

  private static final class CallbackWrapper implements Callback {

    private static final Logger LOG = LoggerFactory.getLogger(CallbackWrapper.class);

    private final Callback callback;

    private CallbackWrapper(Callback callback) {
      this.callback = Preconditions.checkNotNull(callback);
    }

    @Override
    public void completed(HttpResponse response) {
      try {
        callback.completed(response);
      } catch (Exception e) {
        LOG.error("Exception in callback", e);
      }
    }

    @Override
    public void failed(Exception e) {
      try {
        callback.failed(e);
      } catch (Exception f) {
        LOG.error("Exception in callback", f);
      }
    }
  }
}
