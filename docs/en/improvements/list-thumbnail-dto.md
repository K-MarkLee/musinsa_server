# Product list: thumbnail storage and direct DTO projection

[한국어](../../ko/improvements/list-thumbnail-dto.md) · [README](../../../README.md) · [Measurement setup](../environment.md)

---

## Problem and comparison conditions

After stored prices and keyset, lists still loaded Product entities, searched each image collection for a thumbnail, and copied fields into DTOs.<br>
Unloaded image relationships could cause additional SQL.<br>
DTOs already existed; their construction path changed here.

Available products were read in ascending ID order, 24 at a time, using fixed cursors at the first page and depths 100/1,000/5,000.<br>
Each position used one initial call, four warm-ups, and 30 measured calls, repeated three times.<br>
Each execution measured 360 requests; three additional runs produced 1,080 samples per version.<br>
Before values reuse the [keyset remeasurement](list-keyset.md).<br>
Image downloads and browser rendering are excluded.

---

## Repeated measurements

| Run | Before mean | Before p95 | After mean | After p95 |
| --- | --- | --- | --- | --- |
| Initial screenshot | 19.32ms | 25.44ms | 6.09ms | 8.42ms |
| Additional run 1 | 17.51ms | 22.48ms | 5.35ms | 7.92ms |
| Additional run 2 | 16.17ms | 18.86ms | 4.73ms | 6.22ms |
| Additional run 3 | 15.88ms | 17.95ms | 4.20ms | 5.36ms |
| Additional 3 runs combined | 16.52ms | — | 4.76ms | — |

The additional runs averaged 16.52→4.76ms, a 71.2% reduction, excluding the initial screenshot.<br>
All 1,080 after samples succeeded; means/p95 came from individual InfluxDB request durations.<br>
The server was already running and caches were not cleared.<br>
After means fell from 5.35→4.73→4.20ms across runs.<br>
Reusing the earlier baseline does not isolate warm-up or execution-order effects completely.

Before: keyset read path

![Before: keyset read path](../../images/evidence/list-keyset-2.png)

After thumbnail/DTO change

![After thumbnail/DTO change](../../images/evidence/list-dto-2.png)

*The source page reused the keyset before image (19.26ms), while its initial DTO table records 19.32ms.<br>
These are not forced into one aggregate; both are excluded from the additional-three-run mean.*

---

## Why it became faster

```text
Before: DB → Product entity → image collection / thumbnail lookup → ProductSummary → JSON
After: selected DB columns → ProductSummary → JSON
```

Thumbnail denormalization retains the image table and also stores one URL in `thumbnail_image`; it does not copy an image file.<br>
Creation/update prefers the thumbnail-marked image, falling back to the first image.

Direct DTO projection constructs `ProductSummary` in the application from returned columns.<br>
The DB does not construct Java objects; DTO allocation and JSON serialization remain.<br>
It avoids the intermediate Product entity and image traversal.<br>
QueryDSL can query entities or DTOs, so syntax alone is not the explanation.

```java
// Simplified projection; the actual response contains more fields.
.select(Projections.constructor(
    ProductSummary.class,
    product.productId,
    product.productName,
    product.defaultPrice,
    product.thumbnailImage
)).from(product)
```

Stored thumbnails and direct projection could be applied independently but changed together here.<br>
SQL counts, allocations, GC reductions, and individual contributions were not measured.<br>
Description and other columns remain, so this is not response-size minimization.<br>
A retained mapper method does not imply the list path still calls it.

---

## Decisions and trade-offs

- Duplicated thumbnail: Backfill existing products and synchronize source/summary URLs on replacement, deletion, or concurrent edits.<br>
  Otherwise list and detail images can differ.
- Query/response coupling: Selected columns must match DTO constructor order and types; response changes can require repository changes.
- Defaults and updates: The former mapper's `defaultPrice=null → 0` handling does not automatically carry over.<br>
  Changing a DTO does not update the DB.<br>
  Check null/missing values as well as normal response data.

Response IDs/order/count/prices/URLs/cursors and image-SQL reduction need explicit validation.<br>
Stored prices, COUNT removal, and keyset are common conditions and must not be counted again as this change's gain.
