# Search design: from a product-name substring to option-aware intent

[한국어](../../ko/improvements/product-search.md) · [README](../../../README.md) · [Measurement setup](../environment.md)

A product-name substring cannot express intent well when color, product type, and brand live in different fields.<br>
ES was chosen to preserve option relationships, search across attributes, and rank results.<br>
This document records changed results and their costs alongside latency.

---

## Comparison scenario

Four queries were called equally: `스커트` (skirt), `블랙 스커트` (black skirt), `나이키` (Nike), and `나이키 후드` (Nike hoodie).<br>
Each `GET /api/products?limit=10&keyword=...` retrieves the first ten results.<br>
For each query: one initial call, four warm-ups, and 30 measured calls, repeated three times.<br>
Each execution has 360 measured / 420 total calls; three additional executions provide 1,080 measured samples per method, with zero HTTP failures.

Additional executions ran ES before LIKE.<br>
Apps restarted between versions, but DB/ES caches were not cleared.<br>
Timed k6 calls checked HTTP success without validating JSON bodies.<br>
Result screenshots are separate observations.

---

## Repeated measurements

| Run | Before mean | Before p95 | After mean | After p95 |
| --- | --- | --- | --- | --- |
| Additional run 1 | 89.96ms | 174.35ms | 25.75ms | 60.19ms |
| Additional run 2 | 90.19ms | 175.78ms | 22.95ms | 54.48ms |
| Additional run 3 | 89.91ms | 176.34ms | 22.37ms | 53.50ms |
| Initial screenshot | 87.06ms | 168.54ms | 22.14ms | 47.06ms |
| Arithmetic mean of 4 runs | 89.28ms | 173.75ms | 23.30ms | 53.81ms |

The arithmetic mean of run means is 89.28→23.30ms, a 73.9% reduction.<br>
The final p95 cells, 173.75/53.81ms, average run percentiles; they are not percentiles pooled across all requests.

Additional runs use individual InfluxDB durations.<br>
The initial ES screenshot represents the restored introduction version; additional runs include response-field limiting and a brand-search-field correction.<br>
These are not four identical-code repeats.<br>
Matching and ordering also differ, so this does not compare algorithms producing an identical result set.

Initial LIKE k6 screenshot

![Initial LIKE k6 screenshot](../../images/evidence/search-design-5.png)

Initial ES k6 screenshot

![Initial ES k6 screenshot](../../images/evidence/search-design-10.png)

---

## Observed result differences

These screenshots illustrate individual queries, not a quantitative relevance evaluation.<br>
A result screenshot alone does not establish exactly which field matched every returned product.

<details>
<summary>Skirt: LIKE / Elasticsearch</summary>

LIKE returns product-name substring matches in ID order; ES ordering also reflects category and other relevance.<br>
Repetition in a title alone does not determine BM25 rank.

Skirt LIKE results

![Skirt LIKE results](../../images/evidence/search-design-1.png)

Skirt ES results

![Skirt ES results](../../images/evidence/search-design-6.png)

</details>

<details>
<summary>Black skirt: LIKE / Elasticsearch</summary>

LIKE found no contiguous phrase.<br>
ES matched the tokens separately and returned results; exact field attribution requires a separate explain.

Black skirt LIKE results

![Black skirt LIKE results](../../images/evidence/search-design-2.png)

Black skirt ES results

![Black skirt ES results](../../images/evidence/search-design-7.png)

</details>

<details>
<summary>Nike: LIKE / Elasticsearch</summary>

LIKE uses title inclusion while ES combines multiple fields.<br>
Source brand values include sellers, so neither screenshot establishes verified official-brand identity.

Nike LIKE results

![Nike LIKE results](../../images/evidence/search-design-3.png)

Nike ES results

![Nike ES results](../../images/evidence/search-design-8.png)

</details>

<details>
<summary>Nike hoodie: LIKE / Elasticsearch</summary>

LIKE can miss titles with an intervening word, such as Nike Club Hoodie.<br>
In current ES results, the hoodie-category contribution can outrank another item with a higher title contribution.

Nike hoodie LIKE results

![Nike hoodie LIKE results](../../images/evidence/search-design-4.png)

Nike hoodie ES results

![Nike hoodie ES results](../../images/evidence/search-design-9.png)

</details>

---

## Document unit: one sellable option

Flattening all colors and sizes onto a product can match a combination that no individual option offers.<br>
I used one document per option, applied stock and availability filters, then collapsed on `productId` to return products.

```mermaid
flowchart LR
    A[MySQL products and options] --> B[One search document per option]
    B --> C[Availability / stock / category / brand filters]
    C --> D[Per-token field matching + relevance scores]
    D --> E[Collapse by productId]
    E --> F[Product list]
```

This allows individual option updates but repeats shared product and brand information.<br>
Updating a product can require updating many documents; approximately 155,000 products expand to approximately 11 million option documents.<br>
A product document with nested options is a candidate, but search cost must be compared with reindexing costs when options change.<br>
That migration has not been implemented or measured.

