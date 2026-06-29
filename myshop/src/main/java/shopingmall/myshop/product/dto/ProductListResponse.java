package shopingmall.myshop.product.dto;

import java.util.List;

/**
 * 상품 목록 조회 API의 data 영역입니다.
 *
 * items      : 현재 페이지 상품 목록
 * nextCursor : 다음 페이지 요청에 사용할 커서. 다음 페이지가 없으면 null
 * hasNext    : 다음 페이지 존재 여부
 */
public record ProductListResponse(
        List<ProductItemResponse> items,
        String nextCursor,
        boolean hasNext
) {
}
