package org.example.contentservice.controller;

import org.example.cloudcommon.SeataDemoRequest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/content")
public class InternalSeataDemoController {
    private final JdbcTemplate jdbcTemplate;

    public InternalSeataDemoController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @PostMapping("/seata-demo")
    public boolean createDemoContent(@RequestBody SeataDemoRequest request) {
        if (request == null || request.demoKey() == null || request.demoKey().isBlank()) {
            throw new IllegalArgumentException("demoKey 不能为空");
        }
        return jdbcTemplate.update(
                "INSERT INTO seata_demo_content (demo_key, note) VALUES (?, ?)",
                request.demoKey(), request.contentNote()) == 1;
    }
}
