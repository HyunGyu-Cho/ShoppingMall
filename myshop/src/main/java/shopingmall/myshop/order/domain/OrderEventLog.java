package shopingmall.myshop.order.domain;

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
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "order_event_log")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderEventLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_event_log_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Enumerated(EnumType.STRING)
    @Column(name = "from_status", length = 30)
    private OrderStatus fromStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "to_status", nullable = false, length = 30)
    private OrderStatus toStatus;

    @Column(name = "event_type", nullable = false, length = 50)
    private String eventType;

    @Column(length = 500)
    private String reason;

    @Column(name = "created_by", nullable = false, length = 50)
    private String createdBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public static OrderEventLog create(Order order, OrderStatus fromStatus, OrderStatus toStatus,
                                       String eventType, String reason, String createdBy) {
        if (order == null) {
            throw new IllegalArgumentException("주문은 필수입니다.");
        }
        if (toStatus == null) {
            throw new IllegalArgumentException("변경 후 상태는 필수입니다.");
        }

        OrderEventLog log = new OrderEventLog();
        log.order = order;
        log.fromStatus = fromStatus;
        log.toStatus = toStatus;
        log.eventType = eventType;
        log.reason = reason;
        log.createdBy = createdBy == null || createdBy.isBlank() ? "SYSTEM" : createdBy;
        return log;
    }

    @PrePersist
    private void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}
