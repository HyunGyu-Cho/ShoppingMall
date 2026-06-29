package shopingmall.myshop.product.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import shopingmall.myshop.product.domain.Product;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 상품 DB 접근 레이어입니다.
 */
public interface ProductRepository extends JpaRepository<Product, Long> {

    /**
     * 최신순 목록 조회입니다.
     *
     * 조건 설명:
     * - deletedAt is null       : soft delete 된 상품 제외
     * - categoryId optional     : categoryId가 없으면 전체 카테고리 조회
     * - cursor optional         : cursor가 없으면 첫 페이지 조회
     * - createdAt desc, id desc : 최신순 정렬 + 같은 생성시간일 때 id로 안정적인 정렬
     *
     * Pageable은 offset 페이지네이션 용도가 아니라 limit(size + 1)을 전달하기 위해 사용합니다.
     */
    @Query("""
            select p
            from Product p
            where p.deletedAt is null
              and (:categoryId is null or p.category.id = :categoryId)
              and (
                    :cursorCreatedAt is null
                    or p.createdAt < :cursorCreatedAt
                    or (p.createdAt = :cursorCreatedAt and p.id < :cursorProductId)
                  )
            order by p.createdAt desc, p.id desc
            """)
    List<Product> findLatest(
            @Param("categoryId") Long categoryId,
            @Param("cursorCreatedAt") LocalDateTime cursorCreatedAt,
            @Param("cursorProductId") Long cursorProductId,
            Pageable pageable
    );

    /**
     * 가격 오름차순 목록 조회입니다.
     *
     * 현재 API 스펙의 cursor는 createdAt_productId 형식입니다.
     * 가격 정렬을 완전한 커서 페이지네이션으로 만들려면 price_productId 형식의 커서가 더 적합합니다.
     * 우선은 요청 스펙을 유지하면서 정렬 기능을 제공하기 위해 createdAt 기준 커서 필터를 같이 적용합니다.
     */
    @Query("""
            select p
            from Product p
            where p.deletedAt is null
              and (:categoryId is null or p.category.id = :categoryId)
              and (
                    :cursorCreatedAt is null
                    or p.createdAt < :cursorCreatedAt
                    or (p.createdAt = :cursorCreatedAt and p.id < :cursorProductId)
                  )
            order by p.price asc, p.id asc
            """)
    List<Product> findPriceAsc(
            @Param("categoryId") Long categoryId,
            @Param("cursorCreatedAt") LocalDateTime cursorCreatedAt,
            @Param("cursorProductId") Long cursorProductId,
            Pageable pageable
    );

    /**
     * 가격 내림차순 목록 조회입니다.
     *
     * 가격 정렬 커서의 한계는 findPriceAsc 주석과 동일합니다.
     */
    @Query("""
            select p
            from Product p
            where p.deletedAt is null
              and (:categoryId is null or p.category.id = :categoryId)
              and (
                    :cursorCreatedAt is null
                    or p.createdAt < :cursorCreatedAt
                    or (p.createdAt = :cursorCreatedAt and p.id < :cursorProductId)
                  )
            order by p.price desc, p.id desc
            """)
    List<Product> findPriceDesc(
            @Param("categoryId") Long categoryId,
            @Param("cursorCreatedAt") LocalDateTime cursorCreatedAt,
            @Param("cursorProductId") Long cursorProductId,
            Pageable pageable
    );
}
