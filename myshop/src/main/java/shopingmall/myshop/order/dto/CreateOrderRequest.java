package shopingmall.myshop.order.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import shopingmall.myshop.delivery.dto.DeliveryRequest;

import java.util.List;

public record CreateOrderRequest(
        @NotEmpty List<@Valid Item> items,
        @Valid @NotNull DeliveryRequest delivery
) {
    public record Item(
            @NotNull Long productId,
            @Min(1) int quantity
    ) {
    }
}
