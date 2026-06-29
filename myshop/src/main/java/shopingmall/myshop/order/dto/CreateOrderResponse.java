package shopingmall.myshop.order.dto;

import shopingmall.myshop.order.domain.Order;
import shopingmall.myshop.order.domain.OrderStatus;
import shopingmall.myshop.payment.domain.Payment;

public record CreateOrderResponse(
        Long orderId,
        String orderNo,
        OrderStatus orderStatus,
        long totalPrice,
        long discountAmount,
        long finalPaymentAmount,
        PaymentReadyResponse paymentReady
) {
    public static CreateOrderResponse from(Order order, Payment payment) {
        return new CreateOrderResponse(
                order.getId(),
                order.getOrderNo(),
                order.getOrderStatus(),
                order.getTotalPrice(),
                order.getTotalDiscountAmount(),
                order.getFinalPaymentAmount(),
                new PaymentReadyResponse(payment.getPaymentKey(), payment.getIdempotencyKey())
        );
    }

    public record PaymentReadyResponse(
            String paymentKey,
            String idempotencyKey
    ) {
    }
}
