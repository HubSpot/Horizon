package com.hubspot.horizon;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

public class HorizonHeaders {

  public static final String REQUEST_TIMEOUT = "X-Horizon-Timeout-Millis";

  @SuppressFBWarnings("CT_CONSTRUCTOR_THROW")
  private HorizonHeaders() {
    throw new AssertionError();
  }
}
