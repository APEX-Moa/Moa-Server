package kr.moa.web.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * 인증 관련 요청/응답 DTO 모음.
 */
public final class AuthDtos {

    private AuthDtos() {}

    /** 학생 회원가입 요청 */
    public record StudentRegisterRequest(
            @NotBlank String studentNo,
            @NotBlank String name,
            @NotBlank String pin
    ) {}

    /** 학생 로그인 요청 */
    public record StudentLoginRequest(
            @NotBlank String studentNo,
            @NotBlank String pin
    ) {}

    /** 교직원 로그인 요청 */
    public record StaffLoginRequest(
            @NotBlank String username,
            @NotBlank String password
    ) {}

    /** 인증 공통 응답 (studentNo 는 교직원이면 null) */
    public record AuthResponse(
            String token,
            String role,
            String name,
            String studentNo
    ) {}
}
