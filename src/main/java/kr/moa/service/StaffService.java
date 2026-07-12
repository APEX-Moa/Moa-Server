package kr.moa.service;

import kr.moa.domain.Staff;
import kr.moa.domain.StaffRole;
import kr.moa.repository.StaffRepository;
import kr.moa.web.ApiException;
import kr.moa.web.dto.StaffDtos.StaffDto;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 교직원(교사) 계정 관리 비즈니스 로직. (ADMIN 전용)
 */
@Service
public class StaffService {

    private final StaffRepository staffRepo;
    private final PasswordEncoder encoder;

    public StaffService(StaffRepository staffRepo, PasswordEncoder encoder) {
        this.staffRepo = staffRepo;
        this.encoder = encoder;
    }

    /** 전체 교직원 목록 */
    @Transactional(readOnly = true)
    public List<StaffDto> list() {
        return staffRepo.findAll().stream()
                .map(StaffDto::from)
                .toList();
    }

    /** 교사(STAFF) 계정 생성 */
    @Transactional
    public StaffDto create(String username, String name, String password) {
        if (staffRepo.existsByUsername(username)) {
            throw ApiException.conflict("이미 사용 중인 아이디예요.");
        }
        Staff staff = new Staff(username, encoder.encode(password), name, StaffRole.STAFF);
        staffRepo.save(staff);
        return StaffDto.from(staff);
    }

    /** 비밀번호 재설정 (누구든 대상 가능) */
    @Transactional
    public void resetPassword(Long id, String password) {
        Staff staff = staffRepo.findById(id)
                .orElseThrow(() -> ApiException.notFound("교직원 계정을 찾을 수 없어요."));
        staff.setPasswordHash(encoder.encode(password));
    }

    /** 교직원 삭제 (ADMIN 계정은 삭제 불가) */
    @Transactional
    public void delete(Long id) {
        Staff staff = staffRepo.findById(id)
                .orElseThrow(() -> ApiException.notFound("교직원 계정을 찾을 수 없어요."));
        if (staff.getRole() == StaffRole.ADMIN) {
            throw ApiException.badRequest("관리자 계정은 삭제할 수 없어요");
        }
        staffRepo.delete(staff);
    }
}
