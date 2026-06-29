package shopingmall.myshop.member.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import shopingmall.myshop.member.domain.Member;

@Data
@AllArgsConstructor
public class MemberUpdateResponse {
    private String email;
    private String name;
    private String phone;

    public static MemberUpdateResponse from(Member member) {
        return new MemberUpdateResponse(
                member.getEmail(),
                member.getName(),
                member.getPhone()
        );
    }
}
