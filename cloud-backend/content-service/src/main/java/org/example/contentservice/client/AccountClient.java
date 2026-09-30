package org.example.contentservice.client;

import org.example.cloudcommon.AccountSummary;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(name = "account-service")
public interface AccountClient {
    @PostMapping("/internal/accounts/batch")
    List<AccountSummary> findByIds(@RequestBody List<Integer> ids);
}
