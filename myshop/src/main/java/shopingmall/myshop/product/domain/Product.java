package shopingmall.myshop.product.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import shopingmall.myshop.category.domain.Category;
import shopingmall.myshop.product.domain.enums.ProductStatus;

import java.time.LocalDateTime;

@Entity
@Table(name = "product")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(nullable = false)
    private int price;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ProductStatus status;

    @Column(name = "thumbnail_url", length = 500)
    private String thumbnailUrl;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    public Product(Category category, String name, int price, ProductStatus status,
                   String thumbnailUrl, String description) {
        validateCategory(category);
        validateName(name);
        validatePrice(price);
        validateStatus(status);

        this.category = category;
        this.name = name;
        this.price = price;
        this.status = status;
        this.thumbnailUrl = thumbnailUrl;
        this.description = description;
    }

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public void changeBasicInfo(String name, int price, String thumbnailUrl, String description) {
        validateName(name);
        validatePrice(price);

        this.name = name;
        this.price = price;
        this.thumbnailUrl = thumbnailUrl;
        this.description = description;
    }

    public void changeCategory(Category category) {
        validateCategory(category);
        this.category = category;
    }

    public void changeStatus(ProductStatus status) {
        validateStatus(status);
        this.status = status;
    }

    // soft-delete 정책!!
    public void delete() {
        this.deletedAt = LocalDateTime.now();
        this.status = ProductStatus.DELETED;
    }

    private void validatePrice(int price) {
        if (price < 0) {
            throw new IllegalArgumentException("상품 가격은 0원 이상이어야 합니다.");
        }
    }

    private void validateCategory(Category category) {
        if (category == null) {
            throw new IllegalArgumentException("카테고리 값이 없습니다.");
        }
    }

    private void validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("조회되지 않는 상품입니다.");
        }
    }

    private void validateStatus(ProductStatus status) {
        if (status == null) {
            throw new IllegalArgumentException("Product status is required.");
        }
    }
}
