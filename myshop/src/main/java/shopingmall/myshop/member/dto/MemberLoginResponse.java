package shopingmall.myshop.member.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import shopingmall.myshop.member.domain.Member;
import shopingmall.myshop.member.domain.enums.MemberStatus;
import shopingmall.myshop.member.domain.enums.Role;

@Getter
@AllArgsConstructor
public class MemberLoginResponse {

    private Long id;
    private String email;
    private String name;
    private Role role;
    private MemberStatus status;

    public static MemberLoginResponse from(Member member) {
        return new MemberLoginResponse(
                member.getId(),
                member.getEmail(),
                member.getName(),
                member.getRole(),
                member.getStatus()
        );
    }
}
