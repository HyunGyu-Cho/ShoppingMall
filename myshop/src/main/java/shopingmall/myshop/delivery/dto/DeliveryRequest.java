package shopingmall.myshop.delivery.dto;

import jakarta.validation.constraints.NotBlank;

public record DeliveryRequest(
        @NotBlank String receiverName,
        @NotBlank String receiverPhone,
        @NotBlank String zipcode,
        @NotBlank String address1,
        String address2
) {
}
