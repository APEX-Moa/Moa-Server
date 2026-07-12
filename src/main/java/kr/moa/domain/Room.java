package kr.moa.domain;

import jakarta.persistence.*;

/**
 * 예약 대상 교실.
 *   capacity(정원)가 0 이면 인원 제한 없음으로 취급한다.
 */
@Entity
@Table(name = "room")
public class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    /** 층 (예: "3층") */
    private String floor;

    /** 정원 (0 이면 제한 없음) */
    @Column(nullable = false)
    private int capacity;

    /** 활성 여부 (비활성 교실은 예약 불가/목록 비노출) */
    @Column(nullable = false)
    private boolean active = true;

    protected Room() {
        // JPA 기본 생성자
    }

    public Room(String name, String floor, int capacity, boolean active) {
        this.name = name;
        this.floor = floor;
        this.capacity = capacity;
        this.active = active;
    }

    // --- getters / setters ---
    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFloor() {
        return floor;
    }

    public void setFloor(String floor) {
        this.floor = floor;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
