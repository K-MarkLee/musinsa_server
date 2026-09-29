# Product filters: composite indexes verified with execution plans

[한국어](../../ko/improvements/filter-indexes.md) · [README](../../../README.md) · [Measurement setup](../environment.md)

---

## Problem and comparison conditions

ID-ordered and price-ordered lists need different access paths.<br>
Narrowing candidates with filters does not guarantee cheap retrieval of the cheapest/priciest 24 products.<br>
Composite indexes were evaluated against filtering and ordering together.

The test used the first page of `GET /api/products?limit=24`; SQL reads up to 25 rows for next-page detection.<br>
Ten filters × three orders (ID, lowest price, highest price) produce 30 cases.<br>
Categories were bags, tote bags, or tote/crossbody together; one fixed brand and unisex `ALL` were used.<br>
ALL is a value, not an omitted filter.<br>
Category-prefix and multi-category IN predicates stayed unchanged.

For each case: one initial request, four warm-ups, and 30 measured calls, repeated three times.<br>
An execution contains 2,700 measured / 3,150 total calls; three additional executions yield 8,100 measured / 9,450 total per state.<br>
The without-index state retained PK, unique constraints, and the brand index required for its foreign key.

---

## Selected indexes

```sql
CREATE INDEX idx_product_price
    ON product (is_available, default_price, product_id);
CREATE INDEX idx_product_brand_price
    ON product (brand_id, is_available, default_price, product_id);
```

Query code stayed fixed while both indexes were applied together.<br>
The category candidate was excluded from the final state.

---

## Repeated measurements

Values are milliseconds.<br>
Additional-run aggregates are separate from aggregates including the initial screenshot.

| Run | Before mean | Before p95 | After mean | After p95 |
| --- | --- | --- | --- | --- |
| Initial screenshot | 28.51 | 63.25 | 3.73 | 5.61 |
| Additional run 1 | 31.15 | 64.33 | 8.35 | 11.20 |
| Additional run 2 | 31.57 | 65.28 | 8.14 | 11.04 |
| Additional run 3 | 30.17 | 63.28 | 7.94 | 11.08 |
| Additional 3 runs combined | 30.96 | 64.45 | 8.14 | 11.12 |
| All 4 runs combined | 30.35 | 64.21 | 7.04 | 10.81 |

The additional-run mean is 30.96→8.14ms, a 73.7% reduction, with every measured response HTTP 200.<br>
Pooled p95 was recomputed from individual request values, not averaged across run percentiles.<br>
Additional after means of 8.35/8.14/7.94ms exceed the initial screenshot's 3.73ms; the reason was not isolated.

Before indexes: k6

![Before indexes: k6](../../images/evidence/filter-index-1.png)

After indexes: k6

![After indexes: k6](../../images/evidence/filter-index-2.png)

---

## Repeated results by filter and order

Additional runs only: 270 requests per case per state.<br>
Values are milliseconds.<br>
Cases have equal weight, not production traffic proportions.

| Filter / order | Before mean | Before p95 | After mean | After p95 |
| --- | --- | --- | --- | --- |
| No filter / ID | 8.33 | 12.03 | 7.50 | 10.21 |
| No filter / Lowest price | 63.08 | 67.30 | 7.29 | 10.21 |
| No filter / Highest price | 63.30 | 66.59 | 7.87 | 10.36 |
| Parent category / ID | 9.71 | 12.64 | 9.38 | 11.74 |
| Parent category / Lowest price | 59.53 | 63.07 | 8.36 | 10.51 |
| Parent category / Highest price | 59.27 | 62.17 | 7.96 | 10.34 |
| Brand / ID | 7.99 | 10.38 | 7.49 | 10.01 |
| Brand / Lowest price | 14.07 | 17.16 | 7.64 | 9.99 |
| Brand / Highest price | 14.07 | 17.43 | 7.37 | 9.41 |
| Gender / ID | 7.71 | 10.03 | 7.20 | 9.72 |
| Gender / Lowest price | 63.56 | 66.97 | 7.64 | 10.24 |
| Gender / Highest price | 63.79 | 67.05 | 7.49 | 11.19 |
| Parent category + Brand / ID | 8.31 | 11.58 | 9.36 | 11.74 |
| Parent category + Brand / Lowest price | 13.89 | 16.90 | 7.19 | 9.65 |
| Parent category + Brand / Highest price | 13.80 | 16.26 | 7.74 | 10.07 |
| Parent category + Gender / ID | 9.58 | 11.66 | 9.29 | 12.01 |
| Parent category + Gender / Lowest price | 61.30 | 65.24 | 7.93 | 11.12 |
| Parent category + Gender / Highest price | 62.28 | 68.44 | 7.76 | 10.53 |
| Brand + Gender / ID | 7.48 | 10.43 | 7.73 | 10.43 |
| Brand + Gender / Lowest price | 14.17 | 18.03 | 7.13 | 9.83 |
| Brand + Gender / Highest price | 13.87 | 16.54 | 7.41 | 10.07 |
| Parent category + Brand + Gender / ID | 8.39 | 11.09 | 9.27 | 11.33 |
| Parent category + Brand + Gender / Lowest price | 14.49 | 17.12 | 7.96 | 10.41 |
| Parent category + Brand + Gender / Highest price | 13.95 | 16.79 | 7.78 | 10.35 |
| Leaf category / ID | 10.02 | 13.17 | 9.62 | 12.00 |
| Leaf category / Lowest price | 54.85 | 58.51 | 8.28 | 10.78 |
| Leaf category / Highest price | 55.61 | 59.70 | 8.34 | 11.45 |
| Multiple categories / ID | 10.86 | 13.84 | 10.20 | 13.04 |
| Multiple categories / Lowest price | 60.87 | 64.57 | 8.81 | 11.09 |
| Multiple categories / Highest price | 60.79 | 64.40 | 9.27 | 12.38 |

