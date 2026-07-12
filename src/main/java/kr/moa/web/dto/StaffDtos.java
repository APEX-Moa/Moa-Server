package kr.moa.web.dto;

import jakarta.validation.constraints.NotBlank;
import kr.moa.domain.Staff;

/**
 * 교직원(교사) 계정 관리 DTO 모음. (ADMIN 전용)
 */
public final class StaffDtos {

    private StaffDtos() {}

    /** 교직원 응답 DTO (비밀번호 해시는 절대 노출하지 않는다) */
    public record StaffDto(
            Long id,
            String username,
            String name,
            String role      // "ADMIN" 또는 "STAFF"
    ) {
        public static StaffDto from(Staff s) {
            return new StaffDto(s.getId(), s.getUsername(), s.getName(), s.getRole().name());
        }
    }

    /** 교직원 생성 요청 */
    public record CreateStaffRequest(
            @NotBlank String username,
            @NotBlank String name,
            @NotBlank String password
    ) {}

    /** 비밀번호 재설정 요청 */
    public record ResetPasswordRequest(
            @NotBlank String password
    ) {}
}
