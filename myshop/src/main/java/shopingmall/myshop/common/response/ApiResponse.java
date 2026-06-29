package shopingmall.myshop.common.response;


/**
 * 모든 API가 동일한 응답 포맷을 사용하도록 감싸는 공통 응답 객체입니다.
 *
 * 상품 목록 API 스펙:
 * {
 *   "success": true,
 *   "data": { ... },
 *   "error": null
 * }
 *
 * Controller에서는 실제 데이터만 Service에서 받아온 뒤 ApiResponse.success(data)로 감싸면 됩니다.
 */
public class ApiResponse<T> {

    private final boolean success;
    private final T data;
    private final ErrorResponse error;

    private ApiResponse(boolean success, T data, ErrorResponse error) {
        this.success = success;
        this.data = data;
        this.error = error;
    }

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, data, null);
    }

    public static ApiResponse<Void> success() {
        return new ApiResponse<>(true, null, null);
    }

    public static ApiResponse<Void> fail(ErrorResponse error) {
        return new ApiResponse<>(false, null, error);
    }

    public boolean isSuccess() {
        return success;
    }

    public T getData() {
        return data;
    }

    public ErrorResponse getError() {
        return error;
    }
}
