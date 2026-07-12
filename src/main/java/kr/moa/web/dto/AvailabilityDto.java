package kr.moa.web.dto;

/**
 * GET /api/availability 항목.
 *   특정 날짜의 (교실, 교시) 별 예약 수(REJECTED 제외).
 *   프론트에서 remaining = capacity - count 로 계산한다.
 */
public record AvailabilityDto(Long roomId, String periodId, long count) {
}
