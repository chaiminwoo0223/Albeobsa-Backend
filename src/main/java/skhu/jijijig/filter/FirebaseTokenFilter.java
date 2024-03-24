package skhu.jijijig.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import skhu.jijijig.service.FirebaseService;

import java.io.IOException;
import java.util.List;
import java.util.Arrays;

@Component
@RequiredArgsConstructor
public class FirebaseTokenFilter extends OncePerRequestFilter {
    private final FirebaseService firebaseService;

    // 인증을 필요로 하는 URL 목록
    private static final List<String> INCLUDE_URLS = Arrays.asList(
            "/api/member/login",
            "/api/member/logout",
            "/api/member/refresh"
    );

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String path = request.getRequestURI();
        boolean requiresAuthentication = INCLUDE_URLS.stream().anyMatch(path::matches);
        if (requiresAuthentication) {
            String header = request.getHeader("Authorization");
            if (header == null || !header.startsWith("Bearer ")) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Authorization 헤더가 없거나 Bearer 토큰이 포함되지 않았습니다.");
                return;
            }
            String firebaseToken = header.substring("Bearer ".length());
            firebaseService.authenticateWithFirebaseToken(firebaseToken, response);
        }
        filterChain.doFilter(request, response);
    }
}