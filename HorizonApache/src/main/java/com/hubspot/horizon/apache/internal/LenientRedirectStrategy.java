package com.hubspot.horizon.apache.internal;

import java.net.URI;
import java.net.URISyntaxException;
import org.apache.http.ProtocolException;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.DefaultRedirectStrategy;

public class LenientRedirectStrategy extends DefaultRedirectStrategy {

  @Override
  protected URI createLocationURI(String location) throws ProtocolException {
    try {
      return new URI(location).normalize();
    } catch (URISyntaxException e) {
      try {
        return parseLenient(location).normalize();
      } catch (URISyntaxException f) {
        throw new ProtocolException("Invalid redirect URI: " + location, e);
      }
    }
  }

  private URI parseLenient(String location) throws URISyntaxException {
    int startQuery = location.indexOf('?');
    int startAnchor = location.indexOf('#');
    boolean hasQuery = startQuery > 0;
    boolean hasAnchor = startAnchor > 0;

    final URIBuilder builder;

    if (hasQuery) {
      builder = new URIBuilder(location.substring(0, startQuery));
    } else if (hasAnchor) {
      builder = new URIBuilder(location.substring(0, startAnchor));
    } else {
      builder = new URIBuilder(location);
    }

    if (hasQuery) {
      if (hasAnchor) {
        builder.setQuery(location.substring(startQuery + 1, startAnchor));
      } else {
        builder.setQuery(location.substring(startQuery + 1));
      }
    }

    if (hasAnchor) {
      builder.setFragment(location.substring(startAnchor + 1));
    }

    return builder.build();
  }
}
