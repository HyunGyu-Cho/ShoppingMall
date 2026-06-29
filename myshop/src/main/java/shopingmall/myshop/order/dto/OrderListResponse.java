package shopingmall.myshop.order.dto;

import shopingmall.myshop.order.domain.OrderStatus;

import java.time.LocalDateTime;
import java.util.List;

public record OrderListResponse(
        List<Item> items,
        String nextCursor,
        boolean hasNext
) {
    public record Item(
            Long orderId,
            String orderNo,
            OrderStatus orderStatus,
            long totalPrice,
            LocalDateTime orderedAt,
            String mainProductName,
            int itemCount
    ) {
    }
}
