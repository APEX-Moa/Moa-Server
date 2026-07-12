package kr.moa.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import kr.moa.config.MoaProperties;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

/**
 * JWT 발급/검증 서비스 (jjwt 0.12 API 기준).
 *
 *  토큰 구조:
 *   - subject : 학생이면 학번(studentNo), 교직원이면 username
 *   - claim "role" : "STUDENT" | "STAFF" | "ADMIN"
 *   - claim "name" : 표시용 이름
 */
@Service
public class JwtService {

    public static final String ROLE_STUDENT = "STUDENT";
    public static final String ROLE_STAFF = "STAFF";
    public static final String ROLE_ADMIN = "ADMIN";

    private final SecretKey key;
    private final int expiryHours;

    public JwtService(MoaProperties props) {
        String secret = props.getJwt().getSecret();
        if (secret == null || secret.getBytes(StandardCharsets.UTF_8).length < 32) {
            // HMAC-SHA256 은 최소 32바이트 키가 필요하다. 설정 실수를 조기에 알림.
            throw new IllegalStateException(
                    "moa.jwt.secret 이 너무 짧습니다. 최소 32바이트(권장 64바이트) 이상으로 설정하세요.");
        }
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expiryHours = props.getJwt().getExpiryHours();
    }

    /** 토큰 생성 */
    public String createToken(String subject, String role, String name) {
        Instant now = Instant.now();
        Instant exp = now.plus(expiryHours, ChronoUnit.HOURS);
        return Jwts.builder()
                .subject(subject)
                .claim("role", role)
                .claim("name", name)
                .issuedAt(Date.from(now))
                .expiration(Date.from(exp))
                .signWith(key)
                .compact();
    }

    /** 토큰 파싱(검증 포함). 서명/만료가 잘못되면 예외를 던진다. */
    public Claims parse(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
