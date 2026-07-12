package kr.moa.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * moa.* 커스텀 설정 바인딩 클래스.
 *   moa.school-name / moa.jwt.* / moa.periods[] 를 담는다.
 *
 * record 대신 일반 클래스를 쓰는 이유: @ConfigurationProperties 는
 * setter(또는 생성자 바인딩)를 통해 값을 주입한다. 유지보수 편의상 setter 방식 사용.
 */
@ConfigurationProperties(prefix = "moa")
public class MoaProperties {

    /** 학교 이름 (기본값은 application.yml 에서 지정) */
    private String schoolName = "광주소프트웨어마이스터고";

    /** JWT 관련 설정 */
    private Jwt jwt = new Jwt();

    /** 야자 교시 목록 */
    private List<Period> periods = new ArrayList<>();

    // --- getters / setters ---
    public String getSchoolName() {
        return schoolName;
    }

    public void setSchoolName(String schoolName) {
        this.schoolName = schoolName;
    }

    public Jwt getJwt() {
        return jwt;
    }

    public void setJwt(Jwt jwt) {
        this.jwt = jwt;
    }

    public List<Period> getPeriods() {
        return periods;
    }

    public void setPeriods(List<Period> periods) {
        this.periods = periods;
    }

    /** 특정 periodId 의 교시 정보를 찾는다. 없으면 null. */
    public Period findPeriod(String periodId) {
        if (periodId == null) return null;
        return periods.stream()
                .filter(p -> periodId.equals(p.getId()))
                .findFirst()
                .orElse(null);
    }

    // ================= 중첩 설정 클래스 =================

    /** JWT 서명/만료 설정 */
    public static class Jwt {
        /** HMAC 서명 시크릿 (운영에서는 환경변수로 덮어쓸 것) */
        private String secret;
        /** 토큰 만료 시간(시간 단위) */
        private int expiryHours = 12;

        public String getSecret() {
            return secret;
        }

        public void setSecret(String secret) {
            this.secret = secret;
        }

        public int getExpiryHours() {
            return expiryHours;
        }

        public void setExpiryHours(int expiryHours) {
            this.expiryHours = expiryHours;
        }
    }

    /** 교시 하나 {id, label, time} */
    public static class Period {
        private String id;
        private String label;
        /** "HH:MM – HH:MM" 형식 (en dash, 공백 허용) */
        private String time;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getLabel() {
            return label;
        }

        public void setLabel(String label) {
            this.label = label;
        }

        public String getTime() {
            return time;
        }

        public void setTime(String time) {
            this.time = time;
        }
    }
}
