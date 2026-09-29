# Product scroll: remove COUNT and use keyset pagination

[한국어](../../ko/improvements/list-keyset.md) · [README](../../../README.md) · [Measurement setup](../environment.md)

---

## Problem and comparison conditions

Infinite scrolling needs to know whether another page exists rather than the exact total count.<br>
The stored-price implementation still ran COUNT and OFFSET: counting affected even the first page, while deeper pages added skipping work.

Available products were read in ascending ID order, 24 per page.<br>
Cursors were prepared for equivalent positions below; the test did not traverse 5,000 pages from the beginning.

| Position | Rows skipped by OFFSET | Prepared cursor |
| --- | --- | --- |
| page 0 | 0 rows | None — first page |
| page 100 | 2,400 rows | 2454 |
| page 1000 | 24,000 rows | 24700 |
| page 5000 | 120,000 rows | 123177 |

Each position used one initial call, four warm-ups, and 30 measured calls, repeated three times.<br>
One script execution has 360 measured / 420 total requests; three additional executions yield 1,080 measured samples per version on the same restored final DB.

---

## Repeated measurements

| Run | Before mean | Before p95 | After mean | After p95 |
| --- | --- | --- | --- | --- |
| Initial screenshot | 69.86ms | 96.18ms | 19.26ms | 25.02ms |
| Additional run 1 | 74.90ms | 98.94ms | 17.51ms | 22.48ms |
| Additional run 2 | 73.82ms | 98.63ms | 16.17ms | 18.86ms |
| Additional run 3 | 73.70ms | 97.22ms | 15.88ms | 17.95ms |
| Additional 3 runs combined | 74.14ms | — | 16.52ms | — |

The additional three runs averaged 74.14→16.52ms, a 77.7% reduction.<br>
The initial screenshot is excluded.<br>
Screenshot and additional-run labels distinguish their aggregation scopes.

<br>

### Mean by depth

| Position | OFFSET mean | Keyset mean |
| --- | --- | --- |
| page 0 | 65.23ms | 16.86ms |
| page 100 | 64.90ms | 16.46ms |
| page 1000 | 70.69ms | 16.63ms |
| page 5000 | 95.75ms | 16.13ms |

COUNT/OFFSET screenshot

![COUNT/OFFSET screenshot](../../images/evidence/list-keyset-1.png)

Keyset screenshot

![Keyset screenshot](../../images/evidence/list-keyset-2.png)

---

## Why it became faster

COUNT is a counting operation; a full scan is an execution strategy.<br>
Not every old SQL statement is therefore a full scan.<br>
An index does not make OFFSET an array-position jump either; the actual work depends on the plan and filters.

The new path reads `limit + 1` rows after the last ordering key.<br>
It returns 24 and uses the 25th to determine `hasNext`.<br>
Conceptually, at depth 1,000:

```sql
-- Before
SELECT COUNT(*) FROM product WHERE is_available = true;
SELECT ... FROM product WHERE is_available = true
ORDER BY product_id ASC LIMIT 24 OFFSET 24000;

-- After
SELECT ... FROM product
WHERE is_available = true AND product_id > 24700
ORDER BY product_id ASC LIMIT 25;
```

Removing COUNT also reduces first-page work; keyset avoids skipping earlier entries when a suitable index is available.<br>
Both changed together, so the 77.7% reduction is not attributed to keyset alone.<br>
Stored-price/stock changes were already common to both versions, while image reads remained.

---

## Decisions and trade-offs

- The API returns `totalCount=null`, `hasNext`, and `nextCursor`.<br>
  It trades exact page counts and arbitrary page jumps for sequential navigation.<br>
  Infinite scrolling is UI behavior; keyset is a query method.
- Equal prices require a unique ID alongside price in the cursor.<br>
  Ordering and comparison directions must agree; changing filters/order invalidates the old cursor.<br>
  This test used ID order, not price-cursor correctness.
- Selective filters or unsuitable indexes can still require many examined rows.<br>
  Mutable prices/availability can move results; keyset does not provide one snapshot across requests.
- Clients send the next cursor instead of incrementing a page number and stop at `hasNext=false`.<br>
  Invalid cursors, final pages, duplicate requests, and the actual frontend flow need further validation.

The combined mean weights the four positions equally.<br>
Short executions can finish between VU-metric scrapes while request timings remain recorded; inspect configured VUs and sample counts together.<br>
This is not a capacity test.
