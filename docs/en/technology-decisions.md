# Why These Technologies

[한국어](../ko/technology-decisions.md) · [README](../../README.md)

---

## QueryDSL — choosing how to write JPA queries

### Requirements

Optional category, gender, brand, and price-ordering conditions needed to be combined with corresponding cursor predicates and list DTO reads.

| Alternative | Suitable use | Consideration for this project |
| --- | --- | --- |
| Spring Data JPA derived methods | Simple conditions and fixed query shapes | Optional combinations can increase the number and complexity of method names |
| `@Query` / JPQL | Explicit, stable joins and query structures | Optional predicates, ordering, and cursors must be maintained in string queries |
| Specification | Compose and reuse JPA Criteria predicates | Compare readability when combining conditions with DTO reads and ordering |
| QueryDSL — implemented | Express predicates, ordering, joins, and projections in Java | Requires Q-type generation and another dependency |

### Reason for selection

QueryDSL keeps JPA while allowing small filter/order/cursor expressions to be reused and DTO reads to be written in the same query flow.<br>
The comparison is between ways of writing JPA queries, not QueryDSL versus JPA itself.

### Trade-offs

Q-types must be generated and maintained, and QueryDSL does not guarantee efficient SQL.<br>
Data volume, execution plans, and indexes were examined separately.<br>
Execution speed was not compared with other query-authoring approaches.

