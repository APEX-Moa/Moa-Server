package kr.moa.web.dto;

import jakarta.validation.constraints.NotBlank;
import kr.moa.domain.Room;

/**
 * 교실 응답 DTO.
 */
public record RoomDto(Long id, String name, String floor, int capacity, boolean active) {

    public static RoomDto from(Room room) {
        return new RoomDto(room.getId(), room.getName(), room.getFloor(),
                room.getCapacity(), room.isActive());
    }

    /** 교실 생성/수정 요청 */
    public record RoomRequest(
            @NotBlank String name,
            String floor,
            int capacity,
            Boolean active   // null 이면 기본 true 로 처리
    ) {}
}
