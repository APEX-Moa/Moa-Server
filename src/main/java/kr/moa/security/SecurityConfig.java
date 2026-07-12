package kr.moa.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * Spring Security 6 설정 (Spring Boot 3).
 *   - 세션 없는 stateless JWT 인증
 *   - CSRF 비활성화(REST API + 토큰 인증)
 *   - CORS 허용(개발: 모든 오리진 / 운영: 도메인 제한 권장)
 *   - 공개 엔드포인트: 인증/설정/H2 콘솔
 *   - 그 외는 인증 필요, 일부는 @PreAuthorize 또는 아래 규칙으로 STAFF 제한
 */
@Configuration
@EnableMethodSecurity   // 컨트롤러의 @PreAuthorize 활성화
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    public SecurityConfig(JwtAuthFilter jwtAuthFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // REST + JWT 이므로 CSRF 불필요
                .csrf(AbstractHttpConfigurer::disable)
                // CORS 설정 적용
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                // 세션을 만들지 않는다 (토큰 기반)
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // H2 콘솔은 iframe 을 쓰므로 same-origin 프레임 허용
                .headers(h -> h.frameOptions(f -> f.sameOrigin()))
                .authorizeHttpRequests(auth -> auth
                        // 공개(비인증) 엔드포인트
                        .requestMatchers(HttpMethod.POST, "/api/auth/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/config").permitAll()
                        .requestMatchers("/h2/**").permitAll()
                        // 프리플라이트(OPTIONS) 허용
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        // 그 외는 인증 필요 (세부 STAFF 제한은 컨트롤러의 @PreAuthorize 로 처리)
                        .anyRequest().authenticated()
                )
                // JWT 필터를 표준 인증 필터 앞에 등록
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /** 비밀번호/PIN 해시용 BCrypt 인코더 */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * CORS 설정.
     *   개발 편의를 위해 모든 오리진을 허용한다.
     *   운영에서는 setAllowedOrigins 로 프론트엔드 도메인만 허용하도록 좁히세요.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(List.of("*"));  // 운영: 도메인 목록으로 제한
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setExposedHeaders(List.of("Authorization"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
