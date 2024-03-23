package skhu.jijijig.token;

import io.jsonwebtoken.io.Decoders;
import lombok.extern.slf4j.Slf4j;
import skhu.jijijig.domain.dto.TokenDTO;
import skhu.jijijig.domain.model.Member;
import skhu.jijijig.service.TokenBlackListService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import jakarta.servlet.http.HttpServletRequest;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;

import java.security.Key;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.stream.Collectors;

@Component
@Slf4j
public class TokenProvider {
    private final Key key;
    private final long accessTokenValidityTime;
    private final long refreshTokenValidityTime;
    private final TokenBlackListService tokenBlackListService;

    public TokenProvider(@Value("${jwt.secret}") String secretKey,
                         @Value("${jwt.access-token-validity-in-milliseconds}") long accessTokenValidityTime,
                         @Value("${jwt.refresh-token-validity-in-milliseconds}") long refreshTokenValidityTime,
                         TokenBlackListService tokenBlackListService) {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        this.key = Keys.hmacShaKeyFor(keyBytes);
        this.accessTokenValidityTime = accessTokenValidityTime;
        this.refreshTokenValidityTime = refreshTokenValidityTime;
        this.tokenBlackListService = tokenBlackListService;
    }

    public TokenDTO createTokens(Member member) {
        Date now = new Date();
        Date accessTokenExpiration = new Date(now.getTime() + accessTokenValidityTime);
        Date refreshTokenExpiration = new Date(now.getTime() + refreshTokenValidityTime);
        String accessToken = createJwtToken(member.getId().toString(), member.getRole().name(), accessTokenExpiration);
        String refreshToken = createJwtToken(member.getId().toString(), member.getRole().name(), refreshTokenExpiration);
        return TokenDTO.of(accessToken, refreshToken);
    }

    public TokenDTO renewToken(String refreshToken) {
        if (!validateToken(refreshToken)) {
            log.error("리프레시 토큰 검증 실패");
            throw new SecurityException("리프레시 토큰이 유효하지 않습니다.");
        }
        Claims claims = parseJwtToken(refreshToken);
        String subject = claims.getSubject();
        return createTokenForSubject(subject);
    }

    public String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    public boolean validateToken(String token) {
        try {
            if (tokenBlackListService.isBlackListed(token)) {
                throw new SecurityException("토큰이 블랙리스트에 포함되었습니다.");
            }
            parseJwtToken(token);
            return true;
        } catch (JwtException e) {
            log.error("토큰 검증 실패: {}", e.getMessage());
            return false;
        }
    }

    public Authentication getAuthentication(String accessToken) {
        try {
            Claims claims = parseJwtToken(accessToken);
            Collection<? extends GrantedAuthority> authorities =
                    Arrays.stream(claims.get("auth").toString().split(","))
                            .map(SimpleGrantedAuthority::new)
                            .collect(Collectors.toList());
            return new UsernamePasswordAuthenticationToken(claims.getSubject(), "", authorities);
        } catch (Exception e) {
            log.error("인증 정보 가져오기 실패: {}", e.getMessage());
            return null;
        }
    }

    private String createJwtToken(String subject, String authClaim, Date expiration) {
        return Jwts.builder()
                .setSubject(subject)
                .claim("auth", authClaim)
                .setExpiration(expiration)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    private Claims parseJwtToken(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException e) {
            log.error("토큰 만료: {}", e.getMessage());
            throw e;
        } catch (JwtException e) {
            log.error("토큰 파싱 실패: {}", e.getMessage());
            throw e;
        }
    }

    private TokenDTO createTokenForSubject(String subject) {
        Date now = new Date();
        Date accessTokenExpiration = new Date(now.getTime() + accessTokenValidityTime);
        String accessToken = createJwtToken(subject, "ACCESS", accessTokenExpiration);
        return TokenDTO.of(accessToken, "");
    }
}