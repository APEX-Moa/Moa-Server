package kr.moa.web;

import jakarta.validation.Valid;
import kr.moa.security.CurrentUser;
import kr.moa.service.ReservationService;
import kr.moa.web.dto.ReservationDtos.CheckinRequest;
import kr.moa.web.dto.ReservationDtos.CheckinResponse;
import kr.moa.web.dto.ReservationDtos.CreateRequest;
import kr.moa.web.dto.ReservationDtos.ReservationDto;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 예약/출석 API.
 *   학생 기능은 CurrentUser 로 학번을 확인하여 본인 데이터만 다루도록 한다.
 *   관리자 기능은 @PreAuthorize("hasRole('STAFF')") 로 보호한다.
 */
@RestController
@RequestMapping("/api/reservations")
public class ReservationController {

    private final ReservationService reservationService;
    private final CurrentUser currentUser;

    public ReservationController(ReservationService reservationService, CurrentUser currentUser) {
        this.reservationService = reservationService;
        this.currentUser = currentUser;
    }

    // ============ 학생 ============

    /** 예약 생성 (STUDENT) */
    @PostMapping
    public ReservationDto create(@Valid @RequestBody CreateRequest req) {
        String studentNo = currentUser.requireStudentNo();
        return reservationService.create(studentNo, req.roomId(), req.periodId(), req.date());
    }

    /** 내 예약 목록 (STUDENT) */
    @GetMapping("/me")
    public List<ReservationDto> myReservations() {
        String studentNo = currentUser.requireStudentNo();
        return reservationService.myReservations(studentNo);
    }

    /** 예약 취소 (STUDENT 본인) → 204 */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancel(@PathVariable Long id) {
        String studentNo = currentUser.requireStudentNo();
        reservationService.cancel(studentNo, id);
        return ResponseEntity.noContent().build();
    }

    /** 출석 체크 (STUDENT) */
    @PostMapping("/checkin")
    public CheckinResponse checkin(@Valid @RequestBody CheckinRequest req) {
        String studentNo = currentUser.requireStudentNo();
        return reservationService.checkin(studentNo, req.roomId());
    }

    // ============ 관리자(STAFF) ============

    /** 특정 날짜의 전체 예약 (STAFF) */
    @GetMapping
    @PreAuthorize("hasRole('STAFF')")
    public List<ReservationDto> byDate(@RequestParam("date") String date) {
        return reservationService.byDate(date);
    }

    /** 예약 승인 (STAFF) */
    @PostMapping("/{id}/approve")
    @PreAuthorize("hasRole('STAFF')")
    public ReservationDto approve(@PathVariable Long id) {
        return reservationService.approve(id);
    }

    /** 예약 거절 (STAFF) */
    @PostMapping("/{id}/reject")
    @PreAuthorize("hasRole('STAFF')")
    public ReservationDto reject(@PathVariable Long id) {
        return reservationService.reject(id);
    }

    /** 특정 날짜 PENDING 일괄 승인 (STAFF) */
    @PostMapping("/bulk-approve")
    @PreAuthorize("hasRole('STAFF')")
    public Map<String, Integer> bulkApprove(@RequestParam("date") String date) {
        int approved = reservationService.bulkApprove(date);
        return Map.of("approved", approved);
    }
}
