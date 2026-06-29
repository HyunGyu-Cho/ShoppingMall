package shopingmall.myshop.review.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import shopingmall.myshop.member.domain.Member;
import shopingmall.myshop.order.domain.OrderItem;
import shopingmall.myshop.product.domain.Product;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "review",
        uniqueConstraints = @UniqueConstraint(name = "uk_review_order_item", columnNames = "order_item_id")
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "review_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_item_id", nullable = false)
    private OrderItem orderItem;

    @Column(nullable = false)
    private int rating;

    @Column(nullable = false, length = 1000)
    private String content;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    public static Review create(Member member, Product product, OrderItem orderItem, int rating, String content) {
        if (member == null || product == null || orderItem == null) {
            throw new IllegalArgumentException("리뷰 작성자, 상품, 주문 상품은 필수입니다.");
        }
        if (rating < 1 || rating > 5) {
            throw new IllegalArgumentException("평점은 1점 이상 5점 이하이어야 합니다.");
        }
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("리뷰 내용은 필수입니다.");
        }

        Review review = new Review();
        review.member = member;
        review.product = product;
        review.orderItem = orderItem;
        review.rating = rating;
        review.content = content;
        return review;
    }

    @PrePersist
    private void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }
}
