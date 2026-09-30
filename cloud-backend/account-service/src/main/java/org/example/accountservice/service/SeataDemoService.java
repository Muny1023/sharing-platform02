package org.example.accountservice.service;

import io.seata.spring.annotation.GlobalTransactional;
import org.example.accountservice.client.ContentSeataClient;
import org.example.cloudcommon.SeataDemoRequest;
import org.example.cloudcommon.SeataDemoResult;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class SeataDemoService {
    private final JdbcTemplate jdbcTemplate;

    private final ContentSeataClient contentSeataClient;

    public SeataDemoService(JdbcTemplate jdbcTemplate, ContentSeataClient contentSeataClient) {
        this.jdbcTemplate = jdbcTemplate;
        this.contentSeataClient = contentSeataClient;
    }

    @GlobalTransactional(name = "seata-demo", rollbackFor = Exception.class)
    public SeataDemoResult execute(SeataDemoRequest request, boolean rollback) {
        requireValid(request);
        boolean accountSaved = jdbcTemplate.update(
                "INSERT INTO seata_demo_account (demo_key, note) VALUES (?, ?)",
                request.demoKey(), request.accountNote()) == 1;
        boolean contentSaved = contentSeataClient.createDemoContent(request);
        if (rollback) {
            throw new IllegalStateException("按请求触发 Seata 演示回滚");
        }
        return new SeataDemoResult(request.demoKey(), accountSaved, contentSaved);
    }

    private void requireValid(SeataDemoRequest request) {
        if (request == null || request.demoKey() == null || request.demoKey().isBlank()) {
            throw new IllegalArgumentException("demoKey 不能为空");
        }
    }
}
