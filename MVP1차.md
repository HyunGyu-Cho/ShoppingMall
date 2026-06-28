# MVP 1차 구현 설계

## 1. MVP 1차 목표

1차 MVP의 목표는 쇼핑몰 전체 기능을 모두 만드는 것이 아니다. 핵심은 **주문, 결제, 재고 정합성이 깨지지 않는 최소 주문 시스템**을 만드는 것이다.

이번 단계에서 증명해야 할 것은 다음이다.

- 사용자는 상품을 조회하고 주문할 수 있다.
- 주문 시 재고가 예약된다.
- 동시에 주문해도 재고가 음수가 되지 않는다.
- 결제 성공 시 주문 상태와 결제 상태가 함께 변경된다.
- 결제 성공 시 예약 재고가 확정 차감된다.
- 결제 실패 시 예약 재고가 복구된다.
- 주문 취소 시 상태와 재고가 일관되게 복구된다.
- 구매한 상품에만 리뷰를 작성할 수 있다.
- 같은 주문 상품에는 리뷰를 한 번만 작성할 수 있다.

따라서 1차 MVP는 화면, 쿠폰, 포인트, 실제 PG, 실제 배송사, Redis, MQ, Outbox를 제외한다. 대신 주문 흐름의 핵심 정합성을 테스트로 증명한다.

---

## 2. 1차 MVP에 포함할 기능

### 2-1. 회원

회원 기능은 복잡하게 만들지 않는다. 주문과 리뷰 작성의 주체를 식별할 수 있을 정도면 충분하다.

만들 기능:

- 회원가입
- 로그인 또는 임시 인증
- 내 정보 조회

처음부터 JWT, Refresh Token, Spring Security 전체 구성을 완벽하게 만들 필요는 없다. 다만 이미 프로젝트에서 인증 구조를 잡고 있다면 그 구조를 사용한다.

1차에서 중요한 것은 다음이다.

- `member_id`로 주문 소유자를 식별할 수 있어야 한다.
- 관리자 API와 사용자 API를 분리할 준비가 되어 있어야 한다.
- 이메일 중복 가입이 막혀야 한다.

### 2-2. 상품

상품은 주문 가능한 대상을 만들기 위한 최소 기능만 구현한다.

만들 기능:

- 상품 등록
- 상품 목록 조회
- 상품 상세 조회
- 상품 판매 상태 검증

상품 상태:

- `SELLING`: 판매 중
- `SOLD_OUT`: 품절
- `HIDDEN`: 숨김
- `DISCONTINUED`: 판매 종료

주문 가능한 상태는 `SELLING`만 허용한다.

### 2-3. 재고

재고는 1차 MVP의 핵심이다. 상품 테이블에 재고 수량을 두지 않고 `inventory` 테이블을 분리한다.

재고 수량:

- `total_quantity`: 전체 물리 재고
- `reserved_quantity`: 결제 대기 주문에 잡혀 있는 예약 재고
- `available_quantity`: 현재 주문 가능한 재고

기본 불변식:

```text
total_quantity = reserved_quantity + available_quantity
```

재고 변경은 일반 JPA dirty checking으로 처리하지 않는다. 반드시 조건부 `UPDATE` 전용 쿼리로 처리한다.

재고 예약:

```sql
UPDATE inventory
SET reserved_quantity = reserved_quantity + :quantity,
    available_quantity = available_quantity - :quantity
WHERE product_id = :productId
  AND available_quantity >= :quantity;
```

결제 성공 시 예약 재고 확정:

```sql
UPDATE inventory
SET reserved_quantity = reserved_quantity - :quantity,
    total_quantity = total_quantity - :quantity
WHERE product_id = :productId
  AND reserved_quantity >= :quantity;
```

결제 실패 또는 주문 취소 시 예약 재고 해제:

```sql
UPDATE inventory
SET reserved_quantity = reserved_quantity - :quantity,
    available_quantity = available_quantity + :quantity
WHERE product_id = :productId
  AND reserved_quantity >= :quantity;
```

여러 상품을 한 주문에서 처리할 때는 반드시 `product_id` 오름차순으로 재고를 예약한다. 이것은 데드락 가능성을 줄이기 위한 규칙이다.

