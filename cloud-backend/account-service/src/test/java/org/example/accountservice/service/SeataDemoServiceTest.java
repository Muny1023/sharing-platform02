package org.example.accountservice.service;

import org.example.accountservice.client.ContentSeataClient;
import org.example.cloudcommon.SeataDemoRequest;
import org.example.cloudcommon.SeataDemoResult;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SeataDemoServiceTest {
    private final JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
    private final ContentSeataClient contentClient = mock(ContentSeataClient.class);
    private final SeataDemoService service = new SeataDemoService(jdbcTemplate, contentClient);

    @Test
    void successWritesBothBranches() {
        SeataDemoRequest request = new SeataDemoRequest("demo-1", "account", "content");
        when(jdbcTemplate.update(any(String.class), eq("demo-1"), eq("account"))).thenReturn(1);
        when(contentClient.createDemoContent(request)).thenReturn(true);

        SeataDemoResult result = service.execute(request, false);

        assertEquals(new SeataDemoResult("demo-1", true, true), result);
        verify(contentClient).createDemoContent(request);
    }

    @Test
    void requestedRollbackRaisesAfterCallingBothBranches() {
        SeataDemoRequest request = new SeataDemoRequest("demo-2", "account", "content");
        when(jdbcTemplate.update(any(String.class), eq("demo-2"), eq("account"))).thenReturn(1);
        when(contentClient.createDemoContent(request)).thenReturn(true);

        assertThrows(IllegalStateException.class, () -> service.execute(request, true));
        verify(contentClient).createDemoContent(request);
    }
}
