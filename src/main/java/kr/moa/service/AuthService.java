package kr.moa.service;

import kr.moa.domain.RosterEntry;
import kr.moa.domain.Staff;
import kr.moa.domain.Student;
import kr.moa.repository.RosterRepository;
import kr.moa.repository.StaffRepository;
import kr.moa.repository.StudentRepository;
import kr.moa.security.JwtService;
import kr.moa.web.ApiException;
import kr.moa.web.dto.AuthDtos.AuthResponse;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 인증(회원가입/로그인) 비즈니스 로직.
 */
@Service
public class AuthService {

    private final StudentRepository studentRepo;
    private final RosterRepository rosterRepo;
    private final StaffRepository staffRepo;
    private final PasswordEncoder encoder;
    private final JwtService jwtService;

    public AuthService(StudentRepository studentRepo, RosterRepository rosterRepo,
                       StaffRepository staffRepo, PasswordEncoder encoder, JwtService jwtService) {
        this.studentRepo = studentRepo;
        this.rosterRepo = rosterRepo;
        this.staffRepo = staffRepo;
        this.encoder = encoder;
        this.jwtService = jwtService;
    }

    /**
     * 학생 회원가입.
     *   - 로스터(가입 허용 명단)에 없으면 404
     *   - 명단에 이름이 있는데 다르면 400
     *   - 이미 가입된 학번이면 409
     */
    @Transactional
    public AuthResponse registerStudent(String studentNo, String name, String pin) {
        RosterEntry roster = rosterRepo.findById(studentNo)
                .orElseThrow(() -> ApiException.notFound("가입 대상 명단에 없는 학번입니다. 담당 선생님께 문의하세요."));

        // 명단에 이름이 등록돼 있으면 일치 확인
        if (roster.getName() != null && !roster.getName().isBlank()
                && !roster.getName().equals(name)) {
            throw ApiException.badRequest("학번과 이름이 일치하지 않습니다.");
        }

        if (studentRepo.existsByStudentNo(studentNo)) {
            throw ApiException.conflict("이미 가입된 학번입니다. 로그인해 주세요.");
        }

        // 학번에서 학년/반 파생 (예: "3101" → 학년 3, 반 10). 명단 값이 있으면 우선 사용.
        String grade = (roster.getGrade() != null) ? roster.getGrade() : gradeFromNo(studentNo);
        String klass = (roster.getKlass() != null) ? roster.getKlass() : klassFromNo(studentNo);

        Student student = new Student(studentNo, name, encoder.encode(pin), grade, klass);
        studentRepo.save(student);

        String token = jwtService.createToken(studentNo, JwtService.ROLE_STUDENT, name);
        return new AuthResponse(token, JwtService.ROLE_STUDENT, name, studentNo);
    }

    /** 학생 로그인: pinHash 검증 */
    @Transactional(readOnly = true)
    public AuthResponse loginStudent(String studentNo, String pin) {
        Student student = studentRepo.findByStudentNo(studentNo)
                .orElseThrow(() -> ApiException.unauthorized("학번 또는 PIN이 올바르지 않습니다."));
        if (!encoder.matches(pin, student.getPinHash())) {
            throw ApiException.unauthorized("학번 또는 PIN이 올바르지 않습니다.");
        }
        String token = jwtService.createToken(studentNo, JwtService.ROLE_STUDENT, student.getName());
        return new AuthResponse(token, JwtService.ROLE_STUDENT, student.getName(), studentNo);
    }

    /** 교직원 로그인 */
    @Transactional(readOnly = true)
    public AuthResponse loginStaff(String username, String password) {
        Staff staff = staffRepo.findByUsername(username)
                .orElseThrow(() -> ApiException.unauthorized("아이디 또는 비밀번호가 올바르지 않습니다."));
        if (!encoder.matches(password, staff.getPasswordHash())) {
            throw ApiException.unauthorized("아이디 또는 비밀번호가 올바르지 않습니다.");
        }
        // 계정의 실제 권한(ADMIN 또는 STAFF)을 JWT/응답에 담는다.
        String role = staff.getRole().name();
        String token = jwtService.createToken(username, role, staff.getName());
        // 교직원은 studentNo 가 없으므로 null
        return new AuthResponse(token, role, staff.getName(), null);
    }

    // 학번 첫 글자 = 학년
    private static String gradeFromNo(String studentNo) {
        return (studentNo != null && studentNo.length() >= 1) ? studentNo.substring(0, 1) : null;
    }

    // 학번 2~3번째 글자 = 반
    private static String klassFromNo(String studentNo) {
        return (studentNo != null && studentNo.length() >= 3) ? studentNo.substring(1, 3) : null;
    }
}
