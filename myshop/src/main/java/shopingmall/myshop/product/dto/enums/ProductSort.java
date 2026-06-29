package shopingmall.myshop.product.dto.enums;

/**
 * 상품 목록 조회 정렬 옵션입니다.
 *
 * API 요청 문자열:
 * - latest
 * - priceAsc
 * - priceDesc
 */
public enum ProductSort {
    LATEST,
    PRICE_ASC,
    PRICE_DESC;

    public static ProductSort from(String sort) {
        if (sort == null || sort.isBlank() || sort.equals("latest")) {
            return LATEST;
        }

        if (sort.equals("priceAsc")) {
            return PRICE_ASC;
        }

        if (sort.equals("priceDesc")) {
            return PRICE_DESC;
        }

        throw new IllegalArgumentException("sort must be latest, priceAsc, or priceDesc");
    }
}
