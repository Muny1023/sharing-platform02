package org.example.accountservice.controller;

import org.example.accountservice.entity.Account;
import org.example.accountservice.entity.response.AccountResponse;
import org.example.accountservice.service.AccountService;
import org.example.cloudcommon.RestBean;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountControllerTest {
    @Mock
    private AccountService accountService;

    @InjectMocks
    private AccountController controller;

    @InjectMocks
    private InternalAccountController internalController;

    @Test
    void meReturnsPublicAccountProjection() {
        when(accountService.findAccountById(1)).thenReturn(Optional.of(new Account(1, "alice",
                "encoded-password", "alice@mail.com", "user", new Date(), "爱丽丝", "avatar.png")));

        RestBean<AccountResponse> response = controller.me(1);

        assertEquals(200, response.code());
        assertEquals("alice", response.data().username());
        assertEquals("爱丽丝", response.data().nickname());
        assertFalse(java.util.Arrays.stream(response.data().getClass().getRecordComponents())
                .anyMatch(component -> component.getName().equals("password")));
    }

    @Test
    void meReturns404WhenAccountDoesNotExist() {
        when(accountService.findAccountById(404)).thenReturn(Optional.empty());

        RestBean<AccountResponse> response = controller.me(404);

        assertEquals(404, response.code());
        assertEquals("账号不存在", response.message());
    }

    @Test
    void batchReturnsOnlyPublicAccountFields() {
        when(accountService.listByIds(List.of(1, 2))).thenReturn(List.of(
                new Account(1, "alice", "secret", "a@mail.com", "user", new Date(), "爱丽丝", "a.png"),
                new Account(2, "bob", "secret", "b@mail.com", "user", new Date(), "鲍勃", "b.png")));

        java.util.List<org.example.cloudcommon.AccountSummary> result = internalController.batch(java.util.List.of(1, 2));

        assertEquals(2, result.size());
        assertEquals("爱丽丝", result.get(0).nickname());
        assertEquals("a.png", result.get(0).avatar());
    }
}
