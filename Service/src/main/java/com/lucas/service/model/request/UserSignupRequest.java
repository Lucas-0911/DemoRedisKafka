package com.lucas.service.model.request;


import lombok.Data;

@Data
public class UserSignupRequest {
    private String username;
    private String password;
}
