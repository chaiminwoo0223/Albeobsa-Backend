package skhu.jijijig.token;

import io.jsonwebtoken.io.Decoders;
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

    public TokenDTO createToken(Member member) {
        Date now = new Date();
        Date accessTokenExpiration = new Date(now.getTime() + accessTokenValidityTime);
        Date refreshTokenExpiration = new Date(now.getTime() + refreshTokenValidityTime);
        String accessToken = createJwtToken(member.getId().toString(), member.getRole().name(), accessTokenExpiration);
        String refreshToken = createJwtToken(member.getId().toString(), member.getRole().name(), refreshTokenExpiration);
        return TokenDTO.of(accessToken, refreshToken);
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
                throw new SecurityException("블랙리스트에 포함된 토큰입니다.");
            }
            Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (JwtException e) {
            return false;
        }
    }

    public TokenDTO renewToken(String expiredToken) {
        Claims claims = parseClaims(expiredToken);
        String subject = claims.getSubject();
        return createTokenForSubject(subject);
    }

    public Authentication getAuthentication(String accessToken) {
        Claims claims = parseClaims(accessToken);
        Collection<? extends GrantedAuthority> authorities =
                Arrays.stream(claims.get("auth").toString().split(","))
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toList());
        return new UsernamePasswordAuthenticationToken(claims.getSubject(), "", authorities);
    }

    private String createJwtToken(String subject, String authClaim, Date expiration) {
        return Jwts.builder()
                .setSubject(subject)
                .claim("auth", authClaim)
                .setExpiration(expiration)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    private Claims parseClaims(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException e) {
            return e.getClaims();
        }
    }

    private TokenDTO createTokenForSubject(String subject) {
        Date now = new Date();
        Date accessTokenExpiration = new Date(now.getTime() + accessTokenValidityTime);
        String accessToken = createJwtToken(subject, "ACCESS", accessTokenExpiration);
        return TokenDTO.of(accessToken, null);
    }

}