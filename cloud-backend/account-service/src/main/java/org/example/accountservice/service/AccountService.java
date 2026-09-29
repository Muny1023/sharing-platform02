package org.example.accountservice.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.example.accountservice.entity.Account;
import org.example.accountservice.entity.request.ConfirmResetRequest;
import org.example.accountservice.entity.request.EmailRegisterRequest;
import org.example.accountservice.entity.request.EmailResetRequest;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.Optional;

public interface AccountService extends IService<Account>, UserDetailsService {
    Account findAccountByNameOrEmail(String text);

    Optional<Account> findAccountById(Integer id);

    String registerEmailVerifyCode(String type, String email, String ip);

    String registerEmailAccount(EmailRegisterRequest request);

    String resetConfirm(ConfirmResetRequest request);

    String resetEmailAccountPassword(EmailResetRequest request);
}
