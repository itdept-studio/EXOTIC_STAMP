package metro.ExoticStamp;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Disabled;
import org.springframework.boot.test.context.SpringBootTest;

@Disabled("Requires external infra (DB/Redis/JWT) not provided in unit-test runtime")
@SpringBootTest
class ExoticStampApplicationTests {

	@Test
	void contextLoads() {
	}

}
