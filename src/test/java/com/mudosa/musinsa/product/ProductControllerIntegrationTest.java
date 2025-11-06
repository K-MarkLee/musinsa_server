package com.mudosa.musinsa.product;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mudosa.musinsa.brand.domain.model.Brand;
import com.mudosa.musinsa.brand.domain.repository.BrandRepository;
import com.mudosa.musinsa.notification.domain.service.FcmService;
import com.mudosa.musinsa.product.application.dto.ProductCreateRequest;
import com.mudosa.musinsa.product.domain.model.Category;
import com.mudosa.musinsa.product.domain.model.OptionValue;
import com.mudosa.musinsa.product.domain.repository.CategoryRepository;
import com.mudosa.musinsa.product.domain.repository.OptionValueRepository;
import com.mudosa.musinsa.product.domain.repository.ProductLikeRepository;
import com.mudosa.musinsa.product.domain.repository.ProductRepository;
import com.mudosa.musinsa.product.domain.model.ProductGenderType;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@SuppressWarnings("removal")
class ProductControllerIntegrationTest {

    @MockBean
    FcmService fcmService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private BrandRepository brandRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private OptionValueRepository optionValueRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductLikeRepository productLikeRepository;

    @AfterEach
    void tearDown() {
        productLikeRepository.deleteAll();
        productRepository.deleteAll();
        optionValueRepository.deleteAll();
        categoryRepository.deleteAll();
        brandRepository.deleteAll();
    }

