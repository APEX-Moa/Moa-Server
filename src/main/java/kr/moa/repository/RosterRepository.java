package kr.moa.repository;

import kr.moa.domain.RosterEntry;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 로스터(가입 허용 명단) 저장소. PK 는 학번(String).
 */
public interface RosterRepository extends JpaRepository<RosterEntry, String> {
    // findById(studentNo), existsById(studentNo), count() 등 기본 메서드 사용
}
