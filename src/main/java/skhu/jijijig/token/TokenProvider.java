package skhu.jijijig.token;

import io.jsonwebtoken.io.Decoders;
import skhu.jijijig.domain.dto.TokenDTO;
import skhu.jijijig.domain.Member;
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
public class TokenProvider {
    private final Key key;
    private final TokenRevoker tokenRevoker;
    private final long accessTokenValidityTime;
    private final long refreshTokenValidityTime;

    public TokenProvider(@Value("${jwt.secret}") String secretKey,
                         @Value("${jwt.access-token-validity-in-milliseconds}") long accessTokenValidityTime,
                         @Value("${jwt.refresh-token-validity-in-milliseconds}") long refreshTokenValidityTime,
                         TokenRevoker tokenRevoker) {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        this.key = Keys.hmacShaKeyFor(keyBytes);
        this.accessTokenValidityTime = accessTokenValidityTime;
        this.refreshTokenValidityTime = refreshTokenValidityTime;
        this.tokenRevoker = tokenRevoker;
    }

    // 사용자 정보를 기반으로 토큰을 생성하고, TokenDTO를 반환합니다.
    public TokenDTO createTokens(Member member) {
        String accessToken = createToken(member.getId().toString(), member.getRole().name(), accessTokenValidityTime);
        String refreshToken = createToken(member.getId().toString(), member.getRole().name(), refreshTokenValidityTime);
        return TokenDTO.of(accessToken, refreshToken);
    }

    // refresh 토큰을 갱신합니다.
    public TokenDTO renewToken(String refreshToken) {
        if (!validateToken(refreshToken)) {
            throw new SecurityException("리프레시 토큰이 유효하지 않습니다.");
        }
        Claims claims = parseJwtToken(refreshToken);
        String newAccessToken = createToken(claims.getSubject(), "ACCESS", accessTokenValidityTime);
        String newRefreshToken = createToken(claims.getSubject(), "REFRESH", refreshTokenValidityTime);
        return TokenDTO.of(newAccessToken, newRefreshToken);
    }

    // HttpServletRequest에서 토큰을 해석합니다.
    public String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    // 토큰의 유효성을 검증합니다.
    public boolean validateToken(String token) {
        try {
            if (tokenRevoker.isBlackListed(token)) {
                throw new SecurityException("토큰이 블랙리스트에 포함되었습니다.");
            }
            parseJwtToken(token);
            return true;
        } catch (SecurityException | JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    // 토큰에서 인증 정보를 추출하여, Authentication 객체를 생성합니다.
    public Authentication getAuthentication(String accessToken) {
        Claims claims = parseJwtToken(accessToken);
        Collection<? extends GrantedAuthority> authorities = Arrays.stream(claims.get("auth").toString().split(","))
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
        return new UsernamePasswordAuthenticationToken(claims.getSubject(), "", authorities);
    }

    // JWT 토큰을 생성합니다.
    private String createToken(String subject, String authClaim, long validityTime) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + validityTime);
        return Jwts.builder()
                .setSubject(subject)
                .claim("auth", authClaim)
                .setExpiration(expiration)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    // JWT 토큰을 파싱하여, Claims 객체를 반환합니다.
    private Claims parseJwtToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}