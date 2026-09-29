# Limitations and Follow-up Work

[한국어](../ko/future-work.md) · [README](../../README.md) · [Measurement setup](environment.md)

The completed work examines read changes and local load behavior against prepared, fixed data.<br>
Follow-up work focuses on the deployed environment, update consistency, dictionary storage, and ES document structure.

---

## Deployed-environment validation

Locally, k6, the application, MySQL, Redis, ES, and observability share CPU, memory, and I/O on one machine.<br>
On Azure, separate the load generator from service resources and examine how the deployed network and resource layout affect latency and throughput.

| Area | Validation |
| --- | --- |
| Deployment | Record placement, CPU, memory, disks, and major versions for the app, DB, Redis, ES, and observability |
| Resources and waiting | Observe JVM, Hikari, Tomcat, ES HTTP pools, and DB/ES/generator CPU, memory, and I/O |
| Representative reads | Run scenarios already examined locally; record target RPS, actual throughput, hold duration, p95, HTTP failures, and drops |
| Environment differences | Record data, queries, cache/pool settings, and network differences when comparing bottlenecks |

Do not combine local and Azure numbers into one before/after improvement ratio.<br>
Record the plan before deployment and actual conditions, results, and limits after execution.

[Current setup and workload criteria](environment.md) · [Hikari investigation](troubleshooting/hikari.md) · [Search investigation](troubleshooting/search-capacity.md)

---

## Update consistency

Representative price/thumbnail fields, Redis dictionaries, and ES documents have storage and update paths separate from the source.<br>
Fixed-data read results do not validate propagation and recovery across those paths.

| Target | Consistency work |
| --- | --- |
| List representative fields | Define how option-price, availability, and image changes/deletions recompute and update price/thumbnail fields |
| Redis dictionary | Define refresh/invalidation timing, missing-entry/failure behavior, and visibility differences across instances |
| DB→ES propagation | Validate visibility lag, failures, duplicate/out-of-order updates, and retries for product/option/price/stock changes |
| Full reindexing | Check for missing/duplicate data during concurrent updates and validate cutover and failure recovery |

First define the source, expected updated state, and acceptable propagation delay.<br>
Then check list, detail, and search under normal updates and propagation failures/retries.<br>
ES refresh settings alone do not propagate DB changes.

Completing collection/preprocessing and reproducing the dataset also remain work.<br>
The plan covers reproducible collection, cleaning, and option expansion, with explicit sources for updates to derived data.

[Representative price and response contract](improvements/list-price.md) · [Detail cache scope](improvements/product-detail.md) · [Search indexing](improvements/product-search.md)

---

## Option dictionary: Redis vs. local memory vs. RDB

The comparison concerns 60 color/size dictionary entries, not full product responses or price/stock caches.<br>
Relationship reads and bulk dictionary fetching changed together, so the entire detail improvement is not isolated as an effect of choosing Redis.

| Approach | Expected benefit | Costs and constraints |
| --- | --- | --- |
| Redis bulk reads | A shared dictionary and update point across servers | Network round trips, serialization, dependency failures, and missing entries |
| In-process memory | Remove per-request network dictionary reads | Per-server loading, refresh/invalidation, and deployment timing differences |
| RDB joins / bulk reads | Read with source data and reduce separate cache dependencies | DB round trips, joins, string transfer, mapping, and DB load |

The RDB can serve data from its buffer pool; do not assume a disk read per request.<br>
Hold the remaining query structure, response fields/order, and dataset constant while changing the dictionary-read path.

Compare cold/warm latency, detail p95, external calls, memory, update visibility, and failure behavior.<br>
The current Redis dictionary has no TTL or DB fallback, so define miss/failure policies through the consistency work above and evaluate equivalent expected responses.<br>
If performance differences are small, implementation and operating simplicity also matter.

[Current detail reads and cache scope](improvements/product-detail.md)

---

## ES option documents vs. product-root nested modeling

Currently each option is an ES document, shared product fields repeat, and `productId` collapse groups results.<br>
Build a product-root model with nested options and compare it against the same search requirements.

| Comparison | What to validate |
| --- | --- |
| Option combinations and responses | Same-option color/size matching, product deduplication, ordering, pagination, and relevance |
| Read costs | Mean/p95, throughput, ES CPU, and search queues for equivalent queries and load |
| Update costs | Reindexing scope/time and visibility lag for price, inventory, and option changes |
| Storage and operations | Index size, full rebuild, cutover, and failure recovery |

Keep option-combination predicates within the same nested query.<br>
Match queries, data, weights, and load to examine document structure, distinguishing any changes in response semantics.<br>
Internal option documents and query/scoring work remain with nested modeling; document counts or collapse removal alone do not establish a performance advantage.

[Current search design](improvements/product-search.md) · [Pagination constraints](troubleshooting/search-pagination.md) · [Search load observations](troubleshooting/search-capacity.md)
