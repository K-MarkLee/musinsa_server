# Detail reads: preserve information, change how relationships are fetched

[한국어](../../ko/improvements/product-detail.md) · [README](../../../README.md) · [Measurement setup](../environment.md)

---

## Problem and comparison conditions

The first detail response returns every option, price, stock value, and option description.<br>
This work occurs even without clicking an option.<br>
Previously, product/brand/images were fetched together, then the mapper traversed options, inventory, and option values, potentially issuing additional SQL.<br>
Required information was fetched through repeated small operations.

Five initially selected products were fixed for the comparison.<br>
Product numbers below are document-local sample labels.

| Product | Options | Images |
| --- | --- | --- |
| Product 1 | 60 | 1 |
| Product 2 | 72 | 1 |
| Product 3 | 65 | 1 |
| Product 4 | 67 | 1 |
| Product 5 | 72 | 1 |

For each product: one initial call, four warm-ups, then 30 measured calls, repeated three times.<br>
An execution contains 450 measured / 525 total requests.<br>
Three additional executions yield 1,350 measured / 1,575 total requests per version, with zero HTTP failures.<br>
These measure JSON responses, not image downloads or option clicks.

Measurement started after Redis option/category dictionaries loaded.<br>
Both versions used Java 21, a 6GiB heap, `TieredStopAtLevel=1`, and the dev profile.<br>
The app restarted between versions, while DB/Redis caches remained; before ran first, then after.<br>
The initial IntelliJ screenshot is kept separate from additional runs.

---

## Repeated measurements

| Run | Before mean | Before p95 | After mean | After p95 |
| --- | --- | --- | --- | --- |
| Additional run 1 | 71.33ms | 89.70ms | 9.75ms | 13.21ms |
| Additional run 2 | 70.69ms | 87.94ms | 8.36ms | 9.50ms |
| Additional run 3 | 68.41ms | 80.94ms | 8.62ms | 10.61ms |
| Initial screenshot | 67.44ms | 82.59ms | 9.71ms | 12.43ms |
| Additional 3 runs combined | 70.14ms | — | 8.91ms | — |

The additional-run mean is 70.14→8.91ms, an 87.3% reduction.<br>
Initial calls, warm-ups, and separate correctness checks are excluded.<br>
Means and p95 use individual InfluxDB request durations.

<br>

### Mean by product

Each row combines 270 measured calls across the three additional runs.

| Product | Before mean | After mean |
| --- | --- | --- |
| Product 1 | 63.49ms | 9.27ms |
| Product 2 | 74.01ms | 9.14ms |
| Product 3 | 68.52ms | 8.51ms |
| Product 4 | 69.41ms | 8.75ms |
| Product 5 | 75.30ms | 8.86ms |

Detail before

![Detail before](../../images/evidence/detail-1.png)

Detail after

![Detail after](../../images/evidence/detail-2.png)

Outside the timed benchmark, JSON fields, values, and array ordering matched for the five products.<br>
Timed k6 requests did not parse/validate the entire body.<br>
This does not establish correctness for every distribution or cache failure.

---

## Read flow

Previously, product, brand, and images were fetched together, followed by traversal of options, inventory, and option values.<br>
The new detail path still uses entities; it is not the list's direct DTO projection.

```mermaid
flowchart TD
    A[Product detail request] --> B[Fetch product + brand + options + inventory]
    B --> C[Fetch images separately]
    C --> D[Fetch option-value mappings separately]
    D --> E[Redis multiGet using distinct dictionary IDs]
    E --> F[Assemble mappings by option ID into detail DTO]
```

Options and images are independent collections.<br>
Joining both can multiply result rows, so images are fetched separately.<br>
Each sampled product had one image, however; this experiment did not directly quantify the benefit for multi-image products.

Color and size names are shared dictionary data.<br>
Distinct IDs are read from Redis in bulk and combined with mappings grouped by option ID.<br>
Prices, inventory, and complete product responses are not cached by this change.

