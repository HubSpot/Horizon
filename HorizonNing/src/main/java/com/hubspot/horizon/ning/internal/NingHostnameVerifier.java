package com.hubspot.horizon.ning.internal;

import com.hubspot.horizon.SSLConfig;

import javax.annotation.Nullable;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSession;
import java.security.cert.Certificate;
import java.security.cert.CertificateParsingException;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

/**
 * Tidied up version of org.apache.http.conn.ssl.AbstractVerifier
 */
public class NingHostnameVerifier implements HostnameVerifier {
  private final boolean acceptAllSSL;

  public NingHostnameVerifier(SSLConfig config) {
    this.acceptAllSSL = config.isAcceptAllSSL();
  }

  /**
   * This contains a list of 2nd-level domains that aren't allowed to
   * have wildcards when combined with country-codes.
   * For example: [*.co.uk].
   * <p/>
   * The [*.co.uk] problem is an interesting one.  Should we just hope
   * that CA's would never foolishly allow such a certificate to happen?
   * Looks like we're the only implementation guarding against this.
   * Firefox, Curl, Sun Java 1.4, 5, 6 don't bother with this check.
   */
  private final static String[] BAD_COUNTRY_2LDS =
          { "ac", "co", "com", "ed", "edu", "go", "gouv", "gov", "info", "lg", "ne", "net", "or", "org" };

  static {
    // Just in case developer forgot to manually sort the array.  :-)
    Arrays.sort(BAD_COUNTRY_2LDS);
  }

  @Override
  public boolean verify(String host, SSLSession session) {
    if (acceptAllSSL) {
      return true;
    } else {
      try {
        Certificate[] certs = session.getPeerCertificates();
        X509Certificate x509 = (X509Certificate) certs[0];
        verify(host, x509);
        return true;
      } catch(SSLException e) {
        return false;
      }
    }
  }

  private void verify(String host, X509Certificate cert) throws SSLException {
    List<String> cns = getCNs(cert);
    List<String> subjectAlts = getSubjectAlts(cert, host);

    // Build the list of names we're going to check.  Our DEFAULT and
    // STRICT implementations of the HostnameVerifier only use the
    // first CN provided.  All other CNs are ignored.
    // (Firefox, wget, curl, Sun Java 1.4, 5, 6 all work this way).
    LinkedList<String> names = new LinkedList<String>();
    if (!cns.isEmpty()) {
      names.add(cns.get(0));
    }
    names.addAll(subjectAlts);

    if (names.isEmpty()) {
      String msg = "Certificate for <" + host + "> doesn't contain CN or DNS subjectAlt";
      throw new SSLException(msg);
    }

    // StringBuilder for building the error message.
    StringBuilder buf = new StringBuilder();

    // We're can be case-insensitive when comparing the host we used to
    // establish the socket to the hostname in the certificate.
    String hostName = host.trim().toLowerCase(Locale.US);
    boolean match = false;
    for (Iterator<String> it = names.iterator(); it.hasNext();) {
      // Don't trim the CN, though!
      String cn = it.next();
      cn = cn.toLowerCase(Locale.US);
      // Store CN in StringBuilder in case we need to report an error.
      buf.append(" <");
      buf.append(cn);
      buf.append('>');
      if(it.hasNext()) {
        buf.append(" OR");
      }

      // The CN better have at least two dots if it wants wildcard
      // action.  It also can't be [*.co.uk] or [*.co.jp] or
      // [*.org.uk], etc...
      String parts[] = cn.split("\\.");
      boolean doWildcard = parts.length >= 3 &&
              parts[0].endsWith("*") &&
              acceptableCountryWildcard(cn) &&
              !isIPAddress(host);

      if(doWildcard) {
        String firstpart = parts[0];
        if (firstpart.length() > 1) { // e.g. server*
          String prefix = firstpart.substring(0, firstpart.length() - 1); // e.g. server
          String suffix = cn.substring(firstpart.length()); // skip wildcard part from cn
          String hostSuffix = hostName.substring(prefix.length()); // skip wildcard part from host
          match = hostName.startsWith(prefix) && hostSuffix.endsWith(suffix);
        } else {
          match = hostName.endsWith(cn.substring(1));
        }
      } else {
        match = hostName.equals(cn);
      }
      if(match) {
        break;
      }
    }
    if(!match) {
      throw new SSLException("hostname in certificate didn't match: <" + host + "> !=" + buf);
    }
  }

