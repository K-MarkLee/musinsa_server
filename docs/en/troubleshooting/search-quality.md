# Search quality: confusing best-field scores with required tokens

[한국어](../../ko/troubleshooting/search-quality.md) · [README](../../../README.md) · [Measurement setup](../environment.md)

---

## Problem

The intended requirement was to match color and product type even when they appeared in different fields.<br>
Field-score combination and requiring every query token needed to be designed separately.

`best_fields` evaluates a match per field and uses the best field for scoring.<br>
Field-centric `minimum_should_match` differs from requiring each token to match at least one field.<br>
[Official multi-match explanation](https://www.elastic.co/docs/reference/query-languages/query-dsl/query-dsl-multi-match-query)

---

## Separate required token matching from ranking

Each token receives a multi-field `should` group with `minimum_should_match=1`; the groups are combined with `must`.<br>
Whole-query weighted matches are separate `should` clauses.<br>
This explicitly allows `블랙` (black) in color and `스커트` (skirt) in category.

---

## Trade-off

Requiring every token can reduce recall for noise, typos, or missing synonyms.<br>
Matching and ranking therefore need to be tuned together.<br>
[Search design and result images](../improvements/product-search.md)

---

## Current-query relevance example: Nike hoodie

A/B are document-local labels.<br>
In the current query explain, B has the higher title contribution, but A ranks higher after its hoodie-category contribution is included.

| Product | Category | Title contribution | Category contribution | Total |
| --- | --- | --- | --- | --- |
| A · Nike Club Hoodie | Tops > Hoodies | 27.48 | 18.44 | 45.93 |
| B · Repeated Nike in title | Tops > Shirts | 29.39 | 0 | 29.39 |

Rounded contributions can differ slightly from the displayed total.<br>
LIKE does not order by relevance, so differing result order is not itself an error.<br>
See the [query result images and design](../improvements/product-search.md).
