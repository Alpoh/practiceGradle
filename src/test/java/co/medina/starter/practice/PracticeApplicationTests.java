package co.medina.starter.practice;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class PracticeApplicationTests {

    @InjectMocks
    private PracticeApplication practiceApplication;

    @Test
    void contextLoads() {
        Assertions.assertNotNull(practiceApplication);
    }

}
