package shopingmall.myshop.review.dto;

import java.time.LocalDateTime;
import java.util.List;

public record ReviewListResponse(
        List<Item> items,
        String nextCursor,
        boolean hasNext
) {
    public record Item(
            Long reviewId,
            String memberName,
            int rating,
            String content,
            LocalDateTime createdAt
    ) {
    }
}
