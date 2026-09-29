# Product detail: Hikari expansion and the MySQL connection limit

[한국어](../../ko/troubleshooting/hikari.md) · [README](../../../README.md) · [Measurement setup](../environment.md)

Hikari pending and Tomcat busy increased together on the detail API.<br>
Holding Tomcat at 200, I varied only the DB pool to test whether connection acquisition constrained processing.

---

## Experiment order and controls

Five fixed products were requested in rotation.<br>
k6 preallocated/maximum VUs were 1,000, request timeout five seconds, and each setting ran once with the same read logic and shared local environment.<br>
Performance-triggered early aborts were disabled to observe recovery.

1. Hold Tomcat at 200; compare Hikari 10, 20, 30, and 50.<br>
   Holds: 50 RPS for 30s → 1,000 for 45s → 1,500 for 60s → 2,000 for 75s → 50 for 30s.<br>
   Fifteen-second ramps give five minutes total.
2. Add a 15-second ramp and 60-second hold at 3,000 RPS after 2,000.<br>
   Compare Hikari 50, 100, and 200 in this six-minute-15-second scenario.

Throughput approximates completed requests tagged for a hold divided by its duration.<br>
Metric averages use one-second queries over that hold.<br>
Drops below are whole-run totals, not hold-only totals.<br>
Screenshot summaries cover their selected ranges and can differ from stage tables.

---

## Observation behind the hypothesis

With pool ten at 1,500 RPS, average pending reached approximately 188 and Tomcat busy approximately 198.<br>
Requests waiting for DB connections can still occupy Tomcat workers; busy at its cap does not by itself establish a shortage of workers.<br>
Tomcat stayed at 200 while Hikari changed to investigate connection acquisition.

---

## 1. Hikari 10→50: less waiting, fewer occupied Tomcat threads

| 2,000 RPS · 75s | Hikari 10 | 20 | 30 | 50 |
| --- | ---: | ---: | ---: | ---: |
| Approx.<br>throughput, RPS | 1,381 | 1,686 | 1,814 | 1,999 |
| Response p95, ms | 875.2 | 786.3 | 731.1 | 58.6 |
| Mean pending | 189.8 | 179.8 | 169.9 | 10.9 |
| Mean Tomcat busy | 200 | 200 | 200 | 34.7 |
| Mean acquisition time, ms | 137.1 | 106.4 | 93.2 | 4.5 |
| Whole-run drops | 55,382 | 22,948 | 13,281 | 571 |

<br>

### Hikari 10 results

Hikari 10 load result

![Hikari 10 load result](../../images/evidence/hikari-1.png)

Hikari 10 connection waiting

![Hikari 10 connection waiting](../../images/evidence/hikari-2.png)

<br>

### Hikari 20 results

From ten to 20, 1,500-RPS p95 fell from 798.3→53.6ms.<br>
Sustained saturation began later, but pools 20/30 still encountered connection/Tomcat waiting at 2,000 RPS.

Hikari 20 · k6

![Hikari 20 · k6](../../images/evidence/hikari-3.png)

Hikari 20 · Hikari

![Hikari 20 · Hikari](../../images/evidence/hikari-4.png)

<br>

### Hikari 30 results

Hikari 30 · k6

![Hikari 30 · k6](../../images/evidence/hikari-5.png)

Hikari 30 · Hikari

![Hikari 30 · Hikari](../../images/evidence/hikari-6.png)

<br>

### Hikari 50 result

Hikari 50 load result

![Hikari 50 load result](../../images/evidence/hikari-7.png)

Hikari 50 connection observation

![Hikari 50 connection observation](../../images/evidence/hikari-8.png)

Mean busy fell from 200 to 34.7 without changing Tomcat, supporting the hypothesis that much of the occupancy involved DB connection acquisition.<br>
The decision used throughput, latency, and waiting duration, rather than just peak pending.

Pending zero in the screenshot did not mean zero waiting throughout the run.<br>
A one-second query revealed pending 150 around 19:20:48–19:20:55, missed by 15/30-second graph queries.<br>
A brief saturation around elapsed seconds 254–258 left 571 drops.<br>
The connection chart is stacked; read individual legend/tooltip values rather than line heights.

---

## 2. Hikari 50→100→200: reduced waiting did not scale throughput

Hikari 50 below is a separate extended run.<br>
Its 2,000-RPS p95 was 265.9ms, not the earlier five-minute run's 58.6ms.

| 3,000 RPS · 60s | Hikari 50 | 100 | Configured 200 |
| --- | ---: | ---: | ---: |
| Maximum actual connections | 50 | 100 | 152 |
| Approx.<br>throughput, RPS | 2,121 | 2,134 | 2,058 |
| Mean response, ms | 469.4 | 466.9 | 483.8 |
| Response p95, ms | 632.6 | 604.9 | 597.2 |
| Mean acquisition time, ms | 70.4 | 46.6 | 23.1 |
| Mean connection usage time, ms | 23.4 | 46.6 | 73.5 |
| Mean pending | 149.8 | 99.8 | 47.8 |
| Mean Tomcat busy | 200 | 200 | 200 |
| Mean host CPU | 96.6% | 97.0% | 97.5% |
| Whole-run drops | 57,291 | 56,050 | 62,819 |

From 50 to 100, acquisition became faster but connection usage roughly doubled, with only approximately 0.6% more throughput.<br>
Throughput remained near 2,100 RPS while the host shared by the application and DB exceeded 96% CPU.

<br>

### Hikari 100: less waiting, little throughput gain

Hikari 100 · k6

![Hikari 100 · k6](../../images/evidence/hikari-9.png)

Hikari 100 · connections

![Hikari 100 · connections](../../images/evidence/hikari-10.png)

<br>

### Hikari 200: actual connection limit

Load with Hikari configured to 200

![Load with Hikari configured to 200](../../images/evidence/hikari-11.png)

Actual connections limited to 152

![Actual connections limited to 152](../../images/evidence/hikari-12.png)

With MySQL `max_connections=151`, actual connections reached 152 and exporter logs reported `Error 1040: Too many connections`.<br>
The limit was reached at 152, including the additional administrative connection.<br>
[MySQL connection-limit documentation](https://dev.mysql.com/doc/refman/8.0/en/too-many-connections.html)

Prometheus exporter `up=1` remained true while DB connectivity `mysql_up=0`.<br>
Missing MySQL metrics meant collection failure, not an idle DB.<br>
The connection budget must include operations and monitoring clients.

---

## Decision and trade-offs

Hikari 50 remains the local comparison baseline.<br>
Increasing from ten to 50 improved the 2,000-RPS stage, but the initial run still had 571 drops.<br>
From 50 to 100, throughput at a 3,000-RPS target barely changed; configured 200 reached the DB connection limit.

Less pool waiting does not necessarily increase system capacity.<br>
Once further pool growth had little effect, the investigation shifted toward connection usage time and shared resources.<br>
The worker-thread comparison follows in the [Tomcat experiment](tomcat.md).

Connection budgeting must include pools across all application instances and headroom for administration and monitoring.<br>
Filling the DB connection limit with service connections can prevent metric collection.
