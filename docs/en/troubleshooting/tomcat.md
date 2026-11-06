# Product detail: Tomcat thread count and connection waits

[한국어](../../ko/troubleshooting/tomcat.md) · [README](../../../README.md)

---

## Problem and test conditions

Even after Hikari 50 reduced connection waits, throughput at 3,000 RPS stayed at about 2,100 RPS, and busy Tomcat threads were capped at the limit of 200.<br>
To see whether more threads would raise throughput, I fixed Hikari at 50 and compared Tomcat 50, 200, and 500.

I rotated through 5 products and ran each setting once with a 1,000 VU limit and a 5-second timeout.<br>
The load used the same stages as Hikari experiment 2 (50 → 1,000 → 1,500 → 2,000 → 3,000 → 50 RPS).

---

## Tomcat 50, 200, 500: thread count and connection waits

| Hikari fixed at 50 | Tomcat 50 | 200 | 500 |
| --- | ---: | ---: | ---: |
| 2,000 RPS throughput (RPS) | 1,999 | 1,998 | 1,998 |
| 2,000 RPS mean (ms) | 51.2 | 77.2 | 73.3 |
| 2,000 RPS p95 (ms) | 158.4 | 265.9 | 238.2 |
| 2,000 RPS p99 (ms) | 253.9 | 434.6 | 446.3 |
| 3,000 RPS throughput (RPS) | 2,112 | 2,121 | 2,063 |
| 3,000 RPS mean (ms) | 472.0 | 469.4 | 482.9 |
| 3,000 RPS p95 (ms) | 613.3 | 632.6 | 901.6 |
| 3,000 RPS p99 (ms) | 724.3 | 758.3 | 1,312.9 |
| 3,000 RPS avg. connections in use | 49.6 | 49.8 | 49.9 |
| 3,000 RPS avg. waiting for a connection | 0 | 149.8 | 449.9 |
| 3,000 RPS avg. Tomcat busy | 50 | 200 | 500 |
| 3,000 RPS avg. host CPU | 96.6% | 96.6% | 96.8% |
| Total drops | 57,148 | 57,291 | 61,498 |

Raising Tomcat from 200 to 500 increased connection waits from about 150 to 450, but the work actually using DB connections stayed at about 50 and throughput didn't go up.<br>
If anything, p95 at 3,000 RPS got longer, from 632.6ms to 901.6ms.<br>
Tomcat 50, on the other hand, had no connection waits, but its throughput was also about 2,100 RPS, and at 2,000 RPS it had the lowest p95 of the three settings.

More threads lengthened the queue in front of the connection pool, and fewer threads just moved that queue to the request queue; throughput didn't change either way.

---

## Assessment

I kept Tomcat at 200.<br>
At 3,000 RPS the host CPU went above 96% regardless of thread count and throughput stopped at about 2,100 RPS, and more threads only added waiting and latency.<br>
Tomcat 50 had the lowest latency for the detail API on its own, but I didn't check whether that holds when it shares the server with other APIs, so I kept 200.

Settings can decide whether requests wait in the connection pool or in the request queue, but how much can be processed is set by this machine's CPU.
