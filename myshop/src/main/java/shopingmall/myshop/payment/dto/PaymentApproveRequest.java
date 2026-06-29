package shopingmall.myshop.payment.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record PaymentApproveRequest(
        @NotNull Long orderId,
        @NotBlank String paymentKey,
        @Min(0) long amount
) {
}