### 2-4. 주문

주문은 상품과 회원을 연결하는 단순 데이터가 아니라 상태를 가진다.

1차 MVP 주문 상태:

- `PENDING_PAYMENT`: 결제 대기
- `PAID`: 결제 완료
- `PAYMENT_FAILED`: 결제 실패
- `CANCELLED`: 주문 취소

1차에서는 배송 준비, 배송 중, 배송 완료, 구매 확정까지 모두 구현하지 않아도 된다. 리뷰 조건 때문에 `DELIVERED` 또는 `CONFIRMED`가 필요할 수 있지만, 1차에서는 리뷰 테스트용 상태 변경 API 또는 내부 테스트 데이터로 처리해도 된다.

주문 생성 시 처리할 일:

1. 회원 존재 확인
2. 상품 존재 확인
3. 상품 판매 상태 확인
4. 주문 상품을 `product_id` 오름차순 정렬
5. 재고 예약
6. 주문 `PENDING_PAYMENT` 생성
7. 주문 상품 `order_item` 생성
8. 배송 정보 `delivery` 생성
9. 결제 `READY` 생성

주문 생성은 하나의 DB 트랜잭션에서 처리한다. 외부 API 호출은 하지 않는다.

### 2-5. 주문 상품

`order_item`은 단순 연결 테이블이 아니다. 주문 당시 상품 정보를 보존하는 스냅샷 테이블이다.

저장할 값:

- 주문 ID
- 상품 ID
- 주문 당시 상품명
- 주문 당시 상품 가격
- 주문 수량
- 주문 당시 옵션명, 옵션을 구현한다면
- 할인 금액, 1차에서는 0으로 둬도 됨

상품명이 나중에 바뀌어도 과거 주문 내역은 바뀌면 안 된다. 따라서 주문 상세 조회는 상품 테이블의 현재 이름이 아니라 `order_item.product_name_snapshot`을 기준으로 보여줘야 한다.

### 2-6. 배송

1차에서는 실제 배송사 연동을 하지 않는다. 주문 시 배송 정보만 저장한다.

만들 기능:

- 주문 생성 시 배송지 저장
- 주문 상세 조회 시 배송 정보 반환

1차 배송 상태:

- `READY`: 배송 준비

배송 시작, 송장 번호, 배송 완료 처리는 2차 이후로 미룬다.

### 2-7. 결제

실제 PG 연동은 제외하고 Mock 결제 API를 만든다.

만들 기능:

- Mock 결제 승인
- Mock 결제 실패
- 결제 중복 요청 방지

결제 상태:

- `READY`: 결제 준비
- `APPROVED`: 결제 승인
- `FAILED`: 결제 실패
- `CANCELLED`: 결제 취소

결제 승인 시 처리:

1. `Idempotency-Key` 확인
2. 주문 상태가 `PENDING_PAYMENT`인지 확인
3. 결제 금액과 주문 금액이 같은지 확인
4. 결제 상태를 `APPROVED`로 변경
5. 주문 상태를 `PAID`로 변경
6. 예약 재고를 확정 차감
7. 주문 이벤트 로그 저장

결제 실패 시 처리:

1. 주문 상태가 `PENDING_PAYMENT`인지 확인
2. 결제 상태를 `FAILED`로 변경
3. 주문 상태를 `PAYMENT_FAILED`로 변경
4. 예약 재고 해제
5. 실패 코드와 실패 메시지 저장
6. 주문 이벤트 로그 저장

결제 승인과 실패는 멱등하게 만든다. 같은 요청이 다시 들어와도 상태가 중복 변경되면 안 된다.

### 2-8. 주문 취소

1차에서는 배송 시작 전 취소만 구현한다.

취소 가능 조건:

- 주문 상태가 `PENDING_PAYMENT` 또는 `PAID`
- 배송 상태가 아직 `READY`

취소 처리:

- `PENDING_PAYMENT` 주문 취소: 예약 재고 해제 후 주문 `CANCELLED`
- `PAID` 주문 취소: Mock 결제 취소 후 주문 `CANCELLED`, 재고 복구

