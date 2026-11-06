# OpenSearch deployment: configuring the client around the product header check

[한국어](../../ko/troubleshooting/opensearch-deployment.md) · [README](../../../README.md)

---

## Problem

Search worked against local Elasticsearch, but once it was pointed at AWS OpenSearch, the Elastic Java Client rejected the responses.<br>
The client uses the `X-Elastic-Product` header to verify that a response comes from Elasticsearch, and OpenSearch responses don't include that header, so the client threw an exception.

---

## Alternatives and choice

| Alternative | What has to change |
| --- | --- |
| Switch search engines | The search engine and data setup in the deployment environment |
| Move to the OpenSearch client | The client dependency and all of the search integration code |
| Add the response header and use compatibility headers | Only the connection config; the existing search code stays as it is |

To keep the existing search code while moving the deployment forward, I added the response header and applied ES 7 compatibility request headers in the connection config.

---

## Fix

Every request carries ES 7 compatibility headers by default.

```http
Accept: application/vnd.elasticsearch+json;compatible-with=7
Content-Type: application/json;compatible-with=7
```

If a response has no product header, an interceptor adds it.

```java
HttpResponseInterceptor productHeaderInjector = (response, context) -> {
    if (!response.containsHeader("X-Elastic-Product")) {
        response.addHeader("X-Elastic-Product", "Elasticsearch");
    }
};
httpClientBuilder.addInterceptorLast(productHeaderInjector);
```

Wiring this RestClient, together with Basic Auth, into the existing `ElasticsearchClient` made search work in the deployment environment without changing the search code.

---

## Choices and trade-offs

- **Responsibility for version differences**: Keeping the search code as it is means version differences between server and client have to be handled by hand in the connection config.
  If OpenSearch stays in use, moving to its own client so the server and client match is the right long-term choice.<br>
