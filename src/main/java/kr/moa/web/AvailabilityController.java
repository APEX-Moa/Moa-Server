package kr.moa.web;

import kr.moa.service.ReservationService;
import kr.moa.web.dto.AvailabilityDto;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 가용 현황 API. 인증된 사용자 누구나 조회.
 */
@RestController
@RequestMapping("/api/availability")
public class AvailabilityController {

    private final ReservationService reservationService;

    public AvailabilityController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    /** GET /api/availability?date=YYYY-MM-DD */
    @GetMapping
    public List<AvailabilityDto> availability(@RequestParam("date") String date) {
        return reservationService.availability(date);
    }
}
