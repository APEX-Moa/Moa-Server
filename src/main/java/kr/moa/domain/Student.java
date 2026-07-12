package kr.moa.domain;

import jakarta.persistence.*;

import java.time.Instant;

/**
 * 가입한 학생 계정.
 *   studentNo(학번)로 로그인/식별한다. pin(4자리 등)은 BCrypt 로 해시하여 저장.
 */
@Entity
@Table(name = "student",
        uniqueConstraints = @UniqueConstraint(name = "uk_student_no", columnNames = "student_no"))
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 학번 (고유) */
    @Column(name = "student_no", nullable = false, unique = true)
    private String studentNo;

    @Column(nullable = false)
    private String name;

    /** PIN 의 BCrypt 해시 */
    @Column(name = "pin_hash", nullable = false)
    private String pinHash;

    /** 학년 (학번 첫 글자에서 파생) */
    private String grade;

    /** 반 (학번 2~3번째 글자에서 파생) */
    private String klass;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected Student() {
        // JPA 기본 생성자
    }

    public Student(String studentNo, String name, String pinHash, String grade, String klass) {
        this.studentNo = studentNo;
        this.name = name;
        this.pinHash = pinHash;
        this.grade = grade;
        this.klass = klass;
    }

    /** 저장 직전 생성시각 자동 세팅 */
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPinHash() {
        return pinHash;
    }

    public void setPinHash(String pinHash) {
        this.pinHash = pinHash;
    }

    public String getGrade() {
        return grade;
    }

    public void setGrade(String grade) {
        this.grade = grade;
    }

    public String getKlass() {
        return klass;
    }

    public void setKlass(String klass) {
        this.klass = klass;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