  private static boolean acceptableCountryWildcard(String cn) {
    String parts[] = cn.split("\\.");
    if (parts.length != 3 || parts[2].length() != 2) {
      return true; // it's not an attempt to wildcard a 2TLD within a country code
    }
    return Arrays.binarySearch(BAD_COUNTRY_2LDS, parts[1]) < 0;
  }

  private static List<String> getCNs(X509Certificate cert) {
    LinkedList<String> cnList = new LinkedList<String>();
        /*
          Sebastian Hauer's original StrictSSLProtocolSocketFactory used
          getName() and had the following comment:

                Parses a X.500 distinguished name for the value of the
                "Common Name" field.  This is done a bit sloppy right
                 now and should probably be done a bit more according to
                <code>RFC 2253</code>.

           I've noticed that toString() seems to do a better job than
           getName() on these X500Principal objects, so I'm hoping that
           addresses Sebastian's concern.

           For example, getName() gives me this:
           1.2.840.113549.1.9.1=#16166a756c6975736461766965734063756362632e636f6d

           whereas toString() gives me this:
           EMAILADDRESS=juliusdavies@cucbc.com

           Looks like toString() even works with non-ascii domain names!
           I tested it with "&#x82b1;&#x5b50;.co.jp" and it worked fine.
        */

    String subjectPrincipal = cert.getSubjectX500Principal().toString();
    StringTokenizer st = new StringTokenizer(subjectPrincipal, ",+");
    while (st.hasMoreTokens()) {
      String tok = st.nextToken().trim();
      if (tok.length() > 3) {
        if (tok.substring(0, 3).equalsIgnoreCase("CN=")) {
          cnList.add(tok.substring(3));
        }
      }
    }
    return cnList;
  }

  private static List<String> getSubjectAlts(X509Certificate cert, @Nullable String hostname) {
    int subjectType;
    if (isIPAddress(hostname)) {
      subjectType = 7;
    } else {
      subjectType = 2;
    }

    LinkedList<String> subjectAltList = new LinkedList<String>();
    try {
      for (List<?> aC : cert.getSubjectAlternativeNames()) {
        int type = ((Integer) aC.get(0));
        if (type == subjectType) {
          String s = (String) aC.get(1);
          if (s != null) {
            subjectAltList.add(s);
          }
        }
      }
    } catch(CertificateParsingException cpe) {
      // ignored
    }
    return subjectAltList;
  }

  private static final Pattern IPV4_PATTERN =
          Pattern.compile(
                  "^(([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\\.){3}([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])$");

  private static final Pattern IPV6_STD_PATTERN =
          Pattern.compile(
                  "^(?:[0-9a-fA-F]{1,4}:){7}[0-9a-fA-F]{1,4}$");

  private static final Pattern IPV6_HEX_COMPRESSED_PATTERN =
          Pattern.compile(
                  "^((?:[0-9A-Fa-f]{1,4}(?::[0-9A-Fa-f]{1,4})*)?)::((?:[0-9A-Fa-f]{1,4}(?::[0-9A-Fa-f]{1,4})*)?)$");

  private static boolean isIPAddress(@Nullable String hostname) {
    return hostname != null && (isIPv4Address(hostname) || isIPv6Address(hostname));
  }

  private static boolean isIPv4Address(String input) {
    return IPV4_PATTERN.matcher(input).matches();
  }

  private static boolean isIPv6Address(String input) {
    return isIPv6StdAddress(input) || isIPv6HexCompressedAddress(input);
  }

  private static boolean isIPv6StdAddress(String input) {
    return IPV6_STD_PATTERN.matcher(input).matches();
  }

  private static boolean isIPv6HexCompressedAddress(String input) {
    int colonCount = 0;
    for(int i = 0; i < input.length(); i++) {
      if (input.charAt(i) == ':') {
        colonCount++;
      }
    }
    return colonCount <= 7 && IPV6_HEX_COMPRESSED_PATTERN.matcher(input).matches();
  }
}
