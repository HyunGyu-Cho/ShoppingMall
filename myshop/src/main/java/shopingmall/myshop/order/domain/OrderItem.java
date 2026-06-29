package shopingmall.myshop.order.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import shopingmall.myshop.product.domain.Product;

@Entity
@Table(name = "order_item")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_item_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "product_name_snapshot", nullable = false, length = 255)
    private String productNameSnapshot;

    @Column(name = "order_price", nullable = false)
    private long orderPrice;

    @Column(nullable = false)
    private int quantity;

    @Column(name = "discount_amount", nullable = false)
    private long discountAmount;

    @Column(name = "option_name_snapshot", length = 255)
    private String optionNameSnapshot;

    public static OrderItem create(Product product, int quantity) {
        if (product == null) {
            throw new IllegalArgumentException("상품은 필수입니다.");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("주문 수량은 1 이상이어야 합니다.");
        }

        OrderItem orderItem = new OrderItem();
        orderItem.product = product;
        orderItem.productNameSnapshot = product.getName();
        orderItem.orderPrice = product.getPrice();
        orderItem.quantity = quantity;
        orderItem.discountAmount = 0;
        return orderItem;
    }

    void assignOrder(Order order) {
        this.order = order;
    }

    public long getTotalOrderPrice() {
        return orderPrice * quantity;
    }
}
