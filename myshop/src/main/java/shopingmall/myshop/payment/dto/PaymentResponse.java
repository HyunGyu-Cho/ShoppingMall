package shopingmall.myshop.payment.dto;

import shopingmall.myshop.order.domain.OrderStatus;
import shopingmall.myshop.payment.domain.Payment;
import shopingmall.myshop.payment.domain.PaymentStatus;

import java.time.LocalDateTime;

public record PaymentResponse(
        Long paymentId,
        Long orderId,
        PaymentStatus paymentStatus,
        OrderStatus orderStatus,
        long requestedAmount,
        long approvedAmount,
        long cancelledAmount,
        LocalDateTime approvedAt,
        LocalDateTime failedAt,
        LocalDateTime cancelledAt,
        String failureCode,
        String failureMessage
) {
    public static PaymentResponse from(Payment payment) {
        return new PaymentResponse(
                payment.getId(),
                payment.getOrder().getId(),
                payment.getPaymentStatus(),
                payment.getOrder().getOrderStatus(),
                payment.getRequestedAmount(),
                payment.getApprovedAmount(),
                payment.getCancelledAmount(),
                payment.getApprovedAt(),
                payment.getFailedAt(),
                payment.getCancelledAt(),
                payment.getFailureCode(),
                payment.getFailureMessage()
        );
    }
}
