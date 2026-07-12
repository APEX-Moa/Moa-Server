package kr.moa.domain;

import jakarta.persistence.*;

/**
 * 가입 허용 명단(로스터) 한 줄.
 *   관리자가 업로드한 "학번,이름" 을 저장한다. 학번을 PK 로 사용.
 *   학생 가입 시 이 명단에 있어야만 가입을 허용한다.
 */
@Entity
@Table(name = "roster_entry")
public class RosterEntry {

    /** 학번 (PK) */
    @Id
    @Column(name = "student_no")
    private String studentNo;

    private String name;

    private String grade;

    private String klass;

    protected RosterEntry() {
        // JPA 기본 생성자
    }

    public RosterEntry(String studentNo, String name, String grade, String klass) {
        this.studentNo = studentNo;
        this.name = name;
        this.grade = grade;
        this.klass = klass;
    }

    // --- getters / setters ---
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
}
