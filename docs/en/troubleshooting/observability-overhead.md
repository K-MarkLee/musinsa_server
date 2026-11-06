# When observability backfires: sizing logs and tracing for load tests

[한국어](../../ko/troubleshooting/observability-overhead.md) · [README](../../../README.md)

---

## Problem

I ran load tests with the DEBUG logs and broad tracing I had used during development still turned on.<br>
As requests increased, so did SQL and bind-parameter log output and span creation and export, and the application and observability tools such as Jaeger and Grafana were all sharing the same machine's CPU, memory, and disk.

---

## Fix

| Target | Change | Effect |
| --- | --- | --- |
| SQL and bind-parameter logs | DEBUG, TRACE → INFO | Less detailed logging on every request |
| Tracing scope | Removed instrumentation points not used for root-cause analysis | Fewer unnecessary spans created and exported |
| Trace collection | 1% sampling | Lower cost than tracing every request |
| Diagnostic metrics | Kept query and ES call spans and connection pool metrics | Processing time and where requests wait are still visible |

Responses improved after narrowing the observability scope, and every later load test used these settings. → [Applied settings](../../../src/main/resources/application-dev.yml)

---

## Choices and trade-offs

- **Fewer requests to trace**: Lower sampling means fewer chances to trace an individual request.
  So I used metrics for throughput, p95, errors, and connection waits, and used Jaeger only to see where time went inside a request.<br>
