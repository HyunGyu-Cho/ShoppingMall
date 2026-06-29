package shopingmall.myshop.product.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import shopingmall.myshop.product.domain.enums.ProductStatus;

public record ProductCreateRequest(
        @NotNull Long categoryId,
        @NotBlank String name,
        @Min(0) int price,
        ProductStatus status,
        String thumbnailUrl,
        String description
) {
}
