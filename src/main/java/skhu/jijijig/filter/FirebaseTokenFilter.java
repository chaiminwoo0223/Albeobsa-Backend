package skhu.jijijig.filter;

import com.google.firebase.auth.AuthErrorCode;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import skhu.jijijig.domain.dto.TokenDTO;
import skhu.jijijig.domain.model.Member;
import skhu.jijijig.domain.model.Role;
import skhu.jijijig.domain.repository.MemberRepository;
import skhu.jijijig.exception.FirebaseAuthenticationException;
import skhu.jijijig.token.TokenProvider;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

@Component
@RequiredArgsConstructor
public class FirebaseTokenFilter extends OncePerRequestFilter {
    private final MemberRepository memberRepository;
    private final TokenProvider tokenProvider;
    private final FirebaseAuth firebaseAuth;

    private static final String AUTH_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";
    private static final String JOIN_REQUEST_PATH = "/api/member/join";

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        if (isJoinRequest(request.getRequestURI())) {
            filterChain.doFilter(request, response);
            return;
        }

        String header = request.getHeader(AUTH_HEADER);
        if (isInvalidHeader(header)) {
            throw new FirebaseAuthenticationException("유효하지 않은 Authorization 헤더입니다.", HttpStatus.UNAUTHORIZED);
        }

        try {
            String firebaseToken = extractToken(header);
            FirebaseToken decodedToken = firebaseAuth.verifyIdToken(firebaseToken);
            Member member = getOrCreateMember(decodedToken);
            authenticateUser(member, response);
        } catch (FirebaseAuthException e) {
            handleAuthException(e);
        }
        filterChain.doFilter(request, response);
    }

    private boolean isJoinRequest(String requestUri) {
        return requestUri.startsWith(JOIN_REQUEST_PATH);
    }

    private boolean isInvalidHeader(String header) {
        return header == null || !header.startsWith(BEARER_PREFIX);
    }

    private String extractToken(String header) {
        return header.substring(BEARER_PREFIX.length());
    }

    private void handleAuthException(FirebaseAuthException e) {
        HttpStatus status = e.getAuthErrorCode() == AuthErrorCode.EXPIRED_ID_TOKEN ?
                HttpStatus.UNAUTHORIZED : HttpStatus.BAD_REQUEST;
        throw new FirebaseAuthenticationException("Firebase 인증 오류: " + e.getMessage(), status);
    }

    private Member getOrCreateMember(FirebaseToken decodedToken) {
        String email = decodedToken.getEmail();
        return memberRepository.findByEmail(email)
                .orElseGet(() -> registerNewMember(decodedToken));
    }

    private Member registerNewMember(FirebaseToken firebaseToken) {
        Member newMember = Member.builder()
                .email(firebaseToken.getEmail())
                .name(firebaseToken.getName())
                .role(Role.USER)
                .firebaseAuth(true)
                .build();
        return memberRepository.save(newMember);
    }

    private void authenticateUser(Member member, HttpServletResponse response) {
        List<GrantedAuthority> authorities = Collections.singletonList(new SimpleGrantedAuthority(member.getRole().name()));
        Authentication authentication = new UsernamePasswordAuthenticationToken(member.getEmail(), null, authorities);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        TokenDTO tokenDTO = tokenProvider.createTokens(member);
        response.addHeader("Authorization", "Bearer " + tokenDTO.getAccessToken());
    }
}