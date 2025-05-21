package com.lucas.service.controller;

import com.lucas.service.model.request.AccountsSignupRequest;
import com.lucas.service.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/registration")
    public String createAccount(@RequestBody AccountsSignupRequest accountsSignupRequest) {
        String message = "Create account failed";
        boolean isCreate = authService.createAccount(accountsSignupRequest);

        if (isCreate) {
            message = "Create account successful";
        }
        return message;
    }

    @GetMapping("/registration/active")
    public String createAccount(@RequestParam("username") String username) {
        String message = "Active account failed";

        boolean isActive = authService.activeAccount(username);
        if (isActive) {
            message = "Active account successful";
        }
        return message;
    }
}
