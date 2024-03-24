package skhu.jijijig.service;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import skhu.jijijig.domain.dto.TokenDTO;
import skhu.jijijig.domain.model.Member;
import skhu.jijijig.domain.model.Role;
import skhu.jijijig.domain.repository.MemberRepository;
import skhu.jijijig.exception.FirebaseAuthenticationException;
import skhu.jijijig.token.TokenProvider;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FirebaseService {
    private final FirebaseAuth firebaseAuth;
    private final MemberRepository memberRepository;
    private final TokenProvider tokenProvider;

    public void authenticateWithFirebaseToken(String firebaseToken, HttpServletResponse response) {
        try {
            FirebaseToken decodedToken = firebaseAuth.verifyIdToken(firebaseToken);
            Member member = getOrCreateMember(decodedToken);
            authenticateMember(member, response);
        } catch (FirebaseAuthException e) {
            throw new FirebaseAuthenticationException("Firebase 인증 오류: " + e.getMessage(), HttpStatus.UNAUTHORIZED);
        }
    }

    private Member getOrCreateMember(FirebaseToken decodedToken) {
        return memberRepository.findByEmail(decodedToken.getEmail())
                .orElseGet(() -> registerNewMember(decodedToken));
    }

    private Member registerNewMember(FirebaseToken firebaseToken) {
        return memberRepository.save(Member.builder()
                .name(firebaseToken.getName())
                .email(firebaseToken.getEmail())
                .role(Role.USER)
                .firebaseAuth(true)
                .build());
    }

    private void authenticateMember(Member member, HttpServletResponse response) {
        List<GrantedAuthority> authorities = Collections.singletonList(new SimpleGrantedAuthority(member.getRole().name()));
        Authentication authentication = new UsernamePasswordAuthenticationToken(member.getEmail(), null, authorities);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        TokenDTO tokenDTO = tokenProvider.createTokens(member);
        response.addHeader("Authorization", "Bearer " + tokenDTO.getAccessToken());
    }
}