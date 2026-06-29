package shopingmall.myshop.common.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 실패 응답에 들어갈 최소 에러 정보입니다.
 * 이번 상품 목록 조회는 정상 응답 중심이지만, ApiResponse 구조상 error 필드 타입이 필요합니다.
 */
@Getter
@AllArgsConstructor
public class ErrorResponse {

    private String code;
    private String message;
}
