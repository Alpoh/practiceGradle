package co.medina.starter.practice.demo;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

class DemoSampleTest {
    @Test
    void demo_shouldAddNumbers() {
        int a = 2;
        int b = 3;
        assertEquals(5, a + b);
    }
}
