package kr.moa.repository;

import kr.moa.domain.Reservation;
import kr.moa.domain.ReservationStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    /** 특정 학생의 예약을 최신순으로 (Pageable 로 개수 제한) */
    List<Reservation> findByStudentNoOrderByDateDescCreatedAtDesc(String studentNo, Pageable pageable);

    /** 특정 날짜의 전체 예약 (관리자) - 생성순(오래된 순) */
    List<Reservation> findByDateOrderByCreatedAtAsc(LocalDate date);

    /** 특정 날짜 + 상태 */
    List<Reservation> findByDateAndStatus(LocalDate date, ReservationStatus status);

    /**
     * 특정 교실/날짜/교시에 대해 REJECTED 를 제외한 예약 수 (정원 계산용).
     */
    long countByRoomIdAndDateAndPeriodIdAndStatusNot(Long roomId, LocalDate date,
                                                     String periodId, ReservationStatus excluded);

    /** 학생의 특정 교실/날짜/상태 예약 (출석 체크용: APPROVED 조회) */
    Optional<Reservation> findByStudentNoAndRoomIdAndDateAndStatus(String studentNo, Long roomId,
                                                                   LocalDate date, ReservationStatus status);

    /**
     * 특정 날짜의 교실+교시별 예약 수(REJECTED 제외)를 그룹으로 집계.
     * 반환: Object[] { roomId(Long), periodId(String), count(Long) }
     */
    @Query("""
            select r.room.id, r.periodId, count(r)
            from Reservation r
            where r.date = :date and r.status <> :excluded
            group by r.room.id, r.periodId
            """)
    List<Object[]> availabilityCounts(@Param("date") LocalDate date,
                                      @Param("excluded") ReservationStatus excluded);

    /**
     * 같은 학생이 같은 날짜+교시에 REJECTED 아닌 예약을 이미 가지고 있는지 확인(중복 예약 방지).
     */
    boolean existsByStudentNoAndDateAndPeriodIdAndStatusNot(String studentNo, LocalDate date,
                                                            String periodId, ReservationStatus excluded);
}
