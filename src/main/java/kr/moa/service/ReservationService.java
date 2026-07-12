package kr.moa.service;

import kr.moa.config.MoaProperties;
import kr.moa.domain.Reservation;
import kr.moa.domain.ReservationStatus;
import kr.moa.domain.Room;
import kr.moa.domain.Student;
import kr.moa.repository.ReservationRepository;
import kr.moa.repository.RoomRepository;
import kr.moa.repository.StudentRepository;
import kr.moa.security.AuthPrincipal;
import kr.moa.security.CurrentUser;
import kr.moa.web.ApiException;
import kr.moa.web.dto.AvailabilityDto;
import kr.moa.web.dto.ReservationDtos.CheckinResponse;
import kr.moa.web.dto.ReservationDtos.ReservationDto;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * 예약/출석 비즈니스 로직.
 */
@Service
public class ReservationService {

    /** 출석 조기 체크 허용(교시 시작 몇 분 전부터) */
    private static final int EARLY_MINUTES = 20;
    /** me 조회 최대 개수 */
    private static final int MY_LIMIT = 100;

    private final ReservationRepository reservationRepo;
    private final RoomRepository roomRepo;
    private final StudentRepository studentRepo;
    private final MoaProperties props;
    private final CurrentUser currentUser;

    public ReservationService(ReservationRepository reservationRepo, RoomRepository roomRepo,
                              StudentRepository studentRepo, MoaProperties props,
                              CurrentUser currentUser) {
        this.reservationRepo = reservationRepo;
        this.roomRepo = roomRepo;
        this.studentRepo = studentRepo;
        this.props = props;
        this.currentUser = currentUser;
    }

    // ============ 학생 ============

    /**
     * 예약 생성.
     *   - 같은 날짜+교시에 이미 (REJECTED 아닌) 예약이 있으면 409
     *   - 교실 비활성/정원 초과면 409
     *
     *  @Transactional 로 조회(정원 계산)와 저장을 한 트랜잭션에 묶는다.
     *  ※ 아주 높은 동시성에서 완벽한 정원 보장이 필요하면
     *     비관적 락(SELECT ... FOR UPDATE)이나 유니크 제약 + 재시도를 추가할 수 있다.
     *     현재 규모(교실별 수십 명)에서는 트랜잭션 + 유니크 제약으로 충분하다.
     */
    @Transactional
    public ReservationDto create(String studentNo, Long roomId, String periodId, String dateStr) {
        LocalDate date = parseDate(dateStr);

        // 교시 유효성
        if (props.findPeriod(periodId) == null) {
            throw ApiException.badRequest("존재하지 않는 교시입니다.");
        }

        // 중복 예약 체크 (유니크 제약과 이중 방어)
        boolean dup = reservationRepo.existsByStudentNoAndDateAndPeriodIdAndStatusNot(
                studentNo, date, periodId, ReservationStatus.REJECTED);
        if (dup) {
            throw ApiException.conflict("이미 해당 교시에 예약이 있습니다.");
        }

        Room room = roomRepo.findById(roomId)
                .orElseThrow(() -> ApiException.notFound("교실을 찾을 수 없습니다."));
        if (!room.isActive()) {
            throw ApiException.conflict("현재 예약할 수 없는 교실입니다.");
        }

        // 정원 체크 (capacity 0 이면 제한 없음)
        if (room.getCapacity() > 0) {
            long count = reservationRepo.countByRoomIdAndDateAndPeriodIdAndStatusNot(
                    roomId, date, periodId, ReservationStatus.REJECTED);
            if (count >= room.getCapacity()) {
                throw ApiException.conflict("해당 교실/교시의 정원이 가득 찼습니다.");
            }
        }

        // 학생 이름은 계정에서 가져온다(없으면 학번으로 대체)
        String name = studentRepo.findByStudentNo(studentNo)
                .map(Student::getName)
                .orElse(studentNo);

        Reservation res = new Reservation(studentNo, name, room, periodId, date);
        try {
            reservationRepo.save(res);
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            // 유니크 제약(동시 요청) 위반 → 사용자 친화 메시지로 변환
            throw ApiException.conflict("이미 해당 교시에 예약이 있습니다.");
        }
        return ReservationDto.from(res);
    }

    /** 내 예약 목록(최신순, 최대 100건) */
    @Transactional(readOnly = true)
    public List<ReservationDto> myReservations(String studentNo) {
        return reservationRepo
                .findByStudentNoOrderByDateDescCreatedAtDesc(studentNo, PageRequest.of(0, MY_LIMIT))
                .stream()
                .map(ReservationDto::from)
                .toList();
    }

    /** 예약 취소 (본인만, 이미 출석했으면 불가) */
    @Transactional
    public void cancel(String studentNo, Long reservationId) {
        Reservation res = reservationRepo.findById(reservationId)
                .orElseThrow(() -> ApiException.notFound("예약을 찾을 수 없습니다."));
        if (!res.getStudentNo().equals(studentNo)) {
            throw ApiException.forbidden("본인의 예약만 취소할 수 있습니다.");
        }
        if (res.isCheckedIn()) {
            throw ApiException.conflict("이미 출석 처리된 예약은 취소할 수 없습니다.");
        }
        reservationRepo.delete(res);
    }

