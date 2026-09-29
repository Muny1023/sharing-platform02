package org.example.accountservice.entity.response;

import lombok.Data;

import java.util.Date;

@Data
public class AuthorizeResponse {
    private String username;
    private String roles;
    private String token;
    private Date expire;
}
