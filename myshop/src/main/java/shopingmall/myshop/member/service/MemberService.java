package shopingmall.myshop.member.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import shopingmall.myshop.member.domain.Member;
import shopingmall.myshop.member.dto.MemberCreateRequest;
import shopingmall.myshop.member.dto.MemberCreateResponse;
import shopingmall.myshop.member.dto.MemberLoginRequest;
import shopingmall.myshop.member.dto.MemberLoginResponse;
import shopingmall.myshop.member.dto.MemberResponse;
import shopingmall.myshop.member.dto.MemberUpdateRequest;
import shopingmall.myshop.member.dto.MemberUpdateResponse;
import shopingmall.myshop.member.repository.MemberRepository;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public MemberCreateResponse signUp(MemberCreateRequest request) {
        validateDuplicateEmail(request.getEmail());

        String passwordHash = passwordEncoder.encode(request.getPassword());
        Member member = Member.createUser(
                request.getEmail(),
                passwordHash,
                request.getName(),
                request.getPhone()
        );

        return MemberCreateResponse.from(memberRepository.save(member));
    }

    public MemberLoginResponse login(MemberLoginRequest request) {
        Member member = memberRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Member not found."));
        member.validateCanLogin();

        if (!passwordEncoder.matches(request.getPassword(), member.getPasswordHash())) {
            throw new IllegalArgumentException("비밀버호가 일치하지 않습니다");
        }

        return MemberLoginResponse.from(member);
    }

    public MemberResponse showMyInfo(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("Member not found."));
        return MemberResponse.from(member);
    }

    @Transactional
    public MemberUpdateResponse updateInfo(Long memberId, MemberUpdateRequest request) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("Member not found."));
        member.updateProfile(request.getName(), request.getPhone());
        return MemberUpdateResponse.from(member);
    }

    public Member findMemberForOrder(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("Member not found."));
        member.validateCanOrder();
        return member;
    }

    private void validateDuplicateEmail(String email) {
        if (memberRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email already exists.");
        }
    }
}
