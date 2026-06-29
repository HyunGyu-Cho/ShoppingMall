package shopingmall.myshop.member.dto;

import lombok.Getter;
import shopingmall.myshop.member.domain.Member;
import shopingmall.myshop.member.domain.enums.MemberStatus;

import java.time.LocalDateTime;

@Getter
public class MemberCreateResponse {

    private Long id;
    private String email;
    private String name;
    private MemberStatus status;
    private LocalDateTime createdAt;

    private MemberCreateResponse(
            Long id,
            String email,
            String name,
            MemberStatus status,
            LocalDateTime createdAt
    ) {
        this.id = id;
        this.email = email;
        this.name = name;
        this.status = status;
        this.createdAt = createdAt;
    }

    public static MemberCreateResponse from(Member member) {
        return new MemberCreateResponse(
                member.getId(),
                member.getEmail(),
                member.getName(),
                member.getStatus(),
                member.getCreatedAt()
        );
    }
}