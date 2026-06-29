package shopingmall.myshop.product.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import shopingmall.myshop.common.response.ApiResponse;
import shopingmall.myshop.product.dto.ProductListResponse;
import shopingmall.myshop.product.service.ProductService;

/**
 * 상품 API Controller입니다.
 *
 * Controller의 책임:
 * - HTTP 요청을 받습니다.
 * - Query Parameter를 Java 타입으로 받습니다.
 * - 비즈니스 로직은 Service에 위임합니다.
 * - Service 결과를 공통 응답 포맷(ApiResponse)으로 감싸서 반환합니다.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    /**
     * 상품 목록 조회 API
     *
     * 요청 예:
     * GET /api/products?categoryId=1&sort=latest&cursor=2026-06-28T12:00:00_100&size=20
     *
     * Query Parameter:
     * - categoryId : 선택. 특정 카테고리 상품만 조회
     * - sort       : 선택. latest, priceAsc, priceDesc. 기본 latest
     * - cursor     : 선택. 다음 페이지 조회 기준
     * - size       : 선택. 조회 개수. 기본 20
     */
    @GetMapping
    public ApiResponse<ProductListResponse> getProducts(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false, defaultValue = "latest") String sort,
            @RequestParam(required = false) String cursor,
            @RequestParam(required = false) Integer size
    ) {
        ProductListResponse response = productService.getProducts(categoryId, sort, cursor, size);
        return ApiResponse.success(response);
    }
}
