package com.lucas.service.service;

import com.lucas.service.model.request.AccountsSignupRequest;

public interface AuthService {
    boolean createAccount(AccountsSignupRequest accountsSignupRequest);

    boolean activeAccount(String username);
}
