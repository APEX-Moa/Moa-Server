package kr.moa.web.dto;

import kr.moa.config.MoaProperties;

import java.util.List;

/**
 * GET /api/config 응답. 학교명 + 교시 목록.
 */
public record ConfigDto(String schoolName, List<PeriodDto> periods) {

    public record PeriodDto(String id, String label, String time) {}

    /** MoaProperties → ConfigDto 변환 */
    public static ConfigDto from(MoaProperties props) {
        List<PeriodDto> periods = props.getPeriods().stream()
                .map(p -> new PeriodDto(p.getId(), p.getLabel(), p.getTime()))
                .toList();
        return new ConfigDto(props.getSchoolName(), periods);
    }
}