1차에서는 실제 PG 취소가 없으므로 Mock 결제 취소로 처리한다.

### 2-9. 리뷰

리뷰는 구매 검증과 중복 작성 방지를 보여주는 기능으로 구현한다.

리뷰 작성 조건:

- 로그인한 회원이어야 한다.
- `order_item`이 본인 주문에 속해야 한다.
- 주문 상태가 `DELIVERED` 또는 `CONFIRMED`여야 한다.
- 하나의 `order_item`에는 리뷰를 하나만 작성할 수 있다.

1차에서 배송 완료 흐름을 만들지 않는다면, 리뷰 테스트를 위해 관리자용 주문 상태 변경 API를 임시로 둘 수 있다. 단, README에는 테스트 보조 API라고 명확히 적는다.

중복 리뷰 방지는 애플리케이션 조회만으로 막지 않는다. DB UNIQUE 제약을 둔다.

```sql
ALTER TABLE review
ADD CONSTRAINT uk_review_order_item UNIQUE (order_item_id);
```

---

## 3. 1차 MVP에서 제외할 기능

다음 기능은 1차에서 구현하지 않는다.

- 실제 PG사 연동
- 실제 배송사 API 연동
- 쿠폰
- 포인트
- 정산
- Redis 캐시
- MessageQueue
- Outbox publisher
- 상품 이미지 업로드
- 상품 대량 업로드
- 관리자 고급 검색
- 배치 재실행 관리 화면
- 운영 모니터링 대시보드

제외하는 이유는 중요하지 않아서가 아니다. 1차 목표가 주문·결제·재고 정합성 검증이기 때문이다.

---

## 4. 만들 테이블

### 4-1. member

역할:

- 주문자 식별
- 리뷰 작성자 식별
- 관리자/사용자 권한 구분 준비

주요 컬럼:

- `member_id`
- `email`
- `password`
- `name`
- `phone`
- `role`
- `status`
- `created_at`
- `updated_at`
- `deleted_at`

제약:

- `email` UNIQUE

### 4-2. product

역할:

- 판매 상품 정보 저장

주요 컬럼:

- `product_id`
- `category_id`, 1차에서 카테고리를 생략하면 nullable 또는 제거 가능
- `name`
- `price`
- `status`
- `thumbnail_url`
- `description`
- `created_at`
- `updated_at`
- `deleted_at`

제약:

- `price >= 0`

인덱스:

- `(status, created_at desc, product_id desc)`
- 카테고리를 구현한다면 `(category_id, status, created_at desc, product_id desc)`

### 4-3. inventory

역할:

- 상품별 재고 관리
- 동시 주문 방어

주요 컬럼:

- `product_id`
- `total_quantity`
- `reserved_quantity`
- `available_quantity`
- `version`
- `updated_at`

제약:

- `total_quantity >= 0`
- `reserved_quantity >= 0`
- `available_quantity >= 0`
- `total_quantity = reserved_quantity + available_quantity`

### 4-4. orders

역할:

- 주문 헤더 저장
- 주문 상태 관리

주요 컬럼:

- `order_id`
- `order_no`
- `member_id`
- `order_status`
- `total_price`
- `total_discount_amount`
- `final_payment_amount`
- `ordered_at`
- `created_at`
- `updated_at`

제약:

- `order_no` UNIQUE
- 금액은 0 이상

인덱스:

- `(member_id, ordered_at desc, order_id desc)`
- `(order_status, ordered_at desc, order_id desc)`

### 4-5. order_item

역할:

- 주문 상품 저장
- 주문 당시 상품 스냅샷 보존
- 리뷰 작성 기준

주요 컬럼:

- `order_item_id`
- `order_id`
- `product_id`
- `product_name_snapshot`
- `order_price`
- `quantity`
- `discount_amount`
- `option_name_snapshot`

제약:

- `quantity > 0`
- `order_price >= 0`

인덱스:

- `(order_id)`
- `(product_id)`

### 4-6. delivery

역할:

- 주문 배송지 정보 저장

주요 컬럼:

