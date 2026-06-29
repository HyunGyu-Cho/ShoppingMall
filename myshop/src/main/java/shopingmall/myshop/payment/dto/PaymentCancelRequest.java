package shopingmall.myshop.payment.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record PaymentCancelRequest(
        @Min(1) long cancelAmount,
        @NotBlank String reason
) {
}
