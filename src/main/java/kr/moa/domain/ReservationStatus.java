package kr.moa.domain;

/**
 * 예약 상태.
 *   PENDING  : 승인 대기
 *   APPROVED : 승인됨(출석 체크 가능)
 *   REJECTED : 거절됨(정원/중복 계산에서 제외)
 */
public enum ReservationStatus {
    PENDING,
    APPROVED,
    REJECTED
}
