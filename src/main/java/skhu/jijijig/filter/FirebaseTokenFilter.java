package skhu.jijijig.filter;

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
import java.util.Arrays;

@Component
@RequiredArgsConstructor
public class FirebaseTokenFilter extends OncePerRequestFilter {
    private final MemberRepository memberRepository;
    private final TokenProvider tokenProvider;
    private final FirebaseAuth firebaseAuth;

    // 인증이 필요하지 않은 URL 목록
    private static final List<String> EXCLUDE_URLS = Arrays.asList(
            "/api/introduction",
            "/api/member/join",
            "/api/member/login",
            "/api/member/logout",
            "/api/member/refresh",
            "/v3/api-docs/**",
            "/swagger-ui/**"
    );

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String path = request.getRequestURI();
        if (EXCLUDE_URLS.stream().anyMatch(excludePath -> path.matches(excludePath.replace("/**", ".*")))) {
            filterChain.doFilter(request, response);
            return;
        }

        String header = request.getHeader("Authorization");
        if (isInvalidHeader(header)) {
            throw new FirebaseAuthenticationException("Authorization 헤더가 없거나 Bearer 토큰이 포함되지 않았습니다.", HttpStatus.UNAUTHORIZED);
        }

        try {
            String firebaseToken = header.substring(7);
            FirebaseToken decodedToken = firebaseAuth.verifyIdToken(firebaseToken);
            Member member = getOrCreateMember(decodedToken);
            authenticateMember(member, response);
        } catch (FirebaseAuthException e) {
            throw new FirebaseAuthenticationException("Firebase 인증 오류: " + e.getMessage(), HttpStatus.UNAUTHORIZED);
        }
        filterChain.doFilter(request, response);
    }

    private boolean isInvalidHeader(String header) {
        return header == null || !header.startsWith("Bearer ");
    }

    private Member getOrCreateMember(FirebaseToken decodedToken) {
        return memberRepository.findByEmail(decodedToken.getEmail())
                .orElseGet(() -> registerNewMember(decodedToken));
    }

    private Member registerNewMember(FirebaseToken firebaseToken) {
        return memberRepository.save(Member.builder()
                .email(firebaseToken.getEmail())
                .name(firebaseToken.getName())
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