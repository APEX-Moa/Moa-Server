package kr.moa.config;

import kr.moa.domain.Room;
import kr.moa.domain.Staff;
import kr.moa.domain.StaffRole;
import kr.moa.repository.RoomRepository;
import kr.moa.repository.StaffRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * 앱 시작 시 초기 데이터 시딩.
 *   - 교직원이 하나도 없으면 기본 관리자(admin/moa0000) 생성
 *   - 교실이 하나도 없으면 샘플 교실 몇 개 추가
 */
@Component
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final StaffRepository staffRepo;
    private final RoomRepository roomRepo;
    private final PasswordEncoder encoder;

    public DataSeeder(StaffRepository staffRepo, RoomRepository roomRepo, PasswordEncoder encoder) {
        this.staffRepo = staffRepo;
        this.roomRepo = roomRepo;
        this.encoder = encoder;
    }

    @Override
    public void run(String... args) {
        seedStaff();
        seedRooms();
    }

    private void seedStaff() {
        String username = "admin";
        String rawPassword = "moa0000";

        // 이미 admin 계정이 있으면 권한만 ADMIN 으로 보정 (구버전 데이터 마이그레이션)
        Staff existing = staffRepo.findByUsername(username).orElse(null);
        if (existing != null) {
            if (existing.getRole() != StaffRole.ADMIN) {
                existing.setRole(StaffRole.ADMIN);
                staffRepo.save(existing);
                log.info("기존 admin 계정의 권한을 ADMIN(슈퍼 관리자)으로 보정했습니다.");
            }
            return;
        }

        // 교직원이 하나도 없으면(또는 admin 이 없으면) 기본 슈퍼 관리자 생성
        Staff admin = new Staff(username, encoder.encode(rawPassword), "관리자", StaffRole.ADMIN);
        staffRepo.save(admin);

        // 기본 계정 정보를 딱 한 번 로그로 안내 (운영 배포 후 반드시 비밀번호 변경 권장)
        log.info("==================================================");
        log.info(" 기본 슈퍼 관리자(ADMIN) 계정이 생성되었습니다.");
        log.info("   아이디: {} / 비밀번호: {}", username, rawPassword);
        log.info("   (운영 환경에서는 반드시 비밀번호를 변경하세요)");
        log.info("==================================================");
    }

    private void seedRooms() {
        if (roomRepo.count() > 0) {
            return;
        }
        roomRepo.save(new Room("3-1 교실", "3층", 30, true));
        roomRepo.save(new Room("3-2 교실", "3층", 30, true));
        roomRepo.save(new Room("멀티미디어실", "2층", 24, true));
        log.info("샘플 교실 3개를 생성했습니다.");
    }
}
