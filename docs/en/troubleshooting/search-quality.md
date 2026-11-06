# Search quality: confusing the best field score with a required condition per token

[한국어](../../ko/troubleshooting/search-quality.md) · [README](../../../README.md)

---

## Problem

`best_fields` in `multi_match`, which searches several fields at once, scores each field separately and uses the score of the best-matching field.<br>
So for a query like "블랙 스커트" (black skirt), where the color is in the color field and the item type is in the category field, it was hard to express that both words must match.<br>
[multi_match documentation](https://www.elastic.co/docs/reference/query-languages/query-dsl/query-dsl-multi-match-query)

---

## Fix

For each word in the query, I built a condition that it only has to match at least one of several fields (`should` + `minimum_should_match=1`), and wrapped those conditions in `must` so that every one of them has to be met.<br>
The per-field weights that decide the ranking were moved into a separate `should`.<br>
Now 블랙 (black) can match in the color field and 스커트 (skirt) in the category field.

---

## Example result: 나이키 후드 (Nike hoodie)

| Product | Category | Name score | Category score | Total |
| --- | --- | ---: | ---: | ---: |
| A, 나이키 클럽 후드 (Nike Club hoodie) | Tops > Hoodies | 27.48 | 18.44 | 45.93 |
| B, 나이키 repeated in the name | Tops > Shirts | 29.39 | 0 | 29.39 |

In explain, B had the higher name score, but A came first because the hoodie category score was added.<br>
For a user searching for "나이키 후드", showing A, an actual hoodie, first is what they meant.

---

## Choices and trade-offs

- **Every word required**: Making every word required makes results more precise, but a single typo or an expression not in the synonym list can drop a product from the results.<br>
