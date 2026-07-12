package kr.moa.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import kr.moa.domain.Reservation;

import java.time.format.DateTimeFormatter;

/**
 * 예약 관련 DTO 모음.
 */
public final class ReservationDtos {

    private ReservationDtos() {}

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ISO_LOCAL_DATE; // YYYY-MM-DD

    /** 예약 생성 요청 (date: "YYYY-MM-DD") */
    public record CreateRequest(
            @NotNull Long roomId,
            @NotBlank String periodId,
            @NotBlank String date
    ) {}

    /** 출석 체크 요청 */
    public record CheckinRequest(
            @NotNull Long roomId
    ) {}

    /** 예약 응답 DTO */
    public record ReservationDto(
            Long id,
            String studentNo,
            String name,
            Long roomId,
            String roomName,
            String periodId,
            String date,           // "YYYY-MM-DD"
            String status,         // enum 이름(대문자)
            boolean checkedIn,
            Long checkInTime,      // epoch millis 또는 null
            String approvedBy      // 승인한 교직원 이름 (미승인이면 null)
    ) {
        public static ReservationDto from(Reservation r) {
            Long roomId = (r.getRoom() != null) ? r.getRoom().getId() : null;
            Long checkInMillis = (r.getCheckInTime() != null) ? r.getCheckInTime().toEpochMilli() : null;
            return new ReservationDto(
                    r.getId(),
                    r.getStudentNo(),
                    r.getStudentName(),
                    roomId,
                    r.getRoomName(),
                    r.getPeriodId(),
                    r.getDate().format(DATE_FMT),
                    r.getStatus().name(),
                    r.isCheckedIn(),
                    checkInMillis,
                    r.getApprovedBy()
            );
        }
    }

    /** 출석 체크 응답 */
    public record CheckinResponse(
            boolean ok,
            String code,
            String message,
            String roomName,
            String periodId
    ) {}
}
