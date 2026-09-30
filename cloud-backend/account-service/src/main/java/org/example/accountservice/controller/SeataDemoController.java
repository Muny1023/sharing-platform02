package org.example.accountservice.controller;

import jakarta.annotation.Resource;
import org.example.accountservice.service.SeataDemoService;
import org.example.cloudcommon.RestBean;
import org.example.cloudcommon.SeataDemoRequest;
import org.example.cloudcommon.SeataDemoResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/seata")
public class SeataDemoController {
    @Resource
    private SeataDemoService seataDemoService;

    @PostMapping("/demo")
    public RestBean<SeataDemoResult> demo(@RequestBody SeataDemoRequest request,
                                          @RequestParam(defaultValue = "false") boolean rollback) {
        return RestBean.success(seataDemoService.execute(request, rollback));
    }
}
