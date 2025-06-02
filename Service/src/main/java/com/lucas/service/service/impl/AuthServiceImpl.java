package com.lucas.service.service.impl;

import com.lucas.common.redis.RedisUtils;
import com.lucas.configservice.config.SynchronousKafkaProperties;
import com.lucas.configservice.dto.DispatchRequest;
import com.lucas.configservice.dto.DispatchResponse;
import com.lucas.service.model.dto.TokenDTO;
import com.lucas.service.model.entity.Accounts;
import com.lucas.service.model.request.AccountSignupRequest;
import com.lucas.service.repository.AuthRepository;
import com.lucas.service.service.AuthService;
import com.lucas.service.utils.JWTUtils;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.kafka.requestreply.ReplyingKafkaTemplate;
import org.springframework.kafka.requestreply.RequestReplyFuture;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.concurrent.ExecutionException;

@Log4j2
@Service
@EnableConfigurationProperties(SynchronousKafkaProperties.class)
public class AuthServiceImpl implements AuthService {

    private final SynchronousKafkaProperties synchronousKafkaProperties;
    private final ReplyingKafkaTemplate<String, DispatchRequest, DispatchResponse> replyingKafkaTemplate;

    public AuthServiceImpl(SynchronousKafkaProperties synchronousKafkaProperties, ReplyingKafkaTemplate<String, DispatchRequest, DispatchResponse> replyingKafkaTemplate) {
        this.synchronousKafkaProperties = synchronousKafkaProperties;
        this.replyingKafkaTemplate = replyingKafkaTemplate;
    }

    @Autowired
    private AuthRepository authRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JWTUtils jwtUtils;

    @Override
    public boolean createAccount(AccountSignupRequest accountSignupRequest) {
        try {
            log.info("Create account : {}", accountSignupRequest.getUsername());
            Accounts accounts = RedisUtils.getObject(genAccountKey(accountSignupRequest.getUsername()), Accounts.class);
            if (accounts != null) {
                log.error("Create account failed. Account {} already exists.", accountSignupRequest.getUsername());
                return false;
            }

            String encodedPassword = passwordEncoder.encode(accountSignupRequest.getPassword());

            Accounts createAccount = Accounts.builder()
                    .username(accountSignupRequest.getUsername())
                    .password(encodedPassword) // Sử dụng mật khẩu đã mã hóa
                    .build();

            createAccount = authRepository.save(createAccount);

            log.info("Create account success : {}", createAccount.getUsername());

            //Set redis
            RedisUtils.setObject(genAccountKey(createAccount.getUsername()), createAccount);
            return true;
        } catch (Exception e) {
            log.error("Error create account: {}", ExceptionUtils.getStackTrace(e));
            return false;
        }
    }

    @Override
    public boolean activeAccount(String username) throws ExecutionException, InterruptedException {
        log.info("Activating account: {}", username);

        Optional<Accounts> account = authRepository.findByUsername(username);
        if (account.isPresent()) {
            Accounts activeAccount = account.get();
            String requestTopic = synchronousKafkaProperties.getRequestTopic();
            DispatchRequest request = new DispatchRequest(genAccountKey(activeAccount.getUsername()));
            ProducerRecord<String, DispatchRequest> producerRecord = new ProducerRecord<>(requestTopic, request);
            RequestReplyFuture<String, DispatchRequest, DispatchResponse> requestReplyFuture = replyingKafkaTemplate.sendAndReceive(producerRecord);

            DispatchResponse response = requestReplyFuture.get().value();
            log.info(response);
            log.info("Activate account success : {}", username);
            return true;
        } else {
            log.info("Account is not exits : {}", username);
            return false;
        }

    }

    /**
     * @param request AccountSignupRequest
     * @return TokenDTO
     */
    @Override
    public TokenDTO login(AccountSignupRequest request) {
        try {
            log.info("Login account : {}", request);
            // Kiểm tra username và password
            Accounts account = authRepository.findByUsername(request.getUsername()).orElse(null);

            if (account == null || !passwordEncoder.matches(request.getPassword(), account.getPassword())) {
                return null;
            }

            // Kiểm tra trạng thái tài khoản
            if (account.getStatus() == Accounts.Status.CREATE) {
                log.error("Account is not active: {}", request.getUsername());
                return null;
            }

            // Tạo token và refresh token
            String accessToken = jwtUtils.generateAccessToken(account);
            String refreshToken = jwtUtils.generateRefreshToken(account);

            // Lưu refresh token vào Redis để quản lý
            String redisKey = "REFRESH_TOKEN:" + account.getUsername();
            RedisUtils.setObject(redisKey, refreshToken, jwtUtils.getExpirationTimeRefreshToken());

            TokenDTO tokenDTO = TokenDTO.builder().accessToken(accessToken).refreshToken(refreshToken).tokenType("Bearer").expiresIn(jwtUtils.getExpirationTimeToken()).build();

            log.info("Login account success : {}", request.getUsername());

            return tokenDTO;
        } catch (Exception e) {
            log.error("Login account fail : {}", ExceptionUtils.getStackTrace(e));
            return null;
        }
    }

    /**
     * validate token jwt
     *
     * @param token
     * @param username
     * @return boolean
     */
    @Override
    public boolean validateToken(String token, String username) {
        try {
            log.info("Validate username : {}", username);
            return jwtUtils.validateToken(token, username);
        } catch (Exception e) {
            log.error("Error validate token: {}", ExceptionUtils.getStackTrace(e));
            return false;
        }
    }

    private String genAccountKey(String username) {
        return "ACCOUNT:" + username;
    }
}
