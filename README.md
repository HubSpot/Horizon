# Horizon

Java HTTP client interfaces designed to make interacting with REST services as user-friendly as possible. Horizon classes use intuitive, fluent API's and leverage Jackson internally so that reading and writing JSON are first-class citizens.

## Features
- Clean, simple interfaces
- Designed to work with JSON
- GZIP and Snappy support for compression/decompression
- Built-in retry functionality with exponential backoff
- Free to mix and match synchronous and asynchronous clients

## Usage

The HorizonCore module contains all of the interfaces and domain objects. Horizon comes with two implementations, packaged in separate artifacts. The HorizonApache module contains an implementation of the synchronous `HttpClient` interface, built on top of Apache's httpclient 4. The HorizonNing module contains an implementation of both `HttpClient` and `AsyncHttpClient` built on top of com.ning:AsyncHttpClient. So if you just want to use the Apache-based client, you would add the following Maven dependency:

```xml
<dependency>
  <groupId>com.hubspot</groupId>
  <artifactId>HorizonApache</artifactId>
  <version>0.0.12</version>
</dependency>
```

And then you can instantiate a new `HttpClient` by doing `HttpClient httpClient = new ApacheHttpClient();`

## Examples

Using the synchronous `HttpClient` to retrieve a single `Widget` by ID:

```java
public Widget getById(int id) {
    HttpRequest request = HttpRequest.newBuilder().setUrl("http://widgets/" + id).build();
    // Jackson is used to convert JSON response to Widget object
    return httpClient.execute(request).getAs(Widget.class);
}
```

Updating a widget:

```java
public Widget update(Widget widget) {
    HttpRequest request = HttpRequest.newBuilder()
            .setMethod(Method.PUT)
            .setUrl("http://widgets/" + widget.getId())
            .setBody(widget) // Jackson is used to convert Widget to JSON
            .build();
    
    return httpClient.execute(request).getAs(Widget.class);
}
```
