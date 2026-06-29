package shopingmall.myshop.member.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import shopingmall.myshop.member.domain.Member;
import shopingmall.myshop.member.domain.enums.MemberStatus;
import shopingmall.myshop.member.domain.enums.Role;

@Getter
@AllArgsConstructor
public class MemberResponse {

    // 내 정보 조회 응답 DTO 작성
    // memberId 반환
    private Long id;

    // email 반환
    private String email;

    // name 반환
    private String name;

    // phone 마스킹 후 반환
    private String phone;

    // role 반환
    private Role role;

    // status 반환
    private MemberStatus status;

    public static MemberResponse from(Member member) {
        return new MemberResponse(
                member.getId(),
                member.getEmail(),
                member.getName(),
                member.getPhone(),
                member.getRole(),
                member.getStatus()
        );
    }

}