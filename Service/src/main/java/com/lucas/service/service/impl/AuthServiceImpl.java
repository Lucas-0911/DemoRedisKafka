package com.lucas.service.service.impl;

import com.lucas.service.model.entity.Accounts;
import com.lucas.service.model.request.AccountsSignupRequest;
import com.lucas.service.repository.AuthRepository;
import com.lucas.service.service.AuthService;
import com.lucas.service.utils.RedisUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class AuthServiceImpl implements AuthService {

    @Autowired
    private AuthRepository authRepository;

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private RedisUtils redisUtils;

    @Override
    public boolean createAccount(AccountsSignupRequest accountsSignupRequest) {

        try {
            //Todo: Check is exits by username in redis

            Accounts createAccount = Accounts.builder()
                    .username(accountsSignupRequest.getUsername())
                    .password(accountsSignupRequest.getPassword())
                    .build();

            createAccount = authRepository.save(createAccount);

            //Set redis
            redisUtils.setObject("ACCOUNT:" + createAccount.getId(), createAccount);

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.getMessage());
            return false;
        }
    }

    @Override
    public boolean activeAccount(String username) {
        log.info(username);
        Accounts accounts = (authRepository.findByUsername(username).orElse(null));
        if (accounts == null) {
            return false;
        }
        // Send message kafka
        kafkaTemplate.send("ACCOUNTS", "ACCOUNT:" + accounts.getId());
        return true;
    }
}
