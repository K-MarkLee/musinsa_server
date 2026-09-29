# Product list load results

[한국어](../../ko/load-tests/product-list.md) · [README](../../../README.md) · [Measurement setup](../environment.md)

List repeatedly requested the first 24 products without a keyword, filters, or price ordering.<br>
Completed the 1,000-RPS hold for 120s, with zero HTTP failures and no recorded drops.

Product list k6

![Product list k6](../../images/evidence/load-list-1.png)

---

## Test conditions

This is one final run from September 23, 2026.<br>
The planned seven-minute scenario ramps from a 10-RPS warm-up through 50/100/250/500/750/1,000 RPS, holding 1,000 for 120 seconds.<br>
Maximum VUs: 200; HTTP timeout: five seconds; Hikari limit: ten.<br>
APIs were not loaded simultaneously.

---

## Whole-run responses

| Response samples | Mean | p95 | Maximum |
| ---: | ---: | ---: | ---: |
| 224,540 | 2.94ms | 5.42ms | 181.10ms |

Whole-run results include warm-up and ramps.<br>
Distinguish them from stage tables and selected dashboard ranges.

---

## Results by load stage

| Hold stage | Hold duration | Response samples | Mean | p95 |
| --- | --- | --- | --- | --- |
| 50 RPS | 30s | 1,500 | 7.02ms | 10.30ms |
| 100 RPS | 30s | 2,997 | 5.07ms | 7.38ms |
| 250 RPS | 30s | 7,496 | 3.58ms | 5.28ms |
| 500 RPS | 30s | 14,995 | 4.22ms | 5.50ms |
| 750 RPS | 60s | 44,983 | 2.68ms | 3.92ms |
| 1,000 RPS | 120s | 119,955 | 2.64ms | 4.26ms |

---

## Connections and resources

Spring Boot / Hikari connection observations

![Spring Boot / Hikari connection observations](../../images/evidence/load-list-2.png)

These maxima come from one-second queries in the same run and need not coincide.<br>
System CPU is not MySQL-only CPU.

| Metric | Result in the same run |
| --- | --- |
| Hikari connection limit | 10 |
| Maximum Hikari active / pending | 10 / 53 |
| Connection timeout increase | 0 |
| Maximum JVM / system CPU | 18.80% / 87.57% |
| Maximum MySQL running threads | 4 |

Raw metrics were queried at one-second intervals across the run and immediate cleanup.<br>
The attached graph can use coarser resolution and miss brief pending peaks.<br>
A displayed pending=0 differs from the run's maximum; maxima across metrics need not occur simultaneously.

---

## Request trace sample

Jaeger request trace

![Jaeger request trace](../../images/evidence/load-list-3.png)

| Span / timing | Time in the trace sample |
| --- | --- |
| HTTP total | 9.25ms |
| DB read call | 8.95ms |
| DB call start | 0.176ms after request start |

In one trace, the DB call accounted for most elapsed time.<br>
The span can include connection acquisition and repository work, so it is not pure SQL execution time.<br>
Brief pending spikes alone do not establish a need to increase the pool for this workload.

---

## Interpretation and measurement scope

This single-API test repeats fixed inputs and includes cache/JIT warm-up and shared local resources.<br>
Lower latency at a later, higher-load stage is not an improvement caused by higher load itself.<br>
HTTP 200 checks do not validate every response field.<br>
One trace does not represent the population mean/p95, and DB-call spans are not pure SQL execution time.

---

## Validated load range and limits

The run completed 1,000 RPS for 120 seconds.<br>
Higher load was not tested in this baseline, so maximum throughput is unestablished.
