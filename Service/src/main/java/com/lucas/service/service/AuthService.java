package com.lucas.service.service;

import com.lucas.service.model.dto.TokenDTO;
import com.lucas.service.model.request.AccountSignupRequest;

import java.util.concurrent.ExecutionException;

public interface AuthService {
    boolean createAccount(AccountSignupRequest accountSignupRequest);

    boolean activeAccount(String username) throws ExecutionException, InterruptedException;

    TokenDTO login(AccountSignupRequest request);
    
    boolean validateToken(String token, String username);
}
