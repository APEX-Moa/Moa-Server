package kr.moa.security;

/**
 * SecurityContext 에 저장되는 인증 주체 정보.
 *   subject = 학번(STUDENT) 또는 username(STAFF)
 */
public record AuthPrincipal(String subject, String role, String name) {

    public boolean isStudent() {
        return JwtService.ROLE_STUDENT.equals(role);
    }

    public boolean isAdmin() {
        return JwtService.ROLE_ADMIN.equals(role);
    }

    /** ADMIN 은 교직원 권한을 포함하므로 STAFF 로도 취급한다. */
    public boolean isStaff() {
        return JwtService.ROLE_STAFF.equals(role) || isAdmin();
    }
}
