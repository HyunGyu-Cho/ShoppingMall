package shopingmall.myshop.member.dto;

import lombok.Getter;
import shopingmall.myshop.member.domain.Member;

@Getter
public class MemberCreateResponse {

    private Long memberId;
    private String email;
    private String name;

    private MemberCreateResponse(
            Long memberId,
            String email,
            String name
    ) {
        this.memberId = memberId;
        this.email = email;
        this.name = name;
    }

    public static MemberCreateResponse from(Member member) {
        return new MemberCreateResponse(
                member.getId(),
                member.getEmail(),
                member.getName()
        );
    }
}