    @Test
    @DisplayName("상품 검색 API가 페이징 정보를 포함해 반환된다")
    void searchProducts_returnsPagedResponse() throws Exception {
        Brand brand = brandRepository.save(Brand.create("무도사", "MUDOSA", new BigDecimal("10.00")));
        Category category = categoryRepository.save(Category.builder()
            .categoryName("셔츠")
            .parent(null)
            .imageUrl(null)
            .build());
        OptionValue optionValue = createOptionValue("사이즈", "FREE");

        ProductCreateRequest requestA = ProductCreateRequest.builder()
            .brandId(brand.getBrandId())
            .productName("검색 상품 A")
            .productInfo("검색용 상품 A")
            .productGenderType(ProductGenderType.ALL.name())
            .brandName(brand.getNameKo())
            .categoryPath(category.buildPath())
            .isAvailable(true)
            .categoryId(category.getCategoryId())
            .images(List.of(ProductCreateRequest.ImageCreateRequest.builder()
                .imageUrl("https://cdn.musinsa.com/product/search-a.jpg")
                .isThumbnail(true)
                .build()))
            .options(List.of(ProductCreateRequest.OptionCreateRequest.builder()
                .productPrice(new BigDecimal("9900"))
                .stockQuantity(5)
                .optionValueIds(List.of(optionValue.getOptionValueId()))
                .build()))
            .build();

        ProductCreateRequest requestB = ProductCreateRequest.builder()
            .brandId(brand.getBrandId())
            .productName("검색 상품 B")
            .productInfo("검색용 상품 B")
            .productGenderType(ProductGenderType.ALL.name())
            .brandName(brand.getNameKo())
            .categoryPath(category.buildPath())
            .isAvailable(true)
            .categoryId(category.getCategoryId())
            .images(List.of(ProductCreateRequest.ImageCreateRequest.builder()
                .imageUrl("https://cdn.musinsa.com/product/search-b.jpg")
                .isThumbnail(true)
                .build()))
            .options(List.of(ProductCreateRequest.OptionCreateRequest.builder()
                .productPrice(new BigDecimal("19900"))
                .stockQuantity(8)
                .optionValueIds(List.of(optionValue.getOptionValueId()))
                .build()))
            .build();

        mockMvc.perform(post("/api/brands/" + brand.getBrandId() + "/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestA)))
            .andExpect(status().isCreated());

        mockMvc.perform(post("/api/brands/" + brand.getBrandId() + "/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestB)))
            .andExpect(status().isCreated());

        mockMvc.perform(get("/api/products")
                .param("keyword", "검색 상품")
                .param("priceSort", "HIGHEST")
                .param("page", "0")
                .param("size", "10"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalElements").value(2))
            .andExpect(jsonPath("$.totalPages").value(1))
            .andExpect(jsonPath("$.page").value(0))
            .andExpect(jsonPath("$.size").value(10))
            .andExpect(jsonPath("$.products", Matchers.hasSize(2)))
            .andExpect(jsonPath("$.products[0].productName").value("검색 상품 B"))
            .andExpect(jsonPath("$.products[0].lowestPrice").value(19900))
            .andExpect(jsonPath("$.products[0].hasStock").value(true))
            .andExpect(jsonPath("$.products[0].thumbnailUrl").value("https://cdn.musinsa.com/product/search-b.jpg"));
    }

    @Test
    @DisplayName("상품 생성과 상세 조회가 성공한다")
    void createProduct_thenGetDetail_success() throws Exception {
        Brand brand = brandRepository.save(Brand.create("무도사", "MUDOSA", new BigDecimal("10.00")));
        Category category = categoryRepository.save(Category.builder()
            .categoryName("상의")
            .parent(null)
            .imageUrl(null)
            .build());
        OptionValue optionValue = createOptionValue("사이즈", "M");

        ProductCreateRequest request = buildCreateRequest(brand, category, optionValue, Collections.singletonList(optionValue.getOptionValueId()));

        MvcResult createResult = mockMvc.perform(post("/api/brands/" + brand.getBrandId() + "/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(header().string("Location", Matchers.matchesPattern("/api/brands/" + brand.getBrandId() + "/products/\\d+")))
            .andReturn();

        JsonNode createNode = objectMapper.readTree(createResult.getResponse().getContentAsString());
        long productId = createNode.get("productId").asLong();
        assertThat(productId).isPositive();

        mockMvc.perform(get("/api/products/{productId}", productId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.productId").value(productId))
            .andExpect(jsonPath("$.brandId").value(brand.getBrandId()))
            .andExpect(jsonPath("$.productName").value("테스트 상품"))
            .andExpect(jsonPath("$.images", Matchers.hasSize(1)))
            .andExpect(jsonPath("$.options", Matchers.hasSize(1)))
            .andExpect(jsonPath("$.options[0].stockQuantity").value(10))
            .andExpect(jsonPath("$.options[0].optionValues", Matchers.hasSize(1)))
            .andExpect(jsonPath("$.options[0].optionValues[0].optionName").value("사이즈"))
            .andExpect(jsonPath("$.likeCount").value(0));
    }

    @Test
    @DisplayName("존재하지 않는 브랜드로 상품 생성 시 404를 반환한다")
    void createProduct_brandNotFound_returnsNotFound() throws Exception {
        Category category = categoryRepository.save(Category.builder()
            .categoryName("아우터")
            .parent(null)
            .imageUrl(null)
            .build());
        OptionValue optionValue = createOptionValue("사이즈", "L");

        ProductCreateRequest request = ProductCreateRequest.builder()
            .brandId(999L)
            .productName("브랜드 없음")
            .productInfo("브랜드가 없는 경우")
            .productGenderType(ProductGenderType.MEN.name())
            .brandName("없는 브랜드")
            .categoryPath(category.buildPath())
            .isAvailable(true)
            .categoryId(category.getCategoryId())
            .images(List.of(ProductCreateRequest.ImageCreateRequest.builder()
                .imageUrl("https://cdn.musinsa.com/product/no-brand.jpg")
                .isThumbnail(true)
                .build()))
            .options(List.of(ProductCreateRequest.OptionCreateRequest.builder()
                .productPrice(new BigDecimal("19900"))
                .stockQuantity(5)
                .optionValueIds(List.of(optionValue.getOptionValueId()))
                .build()))
            .build();

    mockMvc.perform(post("/api/brands/999/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.errorCode").value("10009"))
            .andExpect(jsonPath("$.message").value(Matchers.containsString("브랜드를 찾을 수 없습니다")));
    }

    @Test
    @DisplayName("유효하지 않은 요청 본문은 400을 반환한다")
    void createProduct_validationFailure_returnsBadRequest() throws Exception {
        Brand brand = brandRepository.save(Brand.create("무도사", "MUDOSA", new BigDecimal("10.00")));
        Category category = categoryRepository.save(Category.builder()
            .categoryName("팬츠")
            .parent(null)
            .imageUrl(null)
            .build());
        OptionValue optionValue = createOptionValue("사이즈", "S");

        ProductCreateRequest request = ProductCreateRequest.builder()
            .brandId(brand.getBrandId())
            .productName("")
            .productInfo("상품명 누락")
            .productGenderType(ProductGenderType.WOMEN.name())
            .brandName(brand.getNameKo())
            .categoryPath(category.buildPath())
            .isAvailable(true)
            .categoryId(category.getCategoryId())
            .images(List.of(ProductCreateRequest.ImageCreateRequest.builder()
                .imageUrl("https://cdn.musinsa.com/product/invalid.jpg")
                .isThumbnail(true)
                .build()))
            .options(List.of(ProductCreateRequest.OptionCreateRequest.builder()
                .productPrice(new BigDecimal("15900"))
                .stockQuantity(3)
                .optionValueIds(List.of(optionValue.getOptionValueId()))
                .build()))
            .build();

    mockMvc.perform(post("/api/brands/" + brand.getBrandId() + "/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.errorCode").value("10001"))
            .andExpect(jsonPath("$.message").value(Matchers.containsString("상품명은 필수입니다")));
    }

    @Test
    @DisplayName("존재하지 않는 옵션 값으로 상품 생성 시 400을 반환한다")
    void createProduct_optionValueNotFound_returnsBadRequest() throws Exception {
        Brand brand = brandRepository.save(Brand.create("무도사", "MUDOSA", new BigDecimal("10.00")));
        Category category = categoryRepository.save(Category.builder()
            .categoryName("니트")
            .parent(null)
            .imageUrl(null)
            .build());

        ProductCreateRequest request = ProductCreateRequest.builder()
            .brandId(brand.getBrandId())
            .productName("옵션 없음")
            .productInfo("존재하지 않는 옵션")
            .productGenderType(ProductGenderType.ALL.name())
            .brandName(brand.getNameKo())
            .categoryPath(category.buildPath())
            .isAvailable(true)
            .categoryId(category.getCategoryId())
            .images(List.of(ProductCreateRequest.ImageCreateRequest.builder()
                .imageUrl("https://cdn.musinsa.com/product/no-option.jpg")
                .isThumbnail(true)
                .build()))
            .options(List.of(ProductCreateRequest.OptionCreateRequest.builder()
                .productPrice(new BigDecimal("18900"))
                .stockQuantity(7)
                .optionValueIds(List.of(999L))
                .build()))
            .build();

    mockMvc.perform(post("/api/brands/" + brand.getBrandId() + "/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.errorCode").value("10001"))
            .andExpect(jsonPath("$.message").value(Matchers.containsString("존재하지 않는 옵션 값 ID")));
    }

    @Test
    @DisplayName("상품 상세 조회에서 존재하지 않는 ID는 404를 반환한다")
    void getProductDetail_notFound_returnsNotFound() throws Exception {
        mockMvc.perform(get("/api/products/{productId}", 9999))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.errorCode").value("10009"))
            .andExpect(jsonPath("$.message").value(Matchers.containsString("Product not found")));
    }

    @Test
    @DisplayName("썸네일이 중복되면 상품 생성이 실패한다")
    void createProduct_duplicateThumbnail_returnsBadRequest() throws Exception {
        Brand brand = brandRepository.save(Brand.create("무도사", "MUDOSA", new BigDecimal("10.00")));
        Category category = categoryRepository.save(Category.builder()
            .categoryName("코트")
            .parent(null)
            .imageUrl(null)
            .build());
        OptionValue optionValue = createOptionValue("사이즈", "L");

        ProductCreateRequest request = ProductCreateRequest.builder()
            .brandId(brand.getBrandId())
            .productName("썸네일 중복")
            .productInfo("썸네일이 두 개")
            .productGenderType(ProductGenderType.MEN.name())
            .brandName(brand.getNameKo())
            .categoryPath(category.buildPath())
            .isAvailable(true)
            .categoryId(category.getCategoryId())
            .images(List.of(
                ProductCreateRequest.ImageCreateRequest.builder()
                    .imageUrl("https://cdn.musinsa.com/product/thumb1.jpg")
                    .isThumbnail(true)
                    .build(),
                ProductCreateRequest.ImageCreateRequest.builder()
                    .imageUrl("https://cdn.musinsa.com/product/thumb2.jpg")
                    .isThumbnail(true)
                    .build()))
            .options(List.of(ProductCreateRequest.OptionCreateRequest.builder()
                .productPrice(new BigDecimal("21900"))
                .stockQuantity(4)
                .optionValueIds(List.of(optionValue.getOptionValueId()))
                .build()))
            .build();

        mockMvc.perform(post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.errorCode").value("10001"))
            .andExpect(jsonPath("$.message").value(Matchers.containsString("썸네일은 하나만")));
    }

    private ProductCreateRequest buildCreateRequest(Brand brand,
                                                    Category category,
                                                    OptionValue optionValue,
                                                    List<Long> optionValueIds) {
        return ProductCreateRequest.builder()
            .brandId(brand.getBrandId())
            .productName("테스트 상품")
            .productInfo("테스트 상품 설명")
            .productGenderType(ProductGenderType.ALL.name())
            .brandName(brand.getNameKo())
            .categoryPath(category.buildPath())
            .isAvailable(true)
            .categoryId(category.getCategoryId())
            .images(List.of(ProductCreateRequest.ImageCreateRequest.builder()
                .imageUrl("https://cdn.musinsa.com/product/main.jpg")
                .isThumbnail(true)
                .build()))
            .options(List.of(ProductCreateRequest.OptionCreateRequest.builder()
                .productPrice(new BigDecimal("19900"))
                .stockQuantity(10)
                .optionValueIds(optionValueIds)
                .build()))
            .build();
    }

    private OptionValue createOptionValue(String optionNameLabel, String optionValueLabel) {
        return optionValueRepository.save(OptionValue.builder()
            .optionName(optionNameLabel)
            .optionValue(optionValueLabel)
            .build());
    }
}
