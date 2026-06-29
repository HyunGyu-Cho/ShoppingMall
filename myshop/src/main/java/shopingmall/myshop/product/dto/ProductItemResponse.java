package shopingmall.myshop.product.dto;

import shopingmall.myshop.product.domain.Product;
import shopingmall.myshop.product.domain.enums.ProductStatus;

/**
 * 상품 목록의 개별 상품 응답 DTO입니다.
 *
 * Entity(Product)를 그대로 응답하지 않고 DTO로 바꾸는 이유:
 * 1. API 스펙에 필요한 필드만 노출할 수 있습니다.
 * 2. Lazy Loading 연관관계(Category 등)가 JSON 변환 중 노출되는 문제를 막을 수 있습니다.
 * 3. reviewCount, averageRating처럼 Entity 자체 필드가 아닌 계산값도 함께 담을 수 있습니다.
 */
public record ProductItemResponse(
        Long productId,
        String name,
        int price,
        String thumbnailUrl,
        long reviewCount,
        double averageRating,
        ProductStatus status
) {

    /**
     * 현재 프로젝트에는 리뷰 집계 조회가 아직 연결되어 있지 않습니다.
     * 그래서 우선 reviewCount=0, averageRating=0.0으로 응답합니다.
     *
     * 이후 ReviewRepository에서 productId별 count/avg를 조회하도록 보강하면
     * 이 메서드의 파라미터로 리뷰 집계값을 받아 넣으면 됩니다.
     */
    public static ProductItemResponse from(Product product) {
        return new ProductItemResponse(
                product.getId(),
                product.getName(),
                product.getPrice(),
                product.getThumbnailUrl(),
                0L,
                0.0,
                product.getStatus()
        );
    }
}