- `delivery_id`
- `order_id`
- `receiver_name`
- `receiver_phone`
- `zipcode`
- `address1`
- `address2`
- `delivery_status`
- `created_at`
- `updated_at`

제약:

- `order_id` UNIQUE

### 4-7. payment

역할:

- 결제 준비, 승인, 실패, 취소 상태 저장
- 결제 멱등성 보장

주요 컬럼:

- `payment_id`
- `order_id`
- `payment_key`
- `idempotency_key`
- `payment_status`
- `requested_amount`
- `approved_amount`
- `cancelled_amount`
- `approved_at`
- `failed_at`
- `cancelled_at`
- `failure_code`
- `failure_message`
- `created_at`
- `updated_at`

제약:

- `payment_key` UNIQUE
- `idempotency_key` UNIQUE
- 금액은 0 이상

### 4-8. review

역할:

- 구매자 리뷰 저장
- 주문 상품 단위 중복 리뷰 방지

주요 컬럼:

- `review_id`
- `member_id`
- `product_id`
- `order_item_id`
- `rating`
- `content`
- `created_at`
- `updated_at`
- `deleted_at`

제약:

- `order_item_id` UNIQUE
- `rating between 1 and 5`

인덱스:

- `(product_id, created_at desc, review_id desc)`
- `(member_id, created_at desc)`

### 4-9. order_event_log

역할:

- 주문 상태 변경 이력 저장
- 장애 발생 시 추적 근거 제공

주요 컬럼:

- `order_event_log_id`
- `order_id`
- `from_status`
- `to_status`
- `event_type`
- `reason`
- `created_by`
- `created_at`

1차에서는 Outbox를 제외하더라도 이 테이블은 넣는 것을 추천한다. 상태 변경을 눈으로 추적할 수 있어 디버깅과 README 정리에 유리하다.

---

## 5. 만들 API

### 5-1. 회원 API

```text
POST /api/members
POST /api/login
GET  /api/members/me
```

1차에서는 인증이 핵심이 아니므로, 로그인 구현이 부담되면 임시 인증 방식으로 시작해도 된다. 단, 주문 API에서는 반드시 `memberId`를 식별해야 한다.

### 5-2. 상품 API

```text
POST /api/admin/products
GET  /api/products
GET  /api/products/{productId}
```

상품 목록은 cursor pagination을 사용한다.

응답에는 Entity를 직접 노출하지 않는다. 필요한 필드만 DTO로 반환한다.

### 5-3. 주문 API

```text
POST /api/orders
GET  /api/orders
GET  /api/orders/{orderId}
POST /api/orders/{orderId}/cancel
```

주문 생성 요청 예시:

```json
{
  "items": [
    {
      "productId": 1,
      "quantity": 2
    }
  ],
  "delivery": {
    "receiverName": "홍길동",
    "receiverPhone": "010-1234-5678",
    "zipcode": "12345",
    "address1": "서울시 강남구",
    "address2": "101호"
  }
}
```

주문 생성 응답 예시:

```json
{
  "success": true,
  "data": {
    "orderId": 1,
    "orderNo": "ORD-20260629-000001",
    "orderStatus": "PENDING_PAYMENT",
    "paymentKey": "PAY-..."
  },
  "error": null
}
```

### 5-4. 결제 API

```text
POST /api/payments/approve
POST /api/payments/fail
POST /api/payments/{paymentId}/cancel
```

결제 승인 API는 `Idempotency-Key` 헤더를 받는다.

```text
Idempotency-Key: ORD-20260629-000001-1
```

1차에서는 실제 PG 요청 없이 서버 내부에서 Mock 승인 처리한다.

### 5-5. 리뷰 API

```text
POST /api/reviews
GET  /api/products/{productId}/reviews
```

리뷰 작성 요청:

```json
{
  "orderItemId": 1,
  "rating": 5,
  "content": "배송도 빠르고 제품도 좋습니다."
}
```

---

## 6. 서비스 설계

### 6-1. OrderService.createOrder

책임:

- 주문 생성 전체 흐름 제어
- 상품 검증
- 재고 예약
- 주문, 주문 상품, 배송, 결제 준비 생성

