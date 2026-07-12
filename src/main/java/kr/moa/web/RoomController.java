package kr.moa.web;

import jakarta.validation.Valid;
import kr.moa.service.RoomService;
import kr.moa.web.dto.RoomDto;
import kr.moa.web.dto.RoomDto.RoomRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 교실 API.
 *   - 조회(활성): 인증된 누구나
 *   - 전체 조회/생성/수정/삭제: STAFF 전용(@PreAuthorize)
 */
@RestController
@RequestMapping("/api/rooms")
public class RoomController {

    private final RoomService roomService;

    public RoomController(RoomService roomService) {
        this.roomService = roomService;
    }

    /** 활성 교실 목록 (인증된 사용자 누구나) */
    @GetMapping
    public List<RoomDto> list() {
        return roomService.listActive();
    }

    /** 전체 교실 목록 (STAFF) */
    @GetMapping("/all")
    @PreAuthorize("hasRole('STAFF')")
    public List<RoomDto> listAll() {
        return roomService.listAll();
    }

    /** 교실 생성 (STAFF) */
    @PostMapping
    @PreAuthorize("hasRole('STAFF')")
    public RoomDto create(@Valid @RequestBody RoomRequest req) {
        return roomService.create(req);
    }

    /** 교실 수정 (STAFF) */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('STAFF')")
    public RoomDto update(@PathVariable Long id, @Valid @RequestBody RoomRequest req) {
        return roomService.update(id, req);
    }

    /** 교실 삭제 (STAFF) → 204 */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('STAFF')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        roomService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
