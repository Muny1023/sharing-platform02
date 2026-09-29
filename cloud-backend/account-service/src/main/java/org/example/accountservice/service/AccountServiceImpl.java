package org.example.accountservice.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.example.accountservice.entity.Account;
import org.example.accountservice.entity.request.ConfirmResetRequest;
import org.example.accountservice.entity.request.EmailRegisterRequest;
import org.example.accountservice.entity.request.EmailResetRequest;
import org.example.accountservice.mapper.AccountMapper;
import org.example.accountservice.util.AccountConstants;
import org.example.accountservice.util.FlowUtils;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@Service
public class AccountServiceImpl extends ServiceImpl<AccountMapper, Account> implements AccountService {
    @Resource
    private FlowUtils flowUtils;

    @Resource
    private AmqpTemplate amqpTemplate;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private PasswordEncoder encoder;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Account account = findAccountByNameOrEmail(username);
        if (account == null) {
            throw new UsernameNotFoundException("用户名或密码错误");
        }
        return User.withUsername(username)
                .password(account.getPassword())
                .roles(account.getRole())
                .build();
    }

    @Override
    public String registerEmailVerifyCode(String type, String email, String ip) {
        synchronized (ip.intern()) {
            if (!flowUtils.limitOnceCheck(AccountConstants.VERIFY_EMAIL_LIMIT + ip, 60)) {
                return "请求频繁稍后重试";
            }
            int code = new Random().nextInt(899999) + 100000;
            amqpTemplate.convertAndSend("mail", Map.of("type", type, "email", email, "code", code));
            stringRedisTemplate.opsForValue().set(
                    AccountConstants.VERIFY_EMAIL_DATA + email,
                    String.valueOf(code), 3, TimeUnit.MINUTES);
            return null;
        }
    }

    @Override
    @Transactional
    public String registerEmailAccount(EmailRegisterRequest request) {
        String key = AccountConstants.VERIFY_EMAIL_DATA + request.getEmail();
        String code = stringRedisTemplate.opsForValue().get(key);
        if (code == null) return "请先获取验证码";
        if (!code.equals(request.getCode())) return "验证码输入错误，请重新输入";
        if (existsAccountByEmail(request.getEmail())) return "此电子邮件已被其他用户注册";
        if (existsAccountByUsername(request.getUsername())) return "此用户名已被其他人注册，请更新一个新的用户名";
        Account account = new Account(null, request.getUsername(), encoder.encode(request.getPassword()),
                request.getEmail(), "user", new Date(), request.getUsername(), null);
        if (!save(account)) return "内部错误，请联系管理员";
        stringRedisTemplate.delete(key);
        return null;
    }

    @Override
    @Transactional
    public String resetEmailAccountPassword(EmailResetRequest request) {
        String verify = resetConfirm(new ConfirmResetRequest(request.getEmail(), request.getCode()));
        if (verify != null) return verify;
        boolean updated = update().eq("email", request.getEmail())
                .set("password", encoder.encode(request.getPassword())).update();
        if (updated) stringRedisTemplate.delete(AccountConstants.VERIFY_EMAIL_DATA + request.getEmail());
        return updated ? null : "账号不存在";
    }

    @Override
    public String resetConfirm(ConfirmResetRequest request) {
        String code = stringRedisTemplate.opsForValue().get(AccountConstants.VERIFY_EMAIL_DATA + request.getEmail());
        if (code == null) return "请先获取验证码";
        return code.equals(request.getCode()) ? null : "验证码错误，请重新输入";
    }

    @Override
    public Account findAccountByNameOrEmail(String text) {
        return query().eq("username", text).or().eq("email", text).one();
    }

    @Override
    public Optional<Account> findAccountById(Integer id) {
        return Optional.ofNullable(getById(id));
    }

    private boolean existsAccountByEmail(String email) {
        return baseMapper.exists(Wrappers.<Account>query().eq("email", email));
    }

    private boolean existsAccountByUsername(String username) {
        return baseMapper.exists(Wrappers.<Account>query().eq("username", username));
    }
}
