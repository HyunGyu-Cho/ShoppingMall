package shopingmall.myshop.payment.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import shopingmall.myshop.order.domain.Order;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "payment",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_payment_payment_key", columnNames = "payment_key"),
                @UniqueConstraint(name = "uk_payment_idempotency_key", columnNames = "idempotency_key")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "payment_id")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Column(name = "payment_key", nullable = false, length = 100)
    private String paymentKey;

    @Column(name = "idempotency_key", length = 100)
    private String idempotencyKey;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false, length = 30)
    private PaymentStatus paymentStatus;

    @Column(name = "requested_amount", nullable = false)
    private long requestedAmount;

    @Column(name = "approved_amount", nullable = false)
    private long approvedAmount;

    @Column(name = "cancelled_amount", nullable = false)
    private long cancelledAmount;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Column(name = "failed_at")
    private LocalDateTime failedAt;

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    @Column(name = "failure_code", length = 50)
    private String failureCode;

    @Column(name = "failure_message", length = 500)
    private String failureMessage;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public static Payment ready(Order order, String paymentKey, long requestedAmount) {
        if (order == null) {
            throw new IllegalArgumentException("주문은 필수입니다.");
        }
        if (paymentKey == null || paymentKey.isBlank()) {
            throw new IllegalArgumentException("결제 키는 필수입니다.");
        }
        if (requestedAmount < 0) {
            throw new IllegalArgumentException("결제 금액은 0 이상이어야 합니다.");
        }

        Payment payment = new Payment();
        payment.order = order;
        payment.paymentKey = paymentKey;
        payment.paymentStatus = PaymentStatus.READY;
        payment.requestedAmount = requestedAmount;
        payment.approvedAmount = 0;
        payment.cancelledAmount = 0;
        return payment;
    }

    public void approve(String idempotencyKey, long amount) {
        if (paymentStatus != PaymentStatus.READY) {
            throw new IllegalStateException("결제 준비 상태만 승인할 수 있습니다.");
        }
        if (amount != requestedAmount) {
            throw new IllegalArgumentException("결제 금액이 주문 금액과 일치하지 않습니다.");
        }
        this.idempotencyKey = idempotencyKey;
        this.paymentStatus = PaymentStatus.APPROVED;
        this.approvedAmount = amount;
        this.approvedAt = LocalDateTime.now();
    }

    public void fail(String failureCode, String failureMessage) {
        if (paymentStatus != PaymentStatus.READY) {
            throw new IllegalStateException("결제 준비 상태만 실패 처리할 수 있습니다.");
        }
        this.paymentStatus = PaymentStatus.FAILED;
        this.failureCode = failureCode;
        this.failureMessage = failureMessage;
        this.failedAt = LocalDateTime.now();
    }

    public void cancel(long cancelAmount) {
        if (paymentStatus != PaymentStatus.APPROVED) {
            throw new IllegalStateException("승인된 결제만 취소할 수 있습니다.");
        }
        if (cancelAmount <= 0 || cancelAmount > approvedAmount - cancelledAmount) {
            throw new IllegalArgumentException("취소 금액이 올바르지 않습니다.");
        }
        this.paymentStatus = PaymentStatus.CANCELLED;
        this.cancelledAmount += cancelAmount;
        this.cancelledAt = LocalDateTime.now();
    }

    @PrePersist
    private void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    private void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
