# Product list under high load

[한국어](../../ko/load-tests/product-list.md) · [README](../../../README.md)

I repeatedly requested the first page of 24 products with no keyword, filter, or price sort.<br>
The API held 1,000 RPS for 120 seconds at p95 4.26ms, with no HTTP failures or drops.

**k6 results**

![Product list k6](../../images/evidence/load-list-1.png)

---

## Test conditions

After a 10 RPS warm-up, the load was held at 50, 100, 250, and 500 RPS for 30 seconds each, 750 RPS for 60 seconds, and 1,000 RPS for 120 seconds, with a 15-second ramp between stages.<br>
Only the list API was called, with a 200 VU limit, a 5-second HTTP timeout, and 10 Hikari connections.

---

## Results by load stage

| Stage | Duration | Responses | Mean | p95 |
| --- | --- | --- | --- | --- |
| 50 RPS | 30s | 1,500 | 7.02ms | 10.30ms |
| 100 RPS | 30s | 2,997 | 5.07ms | 7.38ms |
| 250 RPS | 30s | 7,496 | 3.58ms | 5.28ms |
| 500 RPS | 30s | 14,995 | 4.22ms | 5.50ms |
| 750 RPS | 60s | 44,983 | 2.68ms | 3.92ms |
| 1,000 RPS | 120s | 119,955 | 2.64ms | 4.26ms |

Across all 224,540 requests, including the warm-up and ramps, the mean was 2.94ms, p95 5.42ms, and max 181.10ms.

---

## Connections and resources

**Spring Boot and Hikari connections**

![Spring Boot and Hikari connections](../../images/evidence/load-list-2.png)

| Metric | Max |
| --- | --- |
| Hikari connection limit | 10 |
| Hikari active / pending | 10 / 53 |
| Hikari connection timeouts | 0 |
| JVM / system CPU | 18.80% / 87.57% |
| MySQL running threads | 4 |

These are the maximums sampled every second during the run, and system CPU covers every process running on the same machine.<br>
All 10 connections were in use and pending requests briefly reached 53, but there were no connection timeouts.

---

## Request trace

**Jaeger request trace**

![Jaeger request trace](../../images/evidence/load-list-3.png)

| Span | Time |
| --- | --- |
| Whole HTTP request | 9.25ms |
| DB query call | 8.95ms |
| DB call start | 0.176ms after the request started |

In this request, the DB query call took up most of the time.

---

## Assessment

The list stayed under p95 5ms even at 1,000 RPS, so there was no bottleneck at this load.<br>
Connections briefly queued, but that didn't lead to timeouts or higher latency, so I didn't consider it a reason to enlarge the connection pool.