주의:

- 재고 예약은 `product_id` 오름차순으로 처리한다.
- 하나라도 재고 예약에 실패하면 전체 주문을 실패시킨다.
- 주문 생성 트랜잭션 안에서 외부 API를 호출하지 않는다.

흐름:

```text
createOrder(memberId, request)
  -> 회원 조회
  -> 상품 목록 조회
  -> 상품 판매 상태 검증
  -> 주문 상품 product_id 오름차순 정렬
  -> 재고 예약
  -> 주문 생성
  -> 주문 상품 생성
  -> 배송 생성
  -> 결제 READY 생성
  -> 주문 이벤트 로그 저장
  -> 응답 반환
```

### 6-2. InventoryService.reserve

책임:

- 조건부 `UPDATE`로 재고 예약
- row count가 0이면 재고 부족 예외 발생

서비스에서 재고를 조회한 뒤 수량을 빼지 않는다. 반드시 Repository의 update 쿼리를 사용한다.

### 6-3. PaymentService.approve

책임:

- 결제 승인 멱등성 처리
- 주문 상태 검증
- 금액 검증
- 결제 승인 처리
- 주문 결제 완료 처리
- 예약 재고 확정

흐름:

```text
approvePayment(request, idempotencyKey)
  -> idempotencyKey로 기존 승인 결제 조회
  -> 있으면 기존 결과 반환
  -> 주문 조회
  -> 주문 상태 PENDING_PAYMENT 검증
  -> 결제 금액 검증
  -> payment APPROVED
  -> order PAID
  -> reserved stock confirm
  -> order_event_log 저장
```

### 6-4. PaymentService.fail

책임:

- 결제 실패 처리
- 예약 재고 해제
- 실패 사유 저장

흐름:

```text
failPayment(orderId, reason)
  -> 주문 조회
  -> 주문 상태 PENDING_PAYMENT 검증
  -> payment FAILED
  -> order PAYMENT_FAILED
  -> reserved stock release
  -> failure_code, failure_message 저장
  -> order_event_log 저장
```

### 6-5. OrderService.cancel

책임:

- 주문 취소 가능 여부 검증
- 결제 취소 Mock 처리
- 재고 복구
- 주문 상태 변경

취소는 결제 상태에 따라 다르게 처리한다.

- `PENDING_PAYMENT`: 예약 재고 해제
- `PAID`: 결제 취소 후 재고 복구

### 6-6. ReviewService.createReview

책임:

- 구매자 검증
- 주문 상태 검증
- 중복 리뷰 방지

중복 리뷰는 다음 두 단계로 막는다.

1. 애플리케이션에서 기존 리뷰 조회
2. DB UNIQUE 제약으로 최종 방어

동시 요청에서는 1번만으로 부족하므로 2번이 반드시 필요하다.

---

## 7. 테스트 설계

### 7-1. 도메인 테스트

대상:

- `Order.pay()`
- `Order.cancel()`
- `Payment.approve()`
- `Payment.fail()`
- `Inventory.reserve()`
- `Inventory.release()`

검증:

- 결제 대기 상태에서만 결제 완료 가능
- 이미 취소된 주문은 다시 취소 불가
- 배송 시작 이후 일반 취소 불가, 1차에서는 배송 상태가 READY인지 검증
- 재고 부족 시 예약 실패
- 결제 실패 시 예약 재고 해제

### 7-2. 통합 테스트

필수 테스트:

- 재고가 충분하면 주문 생성 성공
- 재고가 부족하면 주문 생성 실패
- 주문 생성 성공 시 `orders`, `order_item`, `delivery`, `payment READY` 저장
- 결제 승인 시 `order PAID`, `payment APPROVED`, 예약 재고 확정
- 결제 실패 시 `order PAYMENT_FAILED`, `payment FAILED`, 예약 재고 해제
- 주문 취소 시 재고 복구
- 구매하지 않은 상품 리뷰 작성 실패
- 같은 `order_item` 리뷰 중복 작성 실패

### 7-3. 동시성 테스트

이 MVP의 핵심 테스트다.

