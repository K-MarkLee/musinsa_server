# Deduplicating option documents and paginating product search

[한국어](../../ko/troubleshooting/search-pagination.md) · [README](../../../README.md) · [Measurement setup](../environment.md)

---

## Why search individual options?

Color, size, price, and stock vary between options of the same product.<br>
Separate arrays of colors and sizes can match a combination that is not sold: for example, black/M when the available options are black/S and red/M.

Each option became an independent document so its attributes could be evaluated together.<br>
This introduced another problem: multiple matching options could fill the result list with the same product.<br>
Search operates on options, while the UI displays products.

---

## Decision: collapse by productId

I grouped matching options by `productId` and returned the representative selected by the requested ordering.<br>
Relevance selects the highest-scoring option; ascending price selects the cheapest matching option.<br>
[Collapse behavior](https://www.elastic.co/docs/reference/elasticsearch/rest-apis/collapse-search-results)

```mermaid
flowchart LR
    A[Search conditions] --> B[Matching option documents]
    B --> C[Collapse by productId]
    C --> D[Representative option per product]
    D --> E[Deduplicated product list]
```

---

## The pagination conflict

Infinite scrolling initially called for `search_after`.<br>
With collapse, however, the collapse and sort fields must be identical and secondary sorts are disallowed.<br>
Grouping by product ID while sorting by relevance or price did not fit that restriction.

| Alternative | Decision |
| --- | --- |
| Fetch many options and merge in the application | An average of 72 options × 30 products = 2,160 documents still does not guarantee 30 unique products.<br>Matching option counts vary, and transferring and merging more documents adds work. |
| Fetch batches in parallel and merge | Product deduplication, global ordering, and page boundaries still need application logic. |
| collapse + from/size | Selected to keep grouping and ordering in ES while returning pages of products. |

The cursor now contains a page number, translated into `from/size`.<br>
The API retains a cursor-shaped interface while the internal pagination strategy changes.

---

## Trade-offs

- Deep pages: Larger offsets require collecting earlier results and remain subject to the result-window limit, including relevance-sorted searches.
- Product counts: Total hits counts matching option documents before collapse.<br>
  A unique-product count requires separate aggregation.
- Search work: Group selection adds work alongside matching and scoring.<br>
  The [load investigation](search-capacity.md) observed these search, scoring, and grouping paths.

---

## Alternative: a product document with nested options

A product-level document with `nested` options preserves each option's attribute combinations while returning products directly.<br>
It could remove product-deduplication collapse and support `search_after` on product-level sort values.<br>
[Nested document model](https://www.elastic.co/docs/reference/elasticsearch/mapping-reference/nested)

Nested options still become separate Lucene documents searched in relation to their parent.<br>
Option updates also entail reindexing the product's nested document block.<br>
The decision therefore balances search latency, relevance, and option-update cost, rather than treating removal of collapse as a complete optimization.
