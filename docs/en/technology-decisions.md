# Why these technologies

[한국어](../ko/technology-decisions.md) · [README](../../README.md)

---

## QueryDSL

| Approach | Good fit for | In this project |
| --- | --- | --- |
| Spring Data JPA derived queries | Simple, fixed conditions | One more method for every new filter combination |
| JPQL (`@Query`) | Queries with fixed joins and structure | Optional conditions, sorting, and cursors have to be concatenated as strings |
| JDBC, native SQL | Complex SQL that uses DB features directly | Everything managed by hand, without entity mapping or type checking |
| **QueryDSL (chosen)** | Queries that assemble conditions, sorting, and selected columns in code | Needs build setup to generate Q-types |

The list API combines category, brand, and gender filters, sorting, and cursor conditions differently on every request.<br>
I used QueryDSL to build these complex, dynamic queries in Java code rather than strings, and to select only the needed columns straight into DTOs.<br>
The trade-off is extra build configuration to generate the Q-types.

---

## MySQL

| Comparison | MySQL (chosen) | PostgreSQL |
| --- | --- | --- |
| Team familiarity | A DB most of the team had used | Some parts would have to be learned |
| Relational queries | Joins, sorting, and composite indexes are enough | Equally capable |
| Full-text search in the DB | FULLTEXT-level | More full-text features and extensions |

The project only needed ordinary relational queries, so instead of picking a DB for a performance or feature edge, I went with MySQL, which the team knew best.<br>
If search had to be handled inside the DB as well, I would have chosen PostgreSQL for its richer full-text search.<br>
Since search was split out into Elasticsearch, that difference didn't matter much.

---

## Redis

| Approach | Pros | Cons |
| --- | --- | --- |
| Local cache | No network round trip | Each server has to load and refresh its own copy |
| Direct DB query | No extra store needed | Puts load on the DB on every request |
| **Redis (chosen)** | Shared in one place even across servers, with no DB load | Adds a network round trip and needs refresh and failure handling |

Option values such as colors and sizes, and categories, are data that almost never changes.<br>
I used Redis to keep this data in a separate store that every server can share, and to remove the DB load of reading it entirely.<br>
At first, though, I didn't fully account for the network round trip a Redis lookup also costs, and handling for changed values or an unresponsive Redis is still needed.

---

## Elasticsearch

| Approach | How it searches | In this project |
| --- | --- | --- |
| LIKE | Finds a continuous string in the product name | Can't find queries like "블랙 스커트" (black skirt) whose words are spread across fields |
| MySQL FULLTEXT | Word-level search with relevance | Hard to control Korean morphological analysis, synonyms, and per-field weights in detail |
| **Elasticsearch (chosen)** | Morphological analysis, synonyms, multi-field search, and weighting | A separate server to operate and keep in sync with the DB |

User queries mix color, item type, and brand, and that information is spread across the product name, options, and category.<br>
I needed fine control over the condition that each word must match at least one of several fields (`minimum_should_match`), plus the Nori analyzer to split Korean product names into words and synonyms for colors and sizes, so I chose Elasticsearch over FULLTEXT.<br>
To find only the option combinations actually for sale, though, search had to run per option, which called for extra design such as collapse or nested documents to group options by product.

---

## Load testing and monitoring

| Tool | Why I used it |
| --- | --- |
| k6 | To drive load at a set number of requests per second, and keep the scenario and pass criteria in code |
| Prometheus | To collect metrics such as Hikari connections, Tomcat threads, and the ES connection pool over time |
| Grafana | To put throughput, latency, and resource metrics on one timeline and see what changed when |
| Jaeger | To see where time went inside a single request |
| ES Profile | To see which operations in a search query are expensive |
| ES hot threads | To see where ES spends CPU under load |

Only by looking at the load generator, metrics over time, and the inside of a single request together could I connect "when it got slow, what piled up at that moment, and where the request was waiting."
