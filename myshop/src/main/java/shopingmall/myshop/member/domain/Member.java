package shopingmall.myshop.member.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import shopingmall.myshop.member.domain.enums.MemberStatus;
import shopingmall.myshop.member.domain.enums.Role;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "member",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_member_email",
                        columnNames = "email"
                )
        }

        /*
        indexes = {
                @Index(name = "idx_member_status", columnList = "status")
        }
        */
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_id")
    private Long id;

    @Column(nullable = false, length = 255)
    private String email;

    /**
     * 평문 비밀번호가 아니라 암호화된 비밀번호만 저장한다.
     */
    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(length = 20)
    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MemberStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "modified_at", nullable = false)
    private LocalDateTime modifiedAt;

    @Column(name = "withdrawn_at")
    private LocalDateTime withdrawnAt;

    /**
     * 일반 회원 생성
     */
    public static Member createUser(
            // id는 DB에서 auto_increment로 자동 생성
            String email,
            String passwordHash,
            String name,
            String phone
    ) {
        validateRequired(email, "이메일은 필수입니다.");
        validateRequired(passwordHash, "비밀번호는 필수입니다.");
        validateRequired(name, "이름은 필수입니다.");

        Member member = new Member();
        member.email = email;
        member.passwordHash = passwordHash;
        member.name = name;
        member.phone = phone;
        member.role = Role.USER;
        member.status = MemberStatus.ACTIVE;

        return member;
    }

    /**
     * 관리자 회원 생성
     */
    public static Member createAdmin(
            String email,
            String passwordHash,
            String name,
            String phone
    ) {
        validateRequired(email, "이메일은 필수입니다.");
        validateRequired(passwordHash, "비밀번호는 필수입니다.");
        validateRequired(name, "이름은 필수입니다.");

        Member member = new Member();
        member.email = email;
        member.passwordHash = passwordHash;
        member.name = name;
        member.phone = phone;
        member.role = Role.ADMIN;
        member.status = MemberStatus.ACTIVE;

        return member;
    }

    /**
     * 이메일 수정
     */
    public void updateEmail(String email) {
        validateActiveMember();
        validateRequired(email, "이메일은 필수입니다.");

        this.email = email;
    }

    /**
     * 비밀번호 수정
     *
     * passwordHash에는 암호화된 비밀번호를 넘긴다.
     */
    public void updatePassword(String passwordHash) {
        validateActiveMember();
        validateRequired(passwordHash, "비밀번호는 필수입니다.");

        this.passwordHash = passwordHash;
    }

    /**
     * 프로필 수정
     *
     * 이름과 전화번호를 함께 수정한다.
     */
    public void updateProfile(String name, String phone) {
        validateActiveMember();
        validateRequired(name, "이름은 필수입니다.");

        this.name = name;
        this.phone = phone;
    }

    /**
     * 전화번호만 수정
     */
    public void updatePhone(String phone) {
        validateActiveMember();

        this.phone = phone;
    }

    /**
     * 회원 탈퇴
     */
    public void withdraw() {
        if (this.status == MemberStatus.WITHDRAWN) {
            throw new IllegalStateException("이미 탈퇴한 회원입니다.");
        }

        if (this.status == MemberStatus.BLOCKED) {
            throw new IllegalStateException("차단된 회원은 일반 탈퇴 처리할 수 없습니다.");
        }

        this.status = MemberStatus.WITHDRAWN;
        this.withdrawnAt = LocalDateTime.now();
    }

    /**
     * 회원 차단
     */
    public void block() {
        if (this.status == MemberStatus.WITHDRAWN) {
            throw new IllegalStateException("탈퇴한 회원은 차단할 수 없습니다.");
        }

        if (this.status == MemberStatus.BLOCKED) {
            throw new IllegalStateException("이미 차단된 회원입니다.");
        }

        this.status = MemberStatus.BLOCKED;
    }

    /**
     * 휴면 회원으로 전환
     */
    public void markAsDormant() {
        if (this.status != MemberStatus.ACTIVE) {
            throw new IllegalStateException("활성 회원만 휴면 처리할 수 있습니다.");
        }

        this.status = MemberStatus.DORMANT;
    }

    /**
     * 회원 활성화
     *
     * 휴면 회원이나 차단 해제된 회원을 다시 ACTIVE 상태로 만든다.
     */
    public void activate() {
        if (this.status == MemberStatus.WITHDRAWN) {
            throw new IllegalStateException("탈퇴한 회원은 활성화할 수 없습니다.");
        }

        if (this.status == MemberStatus.ACTIVE) {
            throw new IllegalStateException("이미 활성 상태인 회원입니다.");
        }

        this.status = MemberStatus.ACTIVE;
    }

    /**
     * 회원 차단 해제
     */
    public void unblock() {
        if (this.status != MemberStatus.BLOCKED) {
            throw new IllegalStateException("차단된 회원만 차단 해제할 수 있습니다.");
        }

        this.status = MemberStatus.ACTIVE;
    }

    /**
     * 주문 가능한 회원인지 검증
     */
    public void validateCanOrder() {
        if (this.status != MemberStatus.ACTIVE) {
            throw new IllegalStateException("주문할 수 없는 회원 상태입니다.");
        }
    }

    /**
     * 로그인 가능한 회원인지 검증
     */
    public void validateCanLogin() {
        if (this.status == MemberStatus.WITHDRAWN) {
            throw new IllegalStateException("탈퇴한 회원입니다.");
        }

        if (this.status == MemberStatus.BLOCKED) {
            throw new IllegalStateException("차단된 회원입니다.");
        }
    }

    @PrePersist
    private void prePersist() {
        LocalDateTime now = LocalDateTime.now();

        this.createdAt = now;
        this.modifiedAt = now;

        if (this.role == null) {
            this.role = Role.USER;
        }

        if (this.status == null) {
            this.status = MemberStatus.ACTIVE;
        }
    }

    @PreUpdate
    private void preUpdate() {
        this.modifiedAt = LocalDateTime.now();
    }

    private void validateActiveMember() {
        if (this.status != MemberStatus.ACTIVE) {
            throw new IllegalStateException("활성 회원만 정보를 수정할 수 있습니다.");
        }
    }

    private static void validateRequired(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }
    }
}
