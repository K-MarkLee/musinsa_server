# Search under load: isolate transactions, HTTP pools, and ES work

[한국어](../../ko/troubleshooting/search-capacity.md) · [README](../../../README.md) · [Measurement setup](../environment.md)

Search aborted around three minutes because of `dropped_iterations`.<br>
Initially, delay appeared before the service body; later it appeared inside the ES client call.<br>
I investigated DB transaction scope, HTTP connection limits, and then ES CPU usage and queued search tasks.

---

## Initial diagnostic conditions and stages

Four queries rotated with Hikari ten, maximum 200 VUs, and a five-second HTTP timeout.<br>
After a 10-RPS 30-second warm-up, planned holds were 50/100/250/500 RPS for 30 seconds each, 750 for 60 seconds, and 1,000 for 120 seconds, with 15-second ramps.<br>
Jaeger sampling was 1%; initial ES monitoring used ten-second intervals.<br>
The run aborted during the ramp to 500.

| Stage | Completed requests | Mean | p95 |
| --- | --- | --- | --- |
| 50 RPS hold | 1,500 | 19.50ms | 46.62ms |
| 100 RPS hold | 3,000 | 19.01ms | 47.09ms |
| 250 RPS hold | 7,500 | 19.29ms | 48.13ms |
| Ramp to 500 RPS, aborted | 3,369 | 208.46ms | 618.87ms |

HTTP failures were zero, but 179 drops triggered an abort.<br>
This is the initial transaction investigation, separate from later baseline and HTTP-pool comparisons.

---

## 1. ES search inherited a DB transaction

Class-level `@Transactional(readOnly = true)` also covered ES search.<br>
Read-only does not omit a transaction or make MySQL and ES one atomic operation.<br>
An initial example trace took 623.08ms overall: 608.25ms before the service body, 10.50ms in the body, and 10.48ms in ES.<br>
In the same run, Hikari active peaked at ten and pending at 188.

To remove unnecessary DB connection occupancy during ES calls, I applied `Propagation.NOT_SUPPORTED` to `searchProducts`, retained repository transactions for keyword-free MySQL reads, and kept the detail transaction.

| Comparison | Before separation | After |
| --- | ---: | ---: |
| Maximum Hikari pending | 188 | 0 |
| Maximum ES HTTP pending | Comparable value unavailable | 185 |
| p95 at 250 RPS | 48.13ms | 45.31ms |
| p95 during ramp to 500 RPS | 618.87ms | 513.87ms |
| Whole-run drops | 179 | 55 |
| 1,000-RPS hold | Not reached | Not reached |

Hikari waiting disappeared, but a similarly sized queue remained on the existing ES HTTP path.<br>
In this search-only test, both versions stopped at the same load stage.<br>
The next investigation focused on ES HTTP connection acquisition and server processing time.

Initial Hikari active/pending

![Initial Hikari active/pending](../../images/evidence/search-trouble-2.png)

<br>

### Initial request and ES observations

| Observation | Recorded value |
| --- | --- |
| Whole HTTP request | 623.08ms |
| Before service-body entry | 608.25ms |
| Service body | 10.50ms |
| ES call | 10.48ms |
| Same-run Hikari active / pending maxima | 10 / 188 |

The Jaeger sample showed more time before service entry than inside the body.<br>
In the same run, Hikari active reached its cap and pending increased.<br>
Together, these observations directed attention to transaction scope.

Just before the abort, ten-second ES samples showed process CPU 65%, search queue two, cumulative rejected zero, heap 58%, and zero cumulative Old GC during the run.<br>
I then shortened collection intervals to observe brief peaks and changes in waiting.

---

## 2. Separate client, took, and outside time

| Tag | Scope |
| --- | --- |
| `es.client_ms` | Spring Data search call start to return, including acquisition, transmission, server work, and response conversion |
| `es.took_ms` | ES-reported server elapsed time, including search queueing and communication between nodes |
| `es.outside_took_ms` | client − took; a mixture that can include pool waiting, transmission, serialization, and client processing |

