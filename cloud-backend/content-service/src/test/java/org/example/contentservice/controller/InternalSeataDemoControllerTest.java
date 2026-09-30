package org.example.contentservice.controller;

import org.example.cloudcommon.SeataDemoRequest;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class InternalSeataDemoControllerTest {
    private final JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
    private final InternalSeataDemoController controller = new InternalSeataDemoController(jdbcTemplate);

    @Test
    void insertsDedicatedDemoRow() {
        SeataDemoRequest request = new SeataDemoRequest("demo-1", "account", "content");
        when(jdbcTemplate.update(any(String.class), eq("demo-1"), eq("content"))).thenReturn(1);

        assertTrue(controller.createDemoContent(request));
        verify(jdbcTemplate).update(any(String.class), eq("demo-1"), eq("content"));
    }
}
