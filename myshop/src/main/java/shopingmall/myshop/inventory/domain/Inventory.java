package shopingmall.myshop.inventory.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import shopingmall.myshop.product.domain.Product;

import java.time.LocalDateTime;

@Entity
@Table(name = "inventory")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Inventory {

    @Id
    @Column(name = "product_id")
    private Long productId;

    @MapsId
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id")
    private Product product;

    @Column(name = "total_quantity", nullable = false)
    private int totalQuantity;

    @Column(name = "reserved_quantity", nullable = false)
    private int reservedQuantity;

    @Column(name = "available_quantity", nullable = false)
    private int availableQuantity;

    @Version
    private Long version;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public static Inventory create(Product product, int totalQuantity) {
        if (product == null) {
            throw new IllegalArgumentException("상품은 필수입니다.");
        }
        if (totalQuantity < 0) {
            throw new IllegalArgumentException("재고 수량은 0 이상이어야 합니다.");
        }

        Inventory inventory = new Inventory();
        inventory.product = product;
        inventory.totalQuantity = totalQuantity;
        inventory.reservedQuantity = 0;
        inventory.availableQuantity = totalQuantity;
        return inventory;
    }

    @PrePersist
    @PreUpdate
    private void touch() {
        validateInvariant();
        this.updatedAt = LocalDateTime.now();
    }

    private void validateInvariant() {
        if (totalQuantity < 0 || reservedQuantity < 0 || availableQuantity < 0) {
            throw new IllegalStateException("재고 수량은 음수가 될 수 없습니다.");
        }
        if (totalQuantity != reservedQuantity + availableQuantity) {
            throw new IllegalStateException("재고 불변식이 깨졌습니다.");
        }
    }
}
