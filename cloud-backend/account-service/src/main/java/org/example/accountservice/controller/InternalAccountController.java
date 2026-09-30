package org.example.accountservice.controller;

import jakarta.annotation.Resource;
import org.example.accountservice.service.AccountService;
import org.example.cloudcommon.AccountSummary;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/internal/accounts")
public class InternalAccountController {
    @Resource
    private AccountService accountService;

    @PostMapping("/batch")
    public List<AccountSummary> batch(@RequestBody List<Integer> ids) {
        if (ids == null || ids.isEmpty()) return List.of();
        return accountService.listByIds(ids).stream()
                .map(account -> new AccountSummary(account.getId(), account.getUsername(),
                        account.getNickname(), account.getAvatar(), account.getRole()))
                .toList();
    }
}
