package shopingmall.myshop.product.dto;

import shopingmall.myshop.product.domain.Product;
import shopingmall.myshop.product.domain.enums.ProductStatus;

import java.time.LocalDateTime;

public record ProductResponse(
        Long productId,
        Long categoryId,
        String name,
        int price,
        String thumbnailUrl,
        String description,
        ProductStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static ProductResponse from(Product product) {
        Long categoryId = product.getCategory() == null ? null : product.getCategory().getId();
        return new ProductResponse(
                product.getId(),
                categoryId,
                product.getName(),
                product.getPrice(),
                product.getThumbnailUrl(),
                product.getDescription(),
                product.getStatus(),
                product.getCreatedAt(),
                product.getUpdatedAt()
        );
    }
}
