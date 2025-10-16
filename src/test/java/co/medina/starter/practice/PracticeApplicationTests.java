package co.medina.starter.practice;

import org.junit.jupiter.api.*;
import org.mockito.InjectMocks;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class PracticeApplicationTests {
    @InjectMocks
    PracticeApplication practiceApplication;

    @Test
    void contextLoads() {
        Assertions.assertNotNull(practiceApplication);
    }

}
