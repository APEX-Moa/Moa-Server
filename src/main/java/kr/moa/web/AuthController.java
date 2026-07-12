package kr.moa.web;

import jakarta.validation.Valid;
import kr.moa.service.AuthService;
import kr.moa.web.dto.AuthDtos.AuthResponse;
import kr.moa.web.dto.AuthDtos.StaffLoginRequest;
import kr.moa.web.dto.AuthDtos.StudentLoginRequest;
import kr.moa.web.dto.AuthDtos.StudentRegisterRequest;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 인증 API. 모두 공개(비인증) 접근 가능.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/student/register")
    public AuthResponse registerStudent(@Valid @RequestBody StudentRegisterRequest req) {
        return authService.registerStudent(req.studentNo(), req.name(), req.pin());
    }

    @PostMapping("/student/login")
    public AuthResponse loginStudent(@Valid @RequestBody StudentLoginRequest req) {
        return authService.loginStudent(req.studentNo(), req.pin());
    }

    @PostMapping("/staff/login")
    public AuthResponse loginStaff(@Valid @RequestBody StaffLoginRequest req) {
        return authService.loginStaff(req.username(), req.password());
    }
}
