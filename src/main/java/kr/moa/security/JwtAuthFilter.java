package kr.moa.security;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * 요청마다 Authorization: Bearer <token> 을 읽어 JWT 를 검증하고
 * SecurityContext 에 인증 정보를 세팅하는 필터.
 *
 *  - principal   : subject (학번 또는 username)
 *  - authorities : ROLE_STUDENT 또는 ROLE_STAFF
 *
 * 토큰이 없거나 잘못되면 컨텍스트를 비워둔 채 통과시키고,
 * 인가는 SecurityConfig 의 규칙(authenticated 등)이 처리한다.
 */
@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    public JwtAuthFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7).trim();
            try {
                Claims claims = jwtService.parse(token);
                String subject = claims.getSubject();
                String role = claims.get("role", String.class);
                String name = claims.get("name", String.class);

                if (subject != null && role != null) {
                    // Spring Security 권한 관례: "ROLE_" 접두사 + role 이름
                    // ADMIN 은 교직원 기능도 모두 쓸 수 있어야 하므로 ROLE_STAFF 도 함께 부여한다.
                    List<SimpleGrantedAuthority> authorities;
                    if (JwtService.ROLE_ADMIN.equals(role)) {
                        authorities = List.of(
                                new SimpleGrantedAuthority("ROLE_ADMIN"),
                                new SimpleGrantedAuthority("ROLE_STAFF"));
                    } else {
                        authorities = List.of(new SimpleGrantedAuthority("ROLE_" + role));
                    }
                    var principal = new AuthPrincipal(subject, role, name);
                    var auth = new UsernamePasswordAuthenticationToken(
                            principal, null, authorities);
                    SecurityContextHolder.getContext().setAuthentication(auth);
                }
            } catch (Exception ex) {
                // 유효하지 않은 토큰 → 인증 없이 진행 (보호된 엔드포인트에서 401 처리됨)
                SecurityContextHolder.clearContext();
            }
        }

        filterChain.doFilter(request, response);
    }
}
