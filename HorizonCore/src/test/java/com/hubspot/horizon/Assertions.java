package com.hubspot.horizon;

public class Assertions {

  public static HttpResponseAssert assertThat(HttpResponse response) {
    return new HttpResponseAssert(response);
  }
}
