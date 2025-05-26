package com.lucas.service.service;

import com.lucas.service.model.dto.TokenDTO;
import com.lucas.service.model.request.AccountSignupRequest;

public interface AuthService {
    boolean createAccount(AccountSignupRequest accountSignupRequest);

    boolean activeAccount(String username);

    TokenDTO login(AccountSignupRequest request);
    
    boolean validateToken(String token, String username);
}
