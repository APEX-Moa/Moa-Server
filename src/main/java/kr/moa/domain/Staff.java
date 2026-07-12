package kr.moa.domain;

import jakarta.persistence.*;

/**
 * 교직원(관리자) 계정.
 *   username 으로 로그인, 비밀번호는 BCrypt 해시로 저장.
 */
@Entity
@Table(name = "staff",
        uniqueConstraints = @UniqueConstraint(name = "uk_staff_username", columnNames = "username"))
public class Staff {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(nullable = false)
    private String name;

    /** 계정 권한 (기본 STAFF, 슈퍼 관리자는 ADMIN) */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private StaffRole role = StaffRole.STAFF;

    protected Staff() {
        // JPA 기본 생성자
    }

    /** 개별 교사(STAFF) 계정 생성 */
    public Staff(String username, String passwordHash, String name) {
        this(username, passwordHash, name, StaffRole.STAFF);
    }

    public Staff(String username, String passwordHash, String name, StaffRole role) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.name = name;
        this.role = role;
    }

    // --- getters / setters ---
    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public StaffRole getRole() {
        return role;
    }

    public void setRole(StaffRole role) {
        this.role = role;
    }
}
