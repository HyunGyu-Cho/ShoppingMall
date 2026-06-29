package shopingmall.myshop.payment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record PaymentFailRequest(
        @NotNull Long orderId,
        @NotBlank String failureCode,
        @NotBlank String failureMessage
) {
}