`took` is not pure CPU time.<br>
See the [ES Search API definition](https://www.elastic.co/docs/api/doc/elasticsearch/operation/operation-search).

Example client and took breakdown

![Example client and took breakdown](../../images/evidence/search-trouble-3.png)

This sample has HTTP duration 608.58ms, ES span 607.85ms, and tags 607 = 16 + 591ms.<br>
The 591ms outside the ES-reported 16ms prompted investigation of the client path alongside pool metrics.<br>
The image illustrates the three tag scopes.

---

## 3. Instrument and increase HTTP connection limits

The adjustment sequence was 10→20→30→50→100→125→150 per host.<br>
The table contains the runs aggregated by configuration.<br>
The effective-30 run configured 50 per host but only 30 total.

In dev, I exposed leased, available, and pending from the existing pool and reduced Spring scraping to one second.<br>
Recording history mattered more than repeatedly refreshing the screen.<br>
`es_http_pool_pending` sums route-level pending and can include requests creating connections.

ES HTTP connection pool · pending maximum 188

![ES HTTP connection pool · pending maximum 188](../../images/evidence/search-trouble-1.png)

The ES HTTP pending maximum in this image was 188.<br>
The following images show the initial pool and the progression of limit changes across runs.

Initial ES HTTP pool

![Initial ES HTTP pool](../../images/evidence/search-trouble-5.png)

Pool limits across multiple runs

![Pool limits across multiple runs](../../images/evidence/search-trouble-4.png)

The limit-change image spans multiple runs and ends at 150/150, with displayed leased maximum 150 and pending maximum 87.<br>
Available 150 and leased zero after completion indicate returned idle connections.<br>
Per-configuration results are summarized below.

| Tomcat | ES per-host / total | Max leased / pending | Whole-run p95 | Drops |
| ---: | --- | --- | ---: | ---: |
| 200 | 10/30 | 10/188 | 118.46ms | 132 |
| 300 | 10/30 | 10/157 | 214.40ms | 31 |
| 200 | 20/30 | 20/175 | 288.50ms | 162 |
| 200 | 50/30 | 30/166 | 173.89ms | 55 |
| 200 | 50/50 | 50/133 | 295.43ms | 97 |
| 200 | 100/100 | 100/15 | 127.67ms | 12 |
| 200 | 150/150 | 150/30 | 114.33ms | 18 |

Each setting ran once, all with zero recorded HTTP failures and last responses around seconds 172–176.<br>
p95 covers warm-up and ramps too.<br>
Pool maxima include two to three seconds of cleanup and need not occur simultaneously.<br>
These are separate runs from the transaction comparison and [seven-minute baseline search](../load-tests/product-search.md).

Changing only the per-host limit to 50 demonstrably hit the total limit of 30.<br>
Moving from 50/50 to 100/100 reduced waiting and drops, but 150/150 still aborted.<br>
The investigation then shifted to work inside ES.

---

## 4. Relax early stopping to observe internal work

The original drop threshold was:

```javascript
dropped_iterations: [{
  threshold: 'count==0',
  abortOnFail: true,
  delayAbortEval: '30s',
}]
```

The 30-second delay starts at test start, not the first drop.<br>
A drop is an iteration that could not start for lack of an available VU, not an ES rejected task.

A separate diagnostic retained only `['count==0']` for this metric, avoiding drop-triggered early stopping.<br>
With 150/150 connections, it recorded 43,200 responses and 23,419 drops over approximately 252.24 seconds.<br>
The operator stopped it manually after system CPU reached approximately 99.9% and fan noise became high.

ES CPU in docker stats

![ES CPU in docker stats](../../images/evidence/search-trouble-6.png)

Approximately 1141% ES CPU represents summed usage across approximately 11.4 cores, not a thread count.<br>
It cannot be directly added to the system CPU percentage.

<br>

### Hot threads and search queue

```http
GET /_nodes/hot_threads?type=cpu&threads=10&ignore_idle_threads=true
GET /_cat/thread_pool/search?v&h=node_name,name,active,queue,rejected,completed
```

Search-thread CPU sample

![Search-thread CPU sample](../../images/evidence/search-trouble-7.png)

Search-pool sample

![Search-pool sample](../../images/evidence/search-trouble-8.png)

Search T#7 showed cpu 96.8% and other 3.2% during a 500ms sample.<br>
The stacks included `ConjunctionDISI.nextDoc`, `Weight$DefaultBulkScorer`, `FieldComparator$RelevanceComparator`, and `SinglePassGroupingCollector`: matching option documents, calculating scores, and selecting product representatives.

| Search-pool value | Interpretation |
| --- | --- |
| active 22 | 22 internal search tasks executing, occupying the pool's slots at that point |
| queue 176 | 176 internal tasks awaiting slots, not necessarily 176 HTTP requests |
| rejected 0 | No rejection in that cumulative counter, not an absence of latency |
| completed 1,681,810 | Node-lifetime completed tasks, not this run's request count or RPS |

Active 22 means 22 search tasks were executing; queue 176 means 176 tasks were waiting for execution slots.<br>
Together with high CPU and latency, this showed that work remained queued inside ES after HTTP connections were increased.

---

## 5. Profile: multi-field matching and scoring

Deep red skirt BooleanQuery profile

![Deep red skirt BooleanQuery profile](../../images/evidence/search-trouble-9.png)

One shard's `BooleanQuery` reported 45.2ms.<br>
The `딥 레드 스커트` query expands across text fields, with whole-query boost clauses added.<br>
Matching documents and combining scores across these conditions motivates testing simpler fields, duplicate clauses, and boosts.<br>
It does not mean every token must match every field, or that every document is scanned sequentially.

The 45.2ms is the profiled query-node time for that shard.<br>
Collector output showed `QueryPhaseCollector → MultiCollector` without a separate collapse duration.<br>
[Profile measurement scope](https://www.elastic.co/docs/reference/elasticsearch/rest-apis/search-profile)

The next comparison varies collapse, fields, and boosts for the same queries while checking client/took and result quality.<br>
The [nested alternative and pagination decision](search-pagination.md) provide a related document-model option.

---

## Decision and remaining trade-offs

Narrowing transaction scope removed Hikari waiting, and expanding the HTTP pool reduced connection waiting.<br>
ES search queues and high CPU usage remained.<br>
The next targets are multi-field matching, scoring, and grouping over option documents, together with shared local resources.

Simplifying fields and clauses may improve speed but can omit results or change ranking.<br>
It needs a speed-versus-relevance comparison against representative queries.<br>
Nested product documents are a candidate for removing product-level collapse, not a direct transfer of its cost into indexing: parent/option search and update reindexing costs remain.<br>
Measurement stopped here, leaving that comparison as follow-up.
