package kr.moa;

import kr.moa.config.MoaProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

/**
 * Moa 애플리케이션 진입점.
 * - @EnableConfigurationProperties: moa.* 설정을 MoaProperties 로 바인딩
 */
@SpringBootApplication
@EnableConfigurationProperties(MoaProperties.class)
public class MoaApplication {

    public static void main(String[] args) {
        SpringApplication.run(MoaApplication.class, args);
    }
}