    /**
     * 출석 체크.
     *   오늘, 해당 교실의 APPROVED 예약을 찾아 시간 조건에 따라 출석 처리.
     */
    @Transactional
    public CheckinResponse checkin(String studentNo, Long roomId) {
        LocalDate today = LocalDate.now();

        Reservation res = reservationRepo.findByStudentNoAndRoomIdAndDateAndStatus(
                        studentNo, roomId, today, ReservationStatus.APPROVED)
                .orElse(null);

        if (res == null) {
            return new CheckinResponse(false, "NO_RESERVATION",
                    "오늘 이 교실에 승인된 예약이 없어요.", null, null);
        }

        String roomName = res.getRoomName();
        String periodId = res.getPeriodId();

        if (res.isCheckedIn()) {
            return new CheckinResponse(true, "ALREADY",
                    "이미 출석 처리되었어요", roomName, periodId);
        }

        // 교시 시간 파싱
        MoaProperties.Period period = props.findPeriod(periodId);
        PeriodTimeParser.Span span = (period != null) ? PeriodTimeParser.parse(period.getTime()) : null;
        if (span == null) {
            // 시간 정보를 못 읽으면 안전하게 출석 허용 (설정 오류가 학생을 막지 않도록)
            res.setCheckedIn(true);
            res.setCheckInTime(java.time.Instant.now());
            return new CheckinResponse(true, "OK", "출석이 완료되었어요!", roomName, periodId);
        }

        LocalTime now = LocalTime.now();
        LocalTime earliest = span.start().minusMinutes(EARLY_MINUTES);

        if (now.isBefore(earliest)) {
            return new CheckinResponse(false, "TOO_EARLY",
                    "아직 출석 시간이 아니에요. 교시 시작 " + EARLY_MINUTES + "분 전부터 가능해요.",
                    roomName, periodId);
        }
        if (now.isAfter(span.end())) {
            return new CheckinResponse(false, "TOO_LATE",
                    "출석 가능 시간이 지났어요.", roomName, periodId);
        }

        res.setCheckedIn(true);
        res.setCheckInTime(java.time.Instant.now());
        return new CheckinResponse(true, "OK", "출석이 완료되었어요!", roomName, periodId);
    }

    // ============ 가용 현황 ============

    /** 특정 날짜의 (교실,교시)별 예약 수(REJECTED 제외) */
    @Transactional(readOnly = true)
    public List<AvailabilityDto> availability(String dateStr) {
        LocalDate date = parseDate(dateStr);
        return reservationRepo.availabilityCounts(date, ReservationStatus.REJECTED).stream()
                .map(row -> new AvailabilityDto(
                        (Long) row[0],
                        (String) row[1],
                        (Long) row[2]))
                .toList();
    }

    // ============ 관리자 ============

    /** 특정 날짜의 전체 예약(오래된 순) */
    @Transactional(readOnly = true)
    public List<ReservationDto> byDate(String dateStr) {
        LocalDate date = parseDate(dateStr);
        return reservationRepo.findByDateOrderByCreatedAtAsc(date).stream()
                .map(ReservationDto::from)
                .toList();
    }

    @Transactional
    public ReservationDto approve(Long id) {
        Reservation res = reservationRepo.findById(id)
                .orElseThrow(() -> ApiException.notFound("예약을 찾을 수 없습니다."));
        res.setStatus(ReservationStatus.APPROVED);
        res.setApprovedBy(currentStaffName());
        return ReservationDto.from(res);
    }

    @Transactional
    public ReservationDto reject(Long id) {
        Reservation res = reservationRepo.findById(id)
                .orElseThrow(() -> ApiException.notFound("예약을 찾을 수 없습니다."));
        res.setStatus(ReservationStatus.REJECTED);
        res.setApprovedBy(null);
        return ReservationDto.from(res);
    }

    /** 특정 날짜의 PENDING 예약을 모두 승인 → 승인 건수 반환 */
    @Transactional
    public int bulkApprove(String dateStr) {
        LocalDate date = parseDate(dateStr);
        String approver = currentStaffName();
        List<Reservation> pending =
                reservationRepo.findByDateAndStatus(date, ReservationStatus.PENDING);
        pending.forEach(r -> {
            r.setStatus(ReservationStatus.APPROVED);
            r.setApprovedBy(approver);
        });
        return pending.size();
    }

    /** 현재 로그인한 교직원 표시 이름(이름 없으면 username). */
    private String currentStaffName() {
        AuthPrincipal p = currentUser.require();
        if (p.name() != null && !p.name().isBlank()) {
            return p.name();
        }
        return p.subject();
    }

    // ============ 내부 유틸 ============

    private static LocalDate parseDate(String dateStr) {
        try {
            return LocalDate.parse(dateStr); // ISO YYYY-MM-DD
        } catch (Exception e) {
            throw ApiException.badRequest("날짜 형식이 올바르지 않습니다. (YYYY-MM-DD)");
        }
    }
}
