package shopingmall.myshop.product.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import shopingmall.myshop.product.domain.Product;
import shopingmall.myshop.product.dto.ProductCursor;
import shopingmall.myshop.product.dto.ProductItemResponse;
import shopingmall.myshop.product.dto.ProductListResponse;
import shopingmall.myshop.product.dto.enums.ProductSort;
import shopingmall.myshop.product.repository.ProductRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductService {

    private static final int DEFAULT_SIZE = 20;
    private static final int MAX_SIZE = 100;

    private final ProductRepository productRepository;

    /**
     * 상품 목록 조회의 핵심 비즈니스 흐름입니다.
     *
     * 1. sort, size, cursor 같은 요청 파라미터를 서버가 다루기 좋은 값으로 변환합니다.
     * 2. DB에서 클라이언트 요청 개수보다 1개 더 조회합니다.
     * 3. extra 1개가 있으면 다음 페이지가 있다는 뜻이므로 hasNext=true로 응답합니다.
     * 4. 실제 응답 items에는 요청한 size만큼만 담습니다.
     * 5. 마지막 item의 createdAt/productId로 nextCursor를 만듭니다.
     */
    public ProductListResponse getProducts(Long categoryId, String sort, String cursor, Integer size) {
        ProductSort productSort = ProductSort.from(sort);
        ProductCursor productCursor = ProductCursor.from(cursor);
        int pageSize = normalizeSize(size);

        // size + 1개를 조회해야 다음 페이지 존재 여부를 별도 count 쿼리 없이 알 수 있습니다.
        List<Product> products = findProducts(categoryId, productSort, productCursor, pageSize + 1);

        boolean hasNext = products.size() > pageSize;

        // hasNext 판별용으로 더 가져온 마지막 1개는 클라이언트 응답에서 제외합니다.
        List<Product> currentPageProducts = hasNext
                ? products.subList(0, pageSize)
                : products;

        List<ProductItemResponse> items = currentPageProducts.stream()
                .map(ProductItemResponse::from)
                .toList();

        String nextCursor = hasNext
                ? createNextCursor(currentPageProducts.get(currentPageProducts.size() - 1))
                : null;

        return new ProductListResponse(items, nextCursor, hasNext);
    }

    private List<Product> findProducts(
            Long categoryId,
            ProductSort sort,
            ProductCursor cursor,
            int limit
    ) {
        PageRequest pageRequest = PageRequest.of(0, limit);

        return switch (sort) {
            case LATEST -> productRepository.findLatest(
                    categoryId,
                    cursor.createdAt(),
                    cursor.productId(),
                    pageRequest
            );
            case PRICE_ASC -> productRepository.findPriceAsc(
                    categoryId,
                    cursor.createdAt(),
                    cursor.productId(),
                    pageRequest
            );
            case PRICE_DESC -> productRepository.findPriceDesc(
                    categoryId,
                    cursor.createdAt(),
                    cursor.productId(),
                    pageRequest
            );
        };
    }

    /**
     * size가 없으면 기본 20개를 사용합니다.
     * 너무 큰 size는 DB와 서버에 부담이 되므로 MAX_SIZE로 제한합니다.
     */
    private int normalizeSize(Integer size) {
        if (size == null) {
            return DEFAULT_SIZE;
        }

        if (size <= 0) {
            throw new IllegalArgumentException("size must be greater than 0");
        }

        return Math.min(size, MAX_SIZE);
    }

    /**
     * 다음 페이지 커서는 "createdAt_productId" 형식으로 만듭니다.
     * 예: 2026-06-28T11:50:00_99
     */
    private String createNextCursor(Product product) {
        return product.getCreatedAt() + "_" + product.getId();
    }
}
