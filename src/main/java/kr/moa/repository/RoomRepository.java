package kr.moa.repository;

import kr.moa.domain.Room;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RoomRepository extends JpaRepository<Room, Long> {

    /** 활성 교실만 이름순으로 */
    List<Room> findByActiveTrueOrderByNameAsc();

    /** 전체를 이름순으로 (관리자) */
    List<Room> findAllByOrderByNameAsc();
}
