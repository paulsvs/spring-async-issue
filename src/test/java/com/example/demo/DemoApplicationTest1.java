package com.example.demo;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;

@RunWith(SpringRunner.class)
@SpringBootTest
public class DemoApplicationTest1 {

    @MockBean private DemoApplication.ErrorHandler errorHandler;
    @SpyBean private JdbcTemplate jdbcTemplate;

    @Test
    public void transactionIsThere() {
        verify(jdbcTemplate).update(anyString());
    }

}
