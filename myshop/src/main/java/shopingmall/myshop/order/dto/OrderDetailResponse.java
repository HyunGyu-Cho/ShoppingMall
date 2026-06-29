package shopingmall.myshop.order.dto;

import shopingmall.myshop.delivery.dto.DeliveryResponse;
import shopingmall.myshop.order.domain.OrderStatus;
import shopingmall.myshop.payment.dto.PaymentResponse;

import java.time.LocalDateTime;
import java.util.List;

public record OrderDetailResponse(
        Long orderId,
        String orderNo,
        OrderStatus orderStatus,
        long totalPrice,
        long discountAmount,
        long finalPaymentAmount,
        LocalDateTime orderedAt,
        List<Item> items,
        PaymentResponse payment,
        DeliveryResponse delivery
) {
    public record Item(
            Long orderItemId,
            Long productId,
            String productName,
            long orderPrice,
            int quantity,
            long discountAmount
    ) {
    }
}
