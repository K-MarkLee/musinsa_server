# Product list: representative price and stock calculation

[한국어](../../ko/improvements/list-price.md) · [README](../../../README.md) · [Measurement setup](../environment.md)

---

## Problem and comparison conditions

The list needs one representative price, but response assembly previously traversed each product's options and inventory to calculate its minimum price and stock availability.<br>
This reads relationships for the 24 returned products, not all 11.16 million options on every request.

The request was `GET /api/products?page=0&size=24`, without keywords or price ordering.<br>
Each version ran three groups of one initial call, four warm-up calls, and 30 sequential measured calls with 1 VU: 105 total requests and 90 measured samples per version.<br>
All measured requests returned HTTP 200.

Both implementations used the same restored final DB and a 256MiB MySQL buffer pool.<br>
The app restarted between versions, but neither the server nor DB restarted between repeats, and DB caches were not cleared.<br>
This reproduces implementation differences, not every historical DB/index condition.

---

## Repeated measurements

| Run | Before mean | Before p95 | After mean | After p95 |
| --- | --- | --- | --- | --- |
| Additional run 1 | 852.25ms | 955.68ms | 63.88ms | 73.83ms |
| Additional run 2 | 880.45ms | 1,032.82ms | 63.15ms | 70.65ms |
| Additional run 3 | 848.70ms | 957.91ms | 62.16ms | 66.78ms |
| Initial screenshot | 747.17ms | 838.46ms | 64.02ms | 72.31ms |
| Additional 3 runs combined | 860.47ms | — | 63.06ms | — |

The additional three runs averaged 860.47→63.06ms, a 92.7% reduction.<br>
The initial screenshot is excluded from this aggregate.<br>
Additional-run p95 values came from k6; the initial value is from Grafana.<br>
No average of p95 values is presented as a pooled percentile.

List before

![List before](../../images/evidence/list-price-1.png)

List after

![List after](../../images/evidence/list-price-2.png)

---

## Why it was slow and what changed

The mapper found the lowest price among in-stock options, falling back to all options if none had stock, and calculated `hasStock` separately.<br>
Accessing unloaded relationships could repeat SQL calls, DB round trips, result transfer, object creation, and mapping.

The new path reads `default_price` and removes the list's stock calculation, avoiding option/inventory traversal for those two values.<br>
This is the combined effect of stored price and removed stock calculation; SQL and object-processing contributions were not timed separately.<br>
Price sorting also changed to the stored column, but that improvement is outside this unsorted request.

With a fixed request count and sequential 1-VU execution, faster responses shorten the run.<br>
That is not evidence of missing samples or established maximum throughput.<br>
COUNT, OFFSET, and per-product image reads still existed at this comparison point.

---

## Decisions and trade-offs

- Source/summary consistency: Creation and addition of a cheaper option were confirmed to set/lower the representative price.<br>
  This does not cover every price or inventory change.
- Falling and rising prices differ: With options priced at 10,000 and 12,000, adding a 9,000 option needs a comparison.<br>
  Deleting the 10,000 option or raising it to 13,000 requires checking the remaining options to select 12,000.<br>
  Stock-out, restocking, and all-sold-out display policies also matter.
- Update timing: Updating both values in one transaction helps consistency but adds write, locking, and concurrent-update costs.<br>
  Async/batch updates allow stale reads and require recovery.<br>
  These alternatives were not benchmarked.
- Changed response contract: `hasStock` changes from a stock indicator to `null`.<br>
  Clients displaying availability need an alternative; this is not an optimization preserving every response meaning.

---

## Limits and follow-up

Every measured option had stock.<br>
Price increases, deletion, stock-out/restocking, and concurrent updates remain unvalidated.<br>
Detail, deep pages, price sorting, and concurrent load are separate.<br>
The next [COUNT/keyset comparison](list-keyset.md) already includes this stored-price change.
