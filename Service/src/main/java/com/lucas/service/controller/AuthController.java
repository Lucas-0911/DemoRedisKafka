package com.lucas.service.controller;

import com.lucas.service.model.request.UserSignupRequest;
import com.lucas.service.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/registration")
    public String createAccount(@RequestBody UserSignupRequest userSignupRequest) {
        String message = "Create account failed";
        boolean isCreate = authService.createAccount(userSignupRequest);

        if (isCreate) {
            message = "Create account successful";
        }
        return message;
    }

}
