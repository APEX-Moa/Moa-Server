package kr.moa.security;

import kr.moa.web.ApiException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * 현재 로그인한 사용자(SecurityContext)의 정보를 편하게 꺼내는 헬퍼.
 *   컨트롤러/서비스에서 주입받아 사용한다.
 */
@Component
public class CurrentUser {

    /** 현재 인증 주체. 인증이 없으면 401 예외. */
    public AuthPrincipal require() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof AuthPrincipal principal)) {
            throw ApiException.unauthorized("로그인이 필요합니다.");
        }
        return principal;
    }

    /** 현재 학생의 학번. 학생이 아니면 403. */
    public String requireStudentNo() {
        AuthPrincipal p = require();
        if (!p.isStudent()) {
            throw ApiException.forbidden("학생만 사용할 수 있는 기능입니다.");
        }
        return p.subject();
    }

    /** 현재 교직원 username. 교직원이 아니면 403. */
    public String requireStaffUsername() {
        AuthPrincipal p = require();
        if (!p.isStaff()) {
            throw ApiException.forbidden("교직원만 사용할 수 있는 기능입니다.");
        }
        return p.subject();
    }
}
