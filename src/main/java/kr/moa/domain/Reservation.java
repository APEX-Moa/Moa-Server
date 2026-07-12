package kr.moa.domain;

import jakarta.persistence.*;

import java.time.Instant;
import java.time.LocalDate;

/**
 * 야자 교실 예약 한 건.
 *
 * 설계 메모:
 *  - room 은 ManyToOne(LAZY)로 연결하되, 교실 이름이 나중에 바뀌거나 삭제되어도
 *    예약 이력이 깨지지 않도록 roomName 을 별도 컬럼에 복사(비정규화) 저장한다.
 *  - (studentNo, date, periodId) 유니크 제약으로 같은 학생의 같은 교시 중복 예약을 DB 레벨에서 차단.
 *  - date 컬럼에 인덱스 → 날짜별 조회(관리자 화면, 정원 집계) 성능 확보.
 */
@Entity
@Table(name = "reservation",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_res_student_date_period",
                columnNames = {"student_no", "date", "period_id"}),
        indexes = @Index(name = "idx_res_date", columnList = "date"))
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "student_no", nullable = false)
    private String studentNo;

    @Column(name = "student_name", nullable = false)
    private String studentName;

    /** 예약한 교실 (지연 로딩) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    /** 교실 이름 복사본(이력 보존용) */
    @Column(name = "room_name", nullable = false)
    private String roomName;

    @Column(name = "period_id", nullable = false)
    private String periodId;

    @Column(nullable = false)
    private LocalDate date;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private ReservationStatus status;

    /** 승인 처리한 교직원 이름(이름이 없으면 username). 미승인이면 null */
    @Column(name = "approved_by")
    private String approvedBy;

    @Column(name = "checked_in", nullable = false)
    private boolean checkedIn;

    /** 출석 체크 시각 (미출석이면 null) */
    @Column(name = "check_in_time")
    private Instant checkInTime;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected Reservation() {
        // JPA 기본 생성자
    }

    public Reservation(String studentNo, String studentName, Room room,
                       String periodId, LocalDate date) {
        this.studentNo = studentNo;
        this.studentName = studentName;
        this.room = room;
        this.roomName = room.getName();
        this.periodId = periodId;
        this.date = date;
        this.status = ReservationStatus.PENDING;
        this.checkedIn = false;
    }

    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }

    // --- getters / setters ---
    public Long getId() {
        return id;
    }

    public String getStudentNo() {
        return studentNo;
    }

    public void setStudentNo(String studentNo) {
        this.studentNo = studentNo;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public Room getRoom() {
        return room;
    }

    public void setRoom(Room room) {
        this.room = room;
        if (room != null) {
            this.roomName = room.getName();
        }
    }

    public String getRoomName() {
        return roomName;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }

    public String getPeriodId() {
        return periodId;
    }

    public void setPeriodId(String periodId) {
        this.periodId = periodId;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public ReservationStatus getStatus() {
        return status;
    }

    public void setStatus(ReservationStatus status) {
        this.status = status;
    }

    public String getApprovedBy() {
        return approvedBy;
    }

    public void setApprovedBy(String approvedBy) {
        this.approvedBy = approvedBy;
    }

    public boolean isCheckedIn() {
        return checkedIn;
    }

    public void setCheckedIn(boolean checkedIn) {
        this.checkedIn = checkedIn;
    }

    public Instant getCheckInTime() {
        return checkInTime;
    }

    public void setCheckInTime(Instant checkInTime) {
        this.checkInTime = checkInTime;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
