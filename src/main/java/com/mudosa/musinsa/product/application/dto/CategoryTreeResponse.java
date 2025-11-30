package com.mudosa.musinsa.product.application.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.Builder;
import lombok.Value;

import java.util.List;

@Value
@Builder(toBuilder = true)
@JsonDeserialize(builder = CategoryTreeResponse.CategoryTreeResponseBuilder.class)
public class CategoryTreeResponse {
	List<CategoryNode> categories;

	@Value
	@Builder(toBuilder = true)
	@JsonDeserialize(builder = CategoryNode.CategoryNodeBuilder.class)
	public static class CategoryNode {
		Long categoryId;
		String categoryName;
		String categoryPath;
		String imageUrl;
		List<CategoryNode> children;

		@JsonPOJOBuilder(withPrefix = "")
		public static class CategoryNodeBuilder {
		}
	}

	@JsonPOJOBuilder(withPrefix = "")
	public static class CategoryTreeResponseBuilder {
	}
}
