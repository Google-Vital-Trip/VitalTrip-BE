package com.vitaltrip;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@Disabled("DB 연결 필요 - 통합 테스트 환경에서만 실행")
class VitaltripApplicationTests {

    @Test
    void contextLoads() {
    }
}
