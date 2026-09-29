# Observability overhead: scope logging and tracing for load

[한국어](../../ko/troubleshooting/observability-overhead.md) · [README](../../../README.md) · [Measurement setup](../environment.md)

---

## Problem: development settings carried into load tests

DEBUG logs and broad OpenTelemetry instrumentation remained enabled under high load.<br>
More requests produced more SQL/bind logs, spans, serialization, and export traffic.<br>
The application and monitoring tools such as Jaeger and Grafana shared the same machine, competing for CPU, memory, and I/O.

---

## Changes

| Area | Change | Purpose |
| --- | --- | --- |
| SQL/bind logging | INFO instead of DEBUG/TRACE | Reduce detailed per-request logging. |
| Instrumentation scope | Remove unnecessary observation points | Reduce spans that did not contribute to diagnosis. |
| Trace collection | Default sampling of 1% | Reduce the cost of tracing every request. |
| Diagnostic signals | Retain read/ES spans and connection-pool metrics | Preserve visibility into processing and waiting. |

Responses improved after observation was reduced, and subsequent load tests used this configuration.<br>
[Applied configuration](../../../src/main/resources/application-dev.yml)

---

## Decision and trade-off

Monitoring consumes resources alongside the service.<br>
I therefore used different collection scopes for feature development and high-load diagnosis: lower routine logging and sampling, with targeted spans and metrics for the area under investigation.

Lower sampling reduces the opportunity to inspect individual requests.<br>
Metrics provide throughput, p95, failures, and pool waiting; Jaeger provides execution order and delay locations within sampled requests.
