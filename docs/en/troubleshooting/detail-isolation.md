# Product detail: narrowing down waits and DTO assembly with mocks

[한국어](../../ko/troubleshooting/detail-isolation.md) · [README](../../../README.md)

---

## Problem

Detail throughput stopped at about 2,000 RPS, and in Jaeger the Security span looked the longest.<br>
To separate whether authentication itself was slow or something running inside it was, I replaced the request path with mocks one layer at a time, starting from the outside, and measured each step.

---

## Measurements by layer

| Step | Path tested | Measured RPS |
| --- | --- | ---: |
| 1 | Mock controller, without Security | 41,000 |
| 2 | Mock controller, with Security | 42,000 |
| 3 | Mock service, dummy DB | 66,000 |
| 4 | Real detail service | 2,000 |

Throughput didn't change with Security included, so authentication wasn't the cause.<br>
As soon as the path switched to the real service and DB, throughput fell to 2,000 RPS, which narrowed the cause down to the detail service itself.

---

## What I found

The first cause was waiting before the service was entered.<br>
The Security span was long, but judging from when its child spans started and from the connection pool metrics, most of that time wasn't authentication work; it was waiting for a DB connection before the service method ran.

The second was response assembly.<br>
The response was built by lazily loading the product, option, image, and option-value relationships one at a time, which repeated queries and conversions, and joining several relationships at once would have multiplied the rows.

---

## Fix

I split the relationships into separate queries by the unit they were needed in, and changed the color and size dictionary to be read in one call.<br>
This brought the detail mean from 69.47ms to 9.11ms. → [Product detail improvement](../improvements/product-detail.md)<br>
DB connection waits were covered next in the [Hikari experiment](hikari.md) and the [Tomcat experiment](tomcat.md).

---

## Assessment

Rather than tuning the Security config because it had the longest span, I swapped the path out one layer at a time to find the cause.<br>
That let me focus on the real causes: connection waits, how relationships were loaded, and how the response was assembled.
