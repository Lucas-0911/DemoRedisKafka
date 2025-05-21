package com.lucas.service.controller;

import com.lucas.service.model.dto.ResponseDTO;
import com.lucas.service.model.dto.TokenDTO;
import com.lucas.service.model.entity.Accounts;
import com.lucas.service.model.request.AccountSignupRequest;
import com.lucas.service.repository.AuthRepository;
import com.lucas.service.service.AuthService;
import com.lucas.service.utils.ResponseUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private AuthRepository authRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("/signup")
    public ResponseEntity<ResponseDTO<Boolean>> signup(@RequestBody AccountSignupRequest request, HttpServletRequest httpRequest) {
        boolean result = authService.createAccount(request);
        if (result) {
            return ResponseUtils.success(true, "Đăng ký tài khoản thành công", httpRequest);
        } else {
            return ResponseUtils.badRequest("Đăng ký tài khoản thất bại", httpRequest);
        }
    }

    @GetMapping("/active/{username}")
    public ResponseEntity<ResponseDTO<Boolean>> activeAccount(@PathVariable String username, HttpServletRequest request) {
        boolean result = authService.activeAccount(username);
        if (result) {
            return ResponseUtils.success(true, "Kích hoạt tài khoản thành công", request);
        } else {
            return ResponseUtils.notFound("Không tìm thấy tài khoản", request);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<ResponseDTO<TokenDTO>> login(@RequestBody AccountSignupRequest request, HttpServletRequest httpRequest) {
        // Kiểm tra username và password
        Accounts account = authRepository.findByUsername(request.getUsername()).orElse(null);

        if (account == null || !passwordEncoder.matches(request.getPassword(), account.getPassword())) {
            return ResponseUtils.badRequest("Tên đăng nhập hoặc mật khẩu không đúng", httpRequest);
        }

        TokenDTO tokenDTO = authService.login(request.getUsername());

        if (tokenDTO != null) {
            return ResponseUtils.success(tokenDTO, "Đăng nhập thành công", httpRequest);
        } else {
            return ResponseUtils.badRequest("Đăng nhập thất bại", httpRequest);
        }
    }
}
