package kr.moa.web;

import kr.moa.config.MoaProperties;
import kr.moa.web.dto.ConfigDto;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 앱 설정(학교명/교시) 공개 API.
 */
@RestController
@RequestMapping("/api/config")
public class ConfigController {

    private final MoaProperties props;

    public ConfigController(MoaProperties props) {
        this.props = props;
    }

    @GetMapping
    public ConfigDto config() {
        return ConfigDto.from(props);
    }
}
