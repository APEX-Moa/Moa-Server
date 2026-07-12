package kr.moa.web;

import kr.moa.service.RosterService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 로스터(가입 허용 명단) API. 모두 STAFF 전용.
 */
@RestController
@RequestMapping("/api/roster")
@PreAuthorize("hasRole('STAFF')")
public class RosterController {

    private final RosterService rosterService;

    public RosterController(RosterService rosterService) {
        this.rosterService = rosterService;
    }

    /** "학번,이름" 여러 줄 텍스트를 업로드하여 upsert */
    @PostMapping("/bulk")
    public Map<String, Integer> bulk(@RequestBody BulkRequest req) {
        int count = rosterService.bulkUpsert(req.text());
        return Map.of("count", count);
    }

    /** 명단 총 인원 수 */
    @GetMapping("/count")
    public Map<String, Long> count() {
        return Map.of("count", rosterService.count());
    }

    /** 업로드 요청 바디 */
    public record BulkRequest(String text) {}
}