Price ordering improved substantially, while some ID-ordered combinations increased slightly.<br>
Not every case improved.

---

## Execution plan: examined rows and sorting

The plan reads bags and descendants in ascending price/ID order with LIMIT 25.<br>
`IGNORE INDEX (idx_product_price)` supplies the comparison without the price index.

Plan without price index

![Plan without price index](../../images/evidence/filter-index-3.png)

Plan with price index

![Plan with price index](../../images/evidence/filter-index-4.png)

| Metric | Without index | With index |
| --- | --- | --- |
| Recorded actual time (ms) | 0.05..73.9 | 0.25..1.41 |
| Recorded end time | 73.9ms | 1.41ms — 98.09% lower |
| Access | Full product table scan | Price index range scan |
| Scan actual rows | 155,036 | 343 — 99.78% fewer |
| Filter output rows | 39,891 | 25 |
| Separate sort input | 39,891 filtered rows | None — index price order |
| Returned rows | 25 | 25 |

Reading in index price order and stopping after 25 qualifying rows reduced scanned rows from 155,036→343 and removed separate sorting.<br>
These actual times describe one SQL plan, not the 30.96→8.14ms mean API comparison.<br>
Row counts come from the equivalent-condition plan record; timings come from the attached rerun.

---

## Why these indexes

One path fixes availability before price/ID ordering; the other fixes brand and availability first.<br>
Descending price/ID can use reverse traversal.<br>
The former brand-only index narrowed the brand range without providing price order.

ID ordering already benefits from the PK, so it should not show the same reduction.<br>
Rare category/gender matches can require examining many entries to obtain 25 results.<br>
Individual contributions of the two indexes were not isolated.

<br>

### Why the category candidate was excluded

`(is_available, category_path, default_price, product_id)` orders price within each category path.<br>
It does not directly provide global price order across multiple descendant paths in a prefix query.<br>
For bags, the price index found 25 results after 343 rows; forcing the category candidate sent 39,891 rows to sorting.<br>
That is post-index-condition output, not the count of all inspected index entries.

For the same 4,707 tote-bag candidates and top 25 results:

| Predicate | Selected index | Read → returned rows |
| --- | --- | --- |
| LOWER + LIKE | Price | 730 → 25 |
| LIKE without LOWER | Price | 730 → 25 |
| LOWER + equality | Price | 730 → 25 |
| Equality without LOWER | Category composite | 25 → 25 |

Removing LOWER alone was insufficient.<br>
The candidate helped leaf-category equality, but was not retained for the existing parent/descendant behavior.

---

## Trade-offs and measurement limits

Indexes consume storage/buffer-pool space and add write-maintenance costs.<br>
Two price paths were prioritized instead of indexing every combination.<br>
Availability selectivity alone does not explain the gain; ordering and LIMIT matter together.<br>
The brand composite replaced the FK-supporting single-column index while preserving the constraint.

The same running server/restored DB ran three without-index executions followed by three with-index executions.<br>
Warm-up was used without restarting or clearing caches.<br>
The 256MiB MySQL buffer pool and SQL logging/monitoring settings stayed unchanged.<br>
Write costs, body correctness, deep pages, and concurrency remain separate.<br>
The final two indexes were restored and the category candidate removed after measurement.
