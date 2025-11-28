package com.guaumiau;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.properties")
class GuaumiauApplicationTests {

	@Test
	void contextLoads() {
	}

}
