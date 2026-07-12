package kr.moa.service;

import java.time.LocalTime;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 교시 time 문자열("HH:MM – HH:MM")을 시작/종료 LocalTime 으로 파싱하는 유틸.
 *
 *  구분자는 en dash(–), hyphen(-), em dash(—), 물결(~) 등 무엇이 와도 견고하게 처리한다.
 *  앞뒤/사이 공백도 허용.  예) "18:30 – 20:00", "18:30-20:00"
 */
public final class PeriodTimeParser {

    private PeriodTimeParser() {}

    // 두 개의 HH:MM 을 찾아낸다 (구분자 종류 무관)
    private static final Pattern TIME_PATTERN =
            Pattern.compile("(\\d{1,2})\\s*:\\s*(\\d{2})\\D+(\\d{1,2})\\s*:\\s*(\\d{2})");

    public record Span(LocalTime start, LocalTime end) {}

    /**
     * 파싱 성공 시 Span, 실패 시 null 반환.
     */
    public static Span parse(String time) {
        if (time == null) return null;
        Matcher m = TIME_PATTERN.matcher(time);
        if (!m.find()) return null;
        try {
            int sh = Integer.parseInt(m.group(1));
            int sm = Integer.parseInt(m.group(2));
            int eh = Integer.parseInt(m.group(3));
            int em = Integer.parseInt(m.group(4));
            return new Span(LocalTime.of(sh, sm), LocalTime.of(eh, em));
        } catch (Exception e) {
            return null;
        }
    }
}
