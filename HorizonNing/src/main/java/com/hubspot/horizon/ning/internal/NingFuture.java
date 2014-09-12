package com.hubspot.horizon.ning.internal;

import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.AbstractFuture;
import com.hubspot.horizon.AsyncHttpClient.Callback;
import com.hubspot.horizon.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

public class NingFuture extends AbstractFuture<HttpResponse> {
  private final Callback callback;

  public NingFuture(@Nonnull Callback callback) {
    this.callback = new CallbackWrapper(Preconditions.checkNotNull(callback));
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

  @ParametersAreNonnullByDefault
  private static final class CallbackWrapper implements Callback {
    private static final Logger LOG = LoggerFactory.getLogger(CallbackWrapper.class);

    private final Callback callback;

    private CallbackWrapper(@Nonnull Callback callback) {
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

    @Override
    public void cancelled() {
      try {
        callback.cancelled();
      } catch (Exception e) {
        LOG.error("Exception in callback", e);
      }
    }
  }
}
