package com.lucas.service.service.impl;

import com.lucas.service.model.dto.TokenDTO;
import com.lucas.service.model.entity.Accounts;
import com.lucas.service.model.request.AccountSignupRequest;
import com.lucas.service.repository.AuthRepository;
import com.lucas.service.service.AuthService;
import com.lucas.service.utils.JWTUtils;
import com.lucas.service.utils.RedisUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
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

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JWTUtils jwtUtils;

    @Override
    public boolean createAccount(AccountSignupRequest accountSignupRequest) {
        try {
            //Todo: Check is exits by username in redis

            // Mã hóa mật khẩu trước khi lưu vào database
            String encodedPassword = passwordEncoder.encode(accountSignupRequest.getPassword());

            Accounts createAccount = Accounts.builder()
                    .username(accountSignupRequest.getUsername())
                    .password(encodedPassword) // Sử dụng mật khẩu đã mã hóa
                    .build();

            createAccount = authRepository.save(createAccount);

            //Set redis
            redisUtils.setObject("ACCOUNT:" + createAccount.getId(), createAccount);

            return true;
        } catch (Exception e) {
            log.error(ExceptionUtils.getStackTrace(e));
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

    /**
     * @param request
     * @return TokenDTO
     */
    @Override
    public TokenDTO login(AccountSignupRequest request) {
        try {
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

            // Trả về response
            return TokenDTO.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .tokenType("Bearer")
                    .expiresIn(jwtUtils.getExpirationTimeToken())
                    .build();
        } catch (Exception e) {
            e.printStackTrace();
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
            return jwtUtils.validateToken(token, username);
        } catch (Exception e) {
            log.error("Lỗi khi xác thực token: {}", e.getMessage());
            return false;
        }
    }
}
