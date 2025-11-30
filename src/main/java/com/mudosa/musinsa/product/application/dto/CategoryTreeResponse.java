package com.mudosa.musinsa.product.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryTreeResponse {
	List<CategoryNode> categories;

	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	@Builder
	public static class CategoryNode {
		Long categoryId;
		String categoryName;
		String categoryPath;
		String imageUrl;
        List<CategoryNode> children;
    }
}
