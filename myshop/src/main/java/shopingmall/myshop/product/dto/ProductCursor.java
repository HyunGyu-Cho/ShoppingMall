package shopingmall.myshop.product.dto;

import java.time.LocalDateTime;

/**
 * 요청 커서 문자열을 Java 객체로 바꾼 값입니다.
 *
 * API 스펙의 커서 형식:
 * 2026-06-28T12:00:00_100
 *
 * 의미:
 * - 2026-06-28T12:00:00 : 마지막으로 조회한 상품의 createdAt
 * - 100                 : 마지막으로 조회한 상품의 productId
 *
 * createdAt만 커서로 쓰지 않는 이유:
 * 같은 시각에 생성된 상품이 여러 개일 수 있으므로 productId를 보조 정렬값으로 함께 사용합니다.
 */
public record ProductCursor(
        LocalDateTime createdAt,
        Long productId
) {

    public static ProductCursor empty() {
        return new ProductCursor(null, null);
    }

    public static ProductCursor from(String cursor) {
        if (cursor == null || cursor.isBlank()) {
            return empty();
        }

        String[] parts = cursor.split("_");
        if (parts.length != 2) {
            throw new IllegalArgumentException("cursor format must be yyyy-MM-ddTHH:mm:ss_productId");
        }

        return new ProductCursor(
                LocalDateTime.parse(parts[0]),
                Long.parseLong(parts[1])
        );
    }
}
