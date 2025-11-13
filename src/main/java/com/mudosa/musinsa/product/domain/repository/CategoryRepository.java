package com.mudosa.musinsa.product.domain.repository;

import com.mudosa.musinsa.product.domain.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    
    default Category findByPath(String path) {
        // 입력된 경로를 '>' 구분자로 분리
        String[] pathParts = path.split(">");
        
        // 모든 카테고리를 스트림으로 변환하여 검색
        return findAll().stream()
            .filter(category -> {
                String categoryPath = category.buildPath();
                
                // 정확한 일치 확인
                if (categoryPath.equals(path)) {
                    return true;
                }
                
                // 분리된 각 부분이 카테고리 경로에 포함되는지 확인
                for (String pathPart : pathParts) {
                    if (categoryPath.equals(pathPart.trim())) {
                        return true;
                    }
                }
                
                return false;
            })
            .findFirst()
            .orElse(null);
    }
}