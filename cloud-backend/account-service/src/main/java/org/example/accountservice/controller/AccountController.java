package org.example.accountservice.controller;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.example.accountservice.entity.response.AccountResponse;
import org.example.accountservice.service.AccountService;
import org.example.cloudcommon.RestBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/user")
@Slf4j
public class AccountController {
    @Resource
    private AccountService accountService;

    @Value("${server.port:unknown}")
    private String serverPort;

    @GetMapping("/me")
    public RestBean<AccountResponse> me(@RequestAttribute("id") Integer id) {
        log.info("账号服务实例 {} 已处理 /api/user/me 请求", serverPort);
        return accountService.findAccountById(id)
                .map(account -> RestBean.success(new AccountResponse(account.getId(), account.getUsername(),
                        account.getNickname(), account.getAvatar(), account.getRole())))
                .orElseGet(() -> RestBean.failure(404, "账号不存在"));
    }

}
