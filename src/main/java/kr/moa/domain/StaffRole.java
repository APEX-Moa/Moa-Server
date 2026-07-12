package kr.moa.domain;

/**
 * 교직원 계정 권한.
 *   ADMIN : 슈퍼 관리자 (교직원 계정 관리 + 모든 교직원 권한 포함)
 *   STAFF : 개별 교사 (예약 승인/교실 관리 등 교직원 기능)
 */
public enum StaffRole {
    ADMIN,
    STAFF
}
