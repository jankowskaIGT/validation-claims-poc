package com.scms.validationclaims;

import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")  // ← tells Spring to use application-test.yml from src/test/resources
class ValidationClaimsApplicationTests {

	@Test
	void contextLoads() {
	}
}
