package com.lucas.service.model.request;


import lombok.Data;

@Data
public class AccountSignupRequest {
    private String username;
    private String password;
}
