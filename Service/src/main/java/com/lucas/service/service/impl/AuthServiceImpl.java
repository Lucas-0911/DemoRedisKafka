package com.lucas.service.service.impl;

import com.lucas.common.redis.RedisUtils;
import com.lucas.service.model.dto.TokenDTO;
import com.lucas.service.model.entity.Accounts;
import com.lucas.service.model.request.AccountSignupRequest;
import com.lucas.service.repository.AuthRepository;
import com.lucas.service.service.AuthService;
import com.lucas.service.utils.JWTUtils;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Log4j2
@Service
public class AuthServiceImpl implements AuthService {


    @Autowired
    private AuthRepository authRepository;

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

//    @Autowired
//    private RedisUtils redisUtils;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JWTUtils jwtUtils;

    private final RedisUtils redisUtils = new RedisUtils();

    @Override
    public boolean createAccount(AccountSignupRequest accountSignupRequest) {
        try {
            log.info("Create account : {}", accountSignupRequest);
            //Todo: Check is exits by username in redis

            // Mã hóa mật khẩu trước khi lưu vào database
            String encodedPassword = passwordEncoder.encode(accountSignupRequest.getPassword());

            Accounts createAccount = Accounts.builder()
                    .username(accountSignupRequest.getUsername())
                    .password(encodedPassword) // Sử dụng mật khẩu đã mã hóa
                    .build();

            createAccount = authRepository.save(createAccount);

            log.info("Create account success : {}", createAccount);
            //Set redis
            redisUtils.setObject("ACCOUNT:" + createAccount.getId(), createAccount);
            return true;
        } catch (Exception e) {
            log.error("Loi khi tao tai khoan: {}", ExceptionUtils.getStackTrace(e));
            return false;
        }
    }

    @Override
    public boolean activeAccount(String username) {
        log.info("Activating account: {}", username);
        return authRepository.findByUsername(username)
                .map(account -> {
                    kafkaTemplate.send("ACCOUNTS", "ACCOUNT:" + account.getId());
                    log.info("Activate account success : {}", account);
                    return true;
                })
                .orElseGet(() -> {
                    log.info("Activating account failed: {}", username);
                    return false;
                });
    }


    /**
     * @param request
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
                log.error("Tài khoản chưa được kích hoạt: {}", request.getUsername());
                return null;
            }

            // Tạo token và refresh token
            String accessToken = jwtUtils.generateAccessToken(account);
            String refreshToken = jwtUtils.generateRefreshToken(account);

            // Lưu refresh token vào Redis để quản lý
            String redisKey = "REFRESH_TOKEN:" + account.getUsername();
            redisUtils.setObject(redisKey, refreshToken, jwtUtils.getExpirationTimeToken());

            TokenDTO tokenDTO = TokenDTO.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .tokenType("Bearer")
                    .expiresIn(jwtUtils.getExpirationTimeToken())
                    .build();

            log.info("Login account success : {}", tokenDTO);
            // Trả về response
            return tokenDTO;
        } catch (Exception e) {
            log.error("Loi khi dang nhap: {}", ExceptionUtils.getStackTrace(e));
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
            log.info("Validate token: {}, username : {}", token, username);
            return jwtUtils.validateToken(token, username);
        } catch (Exception e) {
            log.error("Lỗi khi xác thực token: {}", ExceptionUtils.getStackTrace(e));
            return false;
        }
    }
}
