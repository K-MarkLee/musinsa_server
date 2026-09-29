# Product detail: isolating waiting and DTO assembly with mocks

[한국어](../../ko/troubleshooting/detail-isolation.md) · [README](../../../README.md) · [Measurement setup](../environment.md)

---

## Starting point: a long Security span

Detail throughput plateaued around 2,000 RPS, while Jaeger showed a long Security span.<br>
I replaced request layers with mocks to determine whether authentication itself was slow or whether the enclosing span included downstream waiting and processing.

---

## Measurements with layers replaced

| Stage | Request path | Measured RPS | Finding |
| --- | --- | ---: | --- |
| 1 | Mock Controller without Security | 41,000 | The simple response path handled substantially more requests than the detail API. |
| 2 | Mock Controller with Security | 42,000 | Throughput remained high with Security, directing the investigation downstream. |
| 3 | Mock Service with Dummy DB | 66,000 | Replacing real business and DB work substantially increased throughput. |
| 4 | Real detail Service | 2,000 | The investigation narrowed to relationship reads and response assembly. |

Each row replaced a different part of the request path.<br>
Starting from the outer layers directed attention to service-entry waiting and real detail processing instead of changing Security itself.

---

## Two areas identified with Jaeger and code

The first was waiting before the service body.<br>
A long enclosing Security span did not mean authentication consumed its entire duration.<br>
Child-span start times and connection-pool metrics exposed waiting before application processing began.

The second was assembling retrieved data into DTOs.<br>
Walking product, option, image, and option-value relationships combined lazy loading with repeated transformations.<br>
Fetch-joining multiple to-many relationships could also multiply rows, so reducing query count alone did not address the full read and assembly cost.

---

## Change: restructure reads and assembly

I separated product/option/inventory reads, image reads, and option-value mappings, then combined the results into the response.<br>
Repeated color/size dictionary values were fetched in batches; price and inventory remained DB reads.

The [detail improvement](../improvements/product-detail.md) documents repeated measurements of this redesign and the 70.14→8.91ms result.<br>
Follow-up [Hikari tuning](hikari.md) examined connection-acquisition waiting under concurrency, and the [Tomcat comparison](tomcat.md) examined worker counts.

---

## Decision

Instead of identifying the cause from the name of the longest enclosing span, I narrowed the executed path with mocks and connected child spans to the implementation.<br>
This focused the changes on connection waiting, relationship reads, and DTO assembly.