Product name, brand, representative price, and thumbnail come from ES to reduce DB rereads for search lists.<br>
The thumbnail is not a query-matching field; its baseline keyword mapping was retained.<br>
Stock/availability filters do not imply automatic synchronization from DB updates.

---

## Analyzers, multifields, and update cost

| Field | Mapping | Reason |
| --- | --- | --- |
| productName / krBrandName | text + nori_default | Tokenized Korean search |
| enBrandName | text | English-brand search |
| categoryPath | keyword + text subfield (nori_default) | Path filters and category-token matching |
| colorOptions / sizeOptions | keyword + text subfields (nori_color / nori_size) | Original option values and synonym-expanded search forms |

`nori_default` uses a Nori tokenizer, part-of-speech filtering, reading-form conversion, and lowercasing.<br>
Color/size analyzers lowercase, expand synonyms, and apply part-of-speech/reading-form filters; search uses `nori_default`.<br>
Examples connect 블랙/black/검정색/blk and M/미디엄/medium.<br>
Index-time synonym changes require reindexing existing documents.

Multifields index the same value as keyword/text representations; this differs from querying multiple fields with `multi_match`.

---

## Separate matching requirements from ranking

The index uses Nori and synonym analysis, with distinct exact-filter and text-search fields.<br>
Every input token must match at least one of product name, category, color, size, or brand.<br>
These token clauses are combined with `must`.<br>
Additional `should` matches for the whole query affect ranking.

| Whole-query field | Boost | Rationale |
| --- | ---: | --- |
| Category / color | 3 | Emphasize product type and option attributes |
| Product name | 2 | Useful meaning mixed with promotional text |
| Size / Korean and English brand | 1 | Supporting clues; collected brand data can include seller names |

These boosts are not separately applied to the required per-token clauses.<br>
They are initial design assumptions, not an optimum established from user logs or relevance judgments.<br>
Color is not assumed universally more important than brand.

Whole-query field boosts

![Whole-query field boosts](../../images/evidence/search-design-11.png)

In the collected data, 131,962 descriptions duplicated product titles.<br>
Descriptions were excluded to avoid adding repeated matches without new information.<br>
Promotional titles and inaccurate attributes remain data-quality problems that a search engine alone does not solve.

A concrete title/category score reversal is documented separately in [search-quality troubleshooting](../troubleshooting/search-quality.md).<br>
Boosts multiply scores rather than assigning fixed ranks; they do not correct erroneous source categories or brands.

---

## Why results and latency changed

LIKE uses product-name substrings and ID order; ES uses morphology, synonyms, multiple fields, and relevance ordering.<br>
The change serves richer cross-attribute intent, and latency also fell in this comparison.<br>
It does not establish that LIKE used a full scan or attribute saved milliseconds to one ES operation.<br>
Queries were equally weighted because empty-result requests can be faster.

---

## One-shard diagnostic

One-shard diagnostic

![One-shard diagnostic](../../images/evidence/search-design-12.png)

The one-shard screenshot shows 38.55ms mean / 99.95ms p95, versus 22.14ms / 47.06ms for the initial two-shard screenshot: approximately 1.74× / 2.12× higher.<br>
Shards can execute in parallel on one node, but equivalence of mappings, analyzers, data, cache, and post-reindex merge state was not established.<br>
This is not an isolated shard-count effect, is excluded from the four-run LIKE/ES aggregate, and the baseline index was restored.

---

## Operational settings and trade-offs

| Setting | Baseline | Meaning / trade-off |
| --- | --- | --- |
| Index | product | Baseline documents and analyzers |
| number_of_shards | 2 | Shard-level parallelism plus merge/management cost |
| number_of_replicas | 0 | Single-node measurement; no failover replica |
| refresh_interval | 10s | Search-visibility delay versus refresh work; benefit not isolated |
| index.queries.cache.enabled | true | Reusable eligible filter document sets, not every complete query response |

A ten-second refresh is not ten-second polling of MySQL.<br>
Query-cache hits/misses and usage were not measured, so cache benefit is not claimed as the cause.<br>
Richer search requires operating ES, indexing/reindexing, propagating changes, and recovering failed updates.<br>
Duplicated product data, collapse, and result merging remain costs.

---

## Current limits and follow-up

Unlike the list's keyset path, search uses `from/size` and a page-number cursor.<br>
Collapse and sorting restrictions are covered in [search and operations troubleshooting](../troubleshooting/search-pagination.md).<br>
Accurate unique-product totals are not guaranteed, and deep-page cost remains.

DB-to-ES propagation and recovery need separate validation.<br>
An index refresh interval does not establish synchronization with MySQL.<br>
The measured local index used two shards and zero replicas; it does not represent a highly available deployment.

At higher load, search CPU work and queueing remained after connection limits were increased.<br>
[Search bottleneck investigation](../troubleshooting/search-capacity.md) separates observations from unmeasured query costs and shared-host effects.<br>
Current query construction is in [ProductIndexSearchQueryRepositoryImpl](../../../src/main/java/com/mudosa/musinsa/product/infrastructure/search/repository/ProductIndexSearchQueryRepositoryImpl.java).
