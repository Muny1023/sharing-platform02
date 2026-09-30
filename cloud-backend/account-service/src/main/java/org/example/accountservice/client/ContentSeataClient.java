package org.example.accountservice.client;

import org.example.cloudcommon.SeataDemoRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "content-service")
public interface ContentSeataClient {
    @PostMapping("/internal/content/seata-demo")
    boolean createDemoContent(@RequestBody SeataDemoRequest request);
}
