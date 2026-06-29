package shopingmall.myshop.order.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import shopingmall.myshop.member.domain.Member;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "orders",
        uniqueConstraints = @UniqueConstraint(name = "uk_orders_order_no", columnNames = "order_no")
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_id")
    private Long id;

    @Column(name = "order_no", nullable = false, length = 40)
    private String orderNo;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Enumerated(EnumType.STRING)
    @Column(name = "order_status", nullable = false, length = 30)
    private OrderStatus orderStatus;

    @Column(name = "total_price", nullable = false)
    private long totalPrice;

    @Column(name = "total_discount_amount", nullable = false)
    private long totalDiscountAmount;

    @Column(name = "final_payment_amount", nullable = false)
    private long finalPaymentAmount;

    @Column(name = "ordered_at", nullable = false)
    private LocalDateTime orderedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private final List<OrderItem> orderItems = new ArrayList<>();

    public static Order createPending(Member member, String orderNo) {
        if (member == null) {
            throw new IllegalArgumentException("회원은 필수입니다.");
        }
        if (orderNo == null || orderNo.isBlank()) {
            throw new IllegalArgumentException("주문 번호는 필수입니다.");
        }

        Order order = new Order();
        order.member = member;
        order.orderNo = orderNo;
        order.orderStatus = OrderStatus.PENDING_PAYMENT;
        order.orderedAt = LocalDateTime.now();
        return order;
    }

    public void addOrderItem(OrderItem orderItem) {
        if (orderItem == null) {
            throw new IllegalArgumentException("주문 상품은 필수입니다.");
        }
        orderItem.assignOrder(this);
        this.orderItems.add(orderItem);
        recalculateAmounts();
    }

    public void pay() {
        if (orderStatus != OrderStatus.PENDING_PAYMENT) {
            throw new IllegalStateException("결제 대기 주문만 결제 완료 처리할 수 있습니다.");
        }
        this.orderStatus = OrderStatus.PAID;
    }

    public void failPayment() {
        if (orderStatus != OrderStatus.PENDING_PAYMENT) {
            throw new IllegalStateException("결제 대기 주문만 결제 실패 처리할 수 있습니다.");
        }
        this.orderStatus = OrderStatus.PAYMENT_FAILED;
    }

    public void cancel() {
        if (orderStatus != OrderStatus.PENDING_PAYMENT && orderStatus != OrderStatus.PAID) {
            throw new IllegalStateException("취소할 수 없는 주문 상태입니다.");
        }
        this.orderStatus = OrderStatus.CANCELLED;
    }

    private void recalculateAmounts() {
        this.totalPrice = orderItems.stream().mapToLong(OrderItem::getTotalOrderPrice).sum();
        this.totalDiscountAmount = orderItems.stream().mapToLong(OrderItem::getDiscountAmount).sum();
        this.finalPaymentAmount = totalPrice - totalDiscountAmount;
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
