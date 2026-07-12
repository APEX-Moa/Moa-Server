package kr.moa.web;

import jakarta.validation.Valid;
import kr.moa.service.StaffService;
import kr.moa.web.dto.StaffDtos.CreateStaffRequest;
import kr.moa.web.dto.StaffDtos.ResetPasswordRequest;
import kr.moa.web.dto.StaffDtos.StaffDto;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 교직원(교사) 계정 관리 API. 슈퍼 관리자(ADMIN) 전용.
 */
@RestController
@RequestMapping("/api/staff")
@PreAuthorize("hasRole('ADMIN')")
public class StaffController {

    private final StaffService staffService;

    public StaffController(StaffService staffService) {
        this.staffService = staffService;
    }

    /** 전체 교직원 목록 (ADMIN) */
    @GetMapping
    public List<StaffDto> list() {
        return staffService.list();
    }

    /** 교사(STAFF) 계정 생성 (ADMIN) */
    @PostMapping
    public StaffDto create(@Valid @RequestBody CreateStaffRequest req) {
        return staffService.create(req.username(), req.name(), req.password());
    }

    /** 비밀번호 재설정 (ADMIN) → 204 */
    @PutMapping("/{id}/password")
    public ResponseEntity<Void> resetPassword(@PathVariable Long id,
                                              @Valid @RequestBody ResetPasswordRequest req) {
        staffService.resetPassword(id, req.password());
        return ResponseEntity.noContent().build();
    }

    /** 교직원 삭제 (ADMIN) → 204 */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        staffService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
