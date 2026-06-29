package shopingmall.myshop.delivery.domain;

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
        name = "delivery",
        uniqueConstraints = @UniqueConstraint(name = "uk_delivery_order", columnNames = "order_id")
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Delivery {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "delivery_id")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Column(name = "receiver_name", nullable = false, length = 50)
    private String receiverName;

    @Column(name = "receiver_phone", nullable = false, length = 20)
    private String receiverPhone;

    @Column(nullable = false, length = 20)
    private String zipcode;

    @Column(nullable = false, length = 255)
    private String address1;

    @Column(length = 255)
    private String address2;

    @Enumerated(EnumType.STRING)
    @Column(name = "delivery_status", nullable = false, length = 30)
    private DeliveryStatus deliveryStatus;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public static Delivery create(Order order, String receiverName, String receiverPhone,
                                  String zipcode, String address1, String address2) {
        if (order == null) {
            throw new IllegalArgumentException("주문은 필수입니다.");
        }

        Delivery delivery = new Delivery();
        delivery.order = order;
        delivery.receiverName = require(receiverName, "수령인 이름은 필수입니다.");
        delivery.receiverPhone = require(receiverPhone, "수령인 전화번호는 필수입니다.");
        delivery.zipcode = require(zipcode, "우편번호는 필수입니다.");
        delivery.address1 = require(address1, "주소는 필수입니다.");
        delivery.address2 = address2;
        delivery.deliveryStatus = DeliveryStatus.READY;
        return delivery;
    }

    public boolean isReady() {
        return deliveryStatus == DeliveryStatus.READY;
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

    private static String require(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }
        return value;
    }
}