---

## Why this path reduces work

Reading inventory individually for 60 options can repeat small SQL calls.<br>
This is an N+1-shaped relationship traversal, but exact query counts depend on mappings and loaded state, not latency alone.<br>
Explicit fetch groups, grouping mappings by option ID, and `multiGet` of distinct dictionary IDs reduce repeated traversal.

Joining three images alongside 60 options can produce 180 rows.<br>
Splitting avoids that multiplication, but every measured product had one image, so the multi-image benefit was not measured directly.<br>
The design balances round trips against result-row expansion.

Detail still reads entities and assembles DTOs in Java, unlike the list's direct DTO projection.<br>
Both versions initialize Redis, but the old detail path reads dictionary values from DB entities and the new one reads Redis.<br>
Detail's `categoryPath` comes from the product, so category-tree caching does not explain this result.<br>
Fetch splitting, join scope, and dictionary caching changed together.

---

## Decisions and alternatives

| Decision | Benefit | Cost or limitation |
| --- | --- | --- |
| Separate image and mapping reads | Control relationship sizes and repeated traversal | More DB round trips and application assembly |
| Bulk Redis dictionary read | Batch repeated name/value lookups | Redis availability, initialization, and consistency |
| Keep prices and inventory in DB reads | Read changing detail data from the DB | Does not eliminate DB work like a full-response cache |

The dictionary has only 60 entries.<br>
Joining option values in the DB or keeping an in-process dictionary are reasonable alternatives, but were not compared here.<br>
Fetch restructuring and cache use changed together, so the 87.3% reduction cannot be attributed to Redis alone.

The current dictionary path has no DB fallback or TTL; missing cached values can produce `null` names and values.<br>
Initialization, missing-data behavior, failure handling, and refresh policy need decisions before operational use.<br>
Full-response caching and Caffeine remain ideas, not implemented achievements.

<br>

### Correctly assembling multiple results

Incorrect option/dictionary ID joins can attach the wrong value or omit data.<br>
Array and image ordering must also be preserved.<br>
Consistency across multiple SQL statements depends on transactions and isolation.<br>
Redis connection failure can fail requests, while missing entries can produce null fields even with a live connection.<br>
Prepared-cache performance is separate from failure behavior.

<br>

### What to compare against a DB join

As an illustration, ten colors and ten sizes mean 20 dictionary entries, 100 options, and 200 mappings.<br>
The current approach combines 200 DB mapping rows with 20 Redis values.<br>
Joining dictionary values onto mappings would return strings on those 200 rows and remove the Redis round trip.<br>
Each mapping references one value, so this is not a cross-product of multiple collections.

The DB can read its buffer pool; repeated values do not imply a disk access or separate SQL call every time.<br>
Duplicate string transfer and join work still have costs.<br>
Hold other reads constant and change only mapping/dictionary access under identical responses, warm-up, and repeats.<br>
If performance is similar, operational/code simplicity matters.<br>
The [Redis/memory/DB comparison](../future-work.md) remains future work.

---

## Interpretation limits

Execution order and warm-up are not fully isolated.<br>
Per-SQL timings, allocation/GC, cache failure, multiple images, and concurrency need separate tests.<br>
Observation classes and `@Observed` were also added while the existing disabled OTLP/tracing settings were retained; their contribution was not isolated.

---

## Follow-up under load

The optimized detail path still waited for connections at higher load.<br>
The 1,000-RPS baseline and Hikari/Tomcat experiments are separate workloads.<br>
See [baseline detail results](../load-tests/product-detail.md) and [connection-pool troubleshooting](../troubleshooting/hikari.md).

Implementation: [ProductQueryService](../../../src/main/java/com/mudosa/musinsa/product/application/ProductQueryService.java) · [ProductQueryMapper](../../../src/main/java/com/mudosa/musinsa/product/application/mapper/ProductQueryMapper.java)
