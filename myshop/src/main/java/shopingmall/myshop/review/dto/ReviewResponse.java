package shopingmall.myshop.review.dto;

import shopingmall.myshop.review.domain.Review;

import java.time.LocalDateTime;

public record ReviewResponse(
        Long reviewId,
        Long productId,
        Long orderItemId,
        int rating,
        String content,
        LocalDateTime createdAt
) {
    public static ReviewResponse from(Review review) {
        return new ReviewResponse(
                review.getId(),
                review.getProduct().getId(),
                review.getOrderItem().getId(),
                review.getRating(),
                review.getContent(),
                review.getCreatedAt()
        );
    }
}
