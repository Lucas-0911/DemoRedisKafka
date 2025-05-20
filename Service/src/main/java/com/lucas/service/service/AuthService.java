package com.lucas.service.service;

import com.lucas.service.model.request.UserSignupRequest;

public interface AuthService {
    boolean createAccount(UserSignupRequest userSignupRequest);
}
