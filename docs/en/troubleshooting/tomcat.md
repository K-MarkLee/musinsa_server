# Product detail: Tomcat workers and connection waiting

[한국어](../../ko/troubleshooting/tomcat.md) · [README](../../../README.md) · [Measurement setup](../environment.md)

After Hikari 50 reduced connection waiting, throughput still plateaued near 2,100 RPS at a 3,000-RPS target.<br>
With Tomcat busy reaching its cap, I tested whether more worker threads increased throughput.

---

## Scenario and comparison

Five products were requested in rotation with Hikari 50, 1,000 preallocated/maximum VUs, and a five-second HTTP timeout.<br>
Tomcat 50/200/500 each ran once.<br>
Holds were 50 RPS for 30s → 1,000 for 45s → 1,500 for 60s → 2,000 for 75s → 3,000 for 60s → 50 for 30s, with 15-second ramps: six minutes 15 seconds total.<br>
Performance-triggered early aborts were disabled.

Tomcat 200 reuses the Hikari-50 run of this extended scenario, not the initial five-minute run.<br>
Throughput approximates hold-stage completed requests divided by duration; metric means use one-second queries over the hold.<br>
Whole-run drops are not limited to the 3,000-RPS stage.

---

## Tomcat 50/200/500: more threads waiting for the same connections

| Hikari fixed at 50 | Tomcat 50 | 200 | 500 |
| --- | ---: | ---: | ---: |
| Approx.<br>throughput at 2,000 RPS | 1,999 | 1,998 | 1,998 |
| 2,000 RPS mean, ms | 51.2 | 77.2 | 73.3 |
| p95 at 2,000 RPS, ms | 158.4 | 265.9 | 238.2 |
| 2,000 RPS p99, ms | 253.9 | 434.6 | 446.3 |
| Approx.<br>throughput at 3,000 RPS | 2,112 | 2,121 | 2,063 |
| Mean at 3,000 RPS, ms | 472.0 | 469.4 | 482.9 |
| p95 at 3,000 RPS, ms | 613.3 | 632.6 | 901.6 |
| p99 at 3,000 RPS, ms | 724.3 | 758.3 | 1,312.9 |
| Mean Hikari active | 49.6 | 49.8 | 49.9 |
| Mean Hikari pending | 0 | 149.8 | 449.9 |
| Mean Tomcat busy | 50 | 200 | 500 |
| Mean host CPU | 96.6% | 96.6% | 96.8% |
| Whole-run drops | 57,148 | 57,291 | 61,498 |

The four metric-average rows describe the 3,000-RPS hold.<br>
From 200 to 500, pending rose from approximately 150 to 450 while active DB connections remained approximately 50.<br>
Throughput did not improve; p95 rose from 632.6 to 901.6ms.

Tomcat 50 had zero Hikari pending but only approximately 2,112 RPS.<br>
All three runs had zero HTTP failures and connection timeouts, but retained drops.<br>
Reducing Hikari waiting alone did not achieve the target throughput.

---

## Decision

Increasing Tomcat from 200 to 500 added waiting and raised p95 without increasing throughput.<br>
I reverted the increase and kept 200 as the local baseline.<br>
Tomcat 50 had the lowest p95 in this detail scenario, making a smaller pool a candidate for mixed-API comparison.

At 2,000 RPS, throughput remained around 1,999 RPS across settings while mean/p95/p99 differed.<br>
At 3,000 RPS, extra workers did not expand the processing capacity of approximately 50 active connections and increased waiting.<br>
Limiting workers to lower Hikari pending is not the same as eliminating end-to-end waiting; throughput, latency, and drops must be assessed together.
