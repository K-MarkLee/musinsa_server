# De-duplicating option documents and paginating search

[한국어](../../ko/troubleshooting/search-pagination.md) · [README](../../../README.md)

---

## Problem

Because search documents are per option, showing the results as-is let several options of the same product take up several slots in the list.<br>
Search had to run per option while the screen showed results per product, and that gap had to be closed.

---

## Fix: grouping by productId (collapse)

Option documents with the same `productId` are collapsed into one, and only one representative option per product, chosen by the sort order, is returned in the list.<br>
For relevance sorting, that's the highest-scoring option; for lowest-price sorting, it's the cheapest option that matches the conditions.<br>
[Collapse documentation](https://www.elastic.co/docs/reference/elasticsearch/rest-apis/collapse-search-results)

```mermaid
flowchart LR
    A[Search conditions] --> B[Matching option documents]
    B --> C[Collapse by productId]
    C --> D[Representative option per product]
    D --> E[Product list without duplicates]
```

---

## Why search_after couldn't be used

I wanted to use `search_after` for infinite scroll, but using it with collapse requires the collapse field and the sort field to be the same.<br>
Results are grouped by product ID but sorted by relevance or price, so that condition couldn't be met.

| Alternative | Assessment |
| --- | --- |
| Fetch plenty of options and merge them in the application | Each product has a different number of matching options, so even 2,160 options (72 on average × 30 products) can't guarantee 30 distinct products. |
| Fetch in several rounds and merge | The application would have to handle de-duplication, overall sorting, and page boundaries. |
| collapse + from/size | Chosen because ES handles de-duplication and sorting and pages by product. |

So the cursor holds a page number, and the next set of products is fetched with `from/size`.

---

## Choices and trade-offs

- **Deep pages**: As `from` grows, ES has to gather all the earlier results too, so the cost goes up.
  Search results are rarely paged through as deeply as a list, though, so this isn't a real problem for the first few pages.<br>


- **Total product count**: The total hit count in a search result is the number of option documents before collapsing, so showing a product count on screen needs a separate aggregation.<br>


- **Search cost**: Picking a representative per product adds to the work of matching and scoring option documents. This cost also showed up in the [search bottleneck investigation](search-capacity.md).<br>
