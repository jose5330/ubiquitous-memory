package com.example.backend;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;


@SpringBootTest
@TestPropertySource("classpath:test.properties")
public class MyTest {

    @Test
    void testSomething() {
        System.out.println("Test is running!");
    }
}
