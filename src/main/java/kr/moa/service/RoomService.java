package kr.moa.service;

import kr.moa.domain.Room;
import kr.moa.repository.RoomRepository;
import kr.moa.web.ApiException;
import kr.moa.web.dto.RoomDto;
import kr.moa.web.dto.RoomDto.RoomRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 교실 CRUD 로직.
 */
@Service
public class RoomService {

    private final RoomRepository roomRepo;

    public RoomService(RoomRepository roomRepo) {
        this.roomRepo = roomRepo;
    }

    /** 활성 교실만 (학생/일반) */
    @Transactional(readOnly = true)
    public List<RoomDto> listActive() {
        return roomRepo.findByActiveTrueOrderByNameAsc().stream()
                .map(RoomDto::from)
                .toList();
    }

    /** 전체 교실 (관리자) */
    @Transactional(readOnly = true)
    public List<RoomDto> listAll() {
        return roomRepo.findAllByOrderByNameAsc().stream()
                .map(RoomDto::from)
                .toList();
    }

    @Transactional
    public RoomDto create(RoomRequest req) {
        boolean active = (req.active() == null) || req.active();
        Room room = new Room(req.name(), req.floor(), req.capacity(), active);
        return RoomDto.from(roomRepo.save(room));
    }

    @Transactional
    public RoomDto update(Long id, RoomRequest req) {
        Room room = roomRepo.findById(id)
                .orElseThrow(() -> ApiException.notFound("교실을 찾을 수 없습니다."));
        room.setName(req.name());
        room.setFloor(req.floor());
        room.setCapacity(req.capacity());
        if (req.active() != null) {
            room.setActive(req.active());
        }
        return RoomDto.from(room); // 영속 상태이므로 flush 시 반영
    }

    @Transactional
    public void delete(Long id) {
        if (!roomRepo.existsById(id)) {
            throw ApiException.notFound("교실을 찾을 수 없습니다.");
        }
        roomRepo.deleteById(id);
    }
}