테스트 1: 재고 1개 상품에 100명 동시 주문

기대 결과:

- 성공 주문 수 = 1
- 실패 주문 수 = 99
- 최종 `available_quantity = 0`
- 최종 `reserved_quantity = 1`, 결제 전 기준
- 재고 음수 발생 없음

테스트 2: 같은 결제 승인 요청 10번 동시 호출

기대 결과:

- 승인 처리 1번
- 나머지는 기존 결과 반환
- `payment APPROVED` 1건
- `order PAID` 상태 중복 변경 없음
- 재고 확정 차감 1번

테스트 3: 같은 `order_item`에 리뷰 10개 동시 작성

기대 결과:

- 성공 리뷰 수 = 1
- 실패 리뷰 수 = 9
- DB UNIQUE 제약으로 중복 방지

### 7-4. 조회 성능 테스트

1차에서 대규모 부하 테스트까지 할 필요는 없다. 대신 다음은 확인한다.

- 상품 목록 조회가 Entity 전체 조회가 아니라 DTO 조회인지
- 주문 목록 조회에서 `order_item` 전체를 즉시 로딩하지 않는지
- 주문 목록 조회 시 N+1이 발생하지 않는지
- cursor pagination에서 중복/누락이 없는지
- 인덱스 적용 전/후 실행 계획 또는 응답 시간을 비교할 수 있는지

---

## 8. 구현 순서

### 8-1. 1단계: 프로젝트 기반 정리

- 패키지 구조 생성
- 공통 응답 포맷 생성
- 공통 에러 코드 생성
- 예외 처리 구조 생성
- DB 연결 확인

### 8-2. 2단계: 회원, 상품, 재고

- `Member` Entity
- `Product` Entity
- `Inventory` Entity
- 상품 등록 API
- 상품 목록 API
- 상품 상세 API
- 재고 조건부 `UPDATE` Repository

### 8-3. 3단계: 주문 생성

- `Order` Entity
- `OrderItem` Entity
- `Delivery` Entity
- `Payment` Entity
- 주문 생성 API
- 재고 예약
- 주문 스냅샷 저장
- 결제 READY 생성

### 8-4. 4단계: Mock 결제

- 결제 승인 API
- 결제 실패 API
- 예약 재고 확정
- 예약 재고 해제
- 결제 멱등성 처리
- 주문 이벤트 로그 저장

### 8-5. 5단계: 주문 조회와 취소

- 내 주문 목록 조회
- 주문 상세 조회
- 주문 취소 API
- 결제 취소 Mock 처리
- 재고 복구

### 8-6. 6단계: 리뷰

- 리뷰 작성 API
- 리뷰 목록 조회 API
- 구매자 검증
- 중복 리뷰 방지

### 8-7. 7단계: 테스트와 README 정리

- 통합 테스트 작성
- 동시성 테스트 작성
- 인덱스 적용
- N+1 점검
- README에 설계 의도와 테스트 결과 정리

---

## 9. 1차 MVP 완료 기준

다음 조건을 만족하면 1차 MVP는 완료로 본다.

- 주문 생성 시 재고가 예약된다.
- 재고 부족 시 주문 생성이 실패한다.
- 결제 성공 시 주문, 결제, 재고 상태가 일관되게 변경된다.
- 결제 실패 시 예약 재고가 복구된다.
- 주문 취소 시 재고가 복구된다.
- 같은 상품 재고 1개에 대해 100명 동시 주문 테스트를 통과한다.
- 같은 결제 승인 요청 중복 호출 테스트를 통과한다.
- 같은 주문 상품 리뷰 중복 작성 테스트를 통과한다.
- 주문 목록 조회에서 N+1 문제가 발생하지 않는다.
- 핵심 API가 공통 응답 포맷과 공통 에러 코드를 사용한다.

1차 MVP의 최종 문장은 다음이다.

> 이 프로젝트는 상품 CRUD 쇼핑몰이 아니라, 동시 주문 상황에서도 재고가 깨지지 않고 결제 성공/실패에 따라 주문과 재고 상태가 일관되게 유지되는 주문 시스템을 구현하는 것을 1차 목표로 한다.