[List DTOs](improvements/list-thumbnail-dto.md) · [Filter indexes](improvements/filter-indexes.md) · [Spring Data JPA query methods](https://docs.spring.io/spring-data/jpa/reference/jpa/query-methods.html) · [Specification](https://docs.spring.io/spring-data/jpa/reference/jpa/specifications.html)

---

## MySQL — relational data and team experience

### Requirements

Product, option, inventory, and brand relationships needed to be managed, with SQL and indexes supporting filtering, ordering, and pagination.

| Criterion | MySQL — implemented | PostgreSQL — alternative |
| --- | --- | --- |
| Relational reads | Current queries use joins, ordering, and composite indexes | An alternative for the same relational-read requirements |
| Actual query costs | Plans, indexes, and connection waiting observed on the dataset | Requires a separate comparison with equivalent data, queries, and environment |
| Development and operations | Use team experience and the existing JPA, Docker, and observability setup | Consider team familiarity and the setup, deployment, and operations work to learn |

### Reason for selection

MySQL supported the required relational reads, while the team's experience reduced the work needed to learn setup and operations.<br>
Read structures and indexes were improved within the existing JPA/Docker setup, with costs examined through execution plans and connection waiting.

### Trade-offs

PostgreSQL also supports the required relational reads and [multiple index types](https://www.postgresql.org/docs/current/indexes.html), so this choice does not establish a performance advantage over it.<br>
Execution plans and tuning results observed in MySQL cannot be applied directly to another database; changing databases would require validating the actual queries again.<br>
Current measurements concern changes to read structures and settings within MySQL; representative-field updates and write consistency remain follow-up validation.

[Representative price](improvements/list-price.md) · [Filter plans](improvements/filter-indexes.md) · [Connection pools](troubleshooting/hikari.md)

---

## Redis — compared with local caching and direct DB reads

### Requirements

Detail responses needed repeated color/size dictionary values for multiple options.<br>
The dictionary contains 60 entries; prices, inventory, and complete product responses are not cached.

| Alternative | Benefit | Cost |
| --- | --- | --- |
| Local cache | Remove network round trips for dictionary reads | Per-instance loading, refresh, invalidation, and value differences |
| DB joins / bulk reads | Read with source data and reduce separate cache dependencies | DB round trips, joins, and repeated string transfer remain considerations even with a warm buffer pool |
| Redis — implemented | A shared external dictionary and `multiGet` bulk reads | Network/serialization, Redis availability, initialization, and refresh policies |

### Reason for selection

Redis separates repeated dictionary reads from DB relationship traversal and retrieves the required values together with `multiGet`.<br>
A common dictionary can be managed in one place instead of maintaining a local copy in each instance.<br>
Sharing across instances is a design benefit; its operational effect was not validated by the current measurements.

### Trade-offs

Redis adds network round trips and serialization, along with cache refresh and failure handling.<br>
The buffer pool keeps DB pages in memory, so it is considered within the direct-DB-read alternative.<br>
A dictionary this small may be simpler to keep in local memory or read from the DB in bulk.

Relationship fetching and Redis use changed together, so the entire detail improvement is not attributed to Redis.<br>
Relative performance and dictionary refresh/miss/failure policies remain follow-up work.

[Detail reads and cache scope](improvements/product-detail.md) · [Alternative validation](future-work.md)

---

## Elasticsearch — compared with LIKE and MySQL FULLTEXT

### Requirements

Search needed to find intent spread across product names, colors, and types, such as “black skirt,” and control relevance with synonyms and field weights.

| Alternative | Fit to the search requirement | Implementation and operating costs |
| --- | --- | --- |
| Existing LIKE | Contiguous title matching and ID ordering | Uses the existing DB; token, multi-field, and relevance behavior need additional implementation |
| MySQL FULLTEXT | Supports CJK ngrams and relevance | Requires search indexes and query design; assess the required synonyms, option relationships, and weighting |
| Elasticsearch — implemented | A search model for morphology, synonyms, multiple fields, and weights | Separate server/index operations, DB propagation, reindexing, and recovery |

### Reason for selection

ES supports morphological analysis, synonyms, and multi-field matching, with required matches and relevance ranking controlled separately.<br>
One document per option preserves actual color/size combinations, while `productId` collapse groups duplicate product results.

### Trade-offs

A separate search server and index need to be operated, with DB change propagation, reindexing, and recovery to design.<br>
Product fields are repeated across option documents, and grouping results by product adds query costs and pagination constraints.<br>
A product-root nested model is a follow-up alternative that preserves option combinations with a different document structure; both read and update costs need comparison.

The LIKE comparison changes search behavior, so latency alone does not explain adoption.<br>
FULLTEXT is a retrospective alternative without direct quality or performance measurements.

[Search case study](improvements/product-search.md) · [Search pagination](troubleshooting/search-pagination.md) · [FULLTEXT relevance](https://dev.mysql.com/doc/refman/8.4/en/fulltext-natural-language.html) · [CJK ngrams](https://dev.mysql.com/doc/refman/8.4/en/fulltext-search-ngram.html)

---

## Load testing and observability — connect load, resources, and request paths

### Requirements

Inputs and load needed to be repeatable, with resource state and waiting inside requests observed when response times increased.

### Reason for selection

Logs help inspect individual events, while profilers help investigate method execution and CPU use.<br>
The following tools complement them by connecting repeatable load, resource changes over time, and request spans.

| Tool | Reason for selection and role in this project | Application |
| --- | --- | --- |
| k6 | Record inputs, load, and thresholds in JavaScript; check latency, HTTP failures, and drops in 1-VU comparisons and target-rate tests | [Scenarios and criteria](environment.md) |
| Prometheus | Collect time series for Hikari active/pending, Tomcat busy, and ES HTTP pools to relate changes to load stages | [Connection pools](troubleshooting/hikari.md) |
| Grafana | Compare throughput, latency, and resources on the same dashboard timeline to select intervals for investigation | [Load results](../../README.md#load-results) |
| Jaeger | Inspect request spans to locate time spent entering the service, fetching DB data, and assembling DTOs | [Detail-path isolation](troubleshooting/detail-isolation.md) |
| ES Profile | Inspect query-operation execution to investigate costly search operations | [Search query costs](troubleshooting/search-capacity.md) |
| ES hot threads | Inspect thread stacks under load to identify work running when CPU use and search queues rise | [Search CPU and queues](troubleshooting/search-capacity.md) |

### Trade-offs

Collection, storage, and tracing consume resources, and observability tools on the same host can affect load-test results.<br>
DEBUG logs and unnecessary tracing were reduced, and ES Profile diagnostics were kept separate from ordinary load measurements.

Metrics changing together identify a possible cause that needs to be checked through experiments.<br>
Individual traces and hot-thread samples do not represent all requests; overall latency and hold statistics follow the load-test measurements and aggregation criteria.

[Observability overhead](troubleshooting/observability-overhead.md) · [Prometheus collection model](https://prometheus.io/docs/introduction/overview/) · [Jaeger architecture](https://www.jaegertracing.io/docs/1.76/architecture/) · [ES Profile scope and overhead](https://www.elastic.co/docs/reference/elasticsearch/rest-apis/search-profile)
