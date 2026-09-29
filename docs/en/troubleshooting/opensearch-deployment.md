# OpenSearch deployment: working around the product-header check

[한국어](../../ko/troubleshooting/opensearch-deployment.md) · [README](../../../README.md) · [Measurement setup](../environment.md)

---

## Problem: search failed after deployment

Search worked against local Elasticsearch, but the Elastic Java Client rejected responses from AWS OpenSearch.<br>
The client checks `X-Elastic-Product` to identify an Elasticsearch response.<br>
OpenSearch did not supply that header, causing the product check to fail.

---

## Alternatives and decision

| Alternative | Scope |
| --- | --- |
| Change the server engine | Change the deployed search engine and data setup. |
| Adopt the OpenSearch client | Change client dependencies and search integration code. |
| Add a response interceptor and compatibility headers | Resolve the product check in connection configuration while retaining the existing search code. |

To proceed with deployment, I configured a custom RestClient with a response-header interceptor and ES 7 compatibility request headers.

---

## Applied workaround

The client supplied these default request headers:

```http
Accept: application/vnd.elasticsearch+json;compatible-with=7
Content-Type: application/json;compatible-with=7
```

The response interceptor supplied the product header when absent:

```java
HttpResponseInterceptor productHeaderInjector = (response, context) -> {
    if (!response.containsHeader("X-Elastic-Product")) {
        response.addHeader("X-Elastic-Product", "Elasticsearch");
    }
};
httpClientBuilder.addInterceptorLast(productHeaderInjector);
```

I connected this RestClient, including Basic Auth, to `RestClientTransport` for the existing `ElasticsearchClient`.<br>
This bypassed the blocking product check at the connection layer and allowed the search integration to continue.

---

## Decision and trade-off

The priority was resolving the deployment issue without rewriting queries and response mapping.<br>
The trade-off was taking responsibility for server/client version differences in the connection layer.

For continued OpenSearch use, the longer-term direction is to adopt its dedicated client.<br>
The workaround supplies product identification and request-format headers; it does not change the server engine into Elasticsearch.<br>
[OpenSearch client guidance](https://docs.opensearch.org/latest/clients/)
