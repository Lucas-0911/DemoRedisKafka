package com.lucas.service.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lucas.service.model.entity.User;
import com.lucas.service.model.request.UserSignupRequest;
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
    private KafkaTemplate<String, Long> kafkaTemplate;

    @Autowired
    private RedisUtils redisUtils;

    @Override
    public boolean createAccount(UserSignupRequest userSignupRequest) {

        try {
            User user = User.builder().username(userSignupRequest.getUsername()).password(userSignupRequest.getPassword()).build();
            user = authRepository.save(user);
            // Success
            // Set cache : userSignupRequest.getUsername();
            ObjectMapper mapper = new ObjectMapper();
            String a = mapper.writeValueAsString(user);
            redisUtils.setObject("user:" + user.getId(), user);
            // Thread.sleep(5 * 1000);
            // Send message kafka
            kafkaTemplate.send("USER", user.getId());
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.getMessage());
            return false;
        }
    }
//
//    @Override
//    public boolean createAccount(UserSignupRequest userSignupRequest) {
//        return false;
//    }
}
