package com.lucas.service.utils;

import com.lucas.service.model.entity.Accounts;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.Data;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Log4j2
@Component
@Data
public class JWTUtils {

    @Autowired
    private RSAPrivateKey rsaPrivateKey;

    @Autowired
    private RSAPublicKey rsaPublicKey;

    @Value("${jwt.expire.token}")
    private long expirationTimeToken;

    @Value("${jwt.expire.refreshToken}")
    private long expirationTimeRefreshToken;

    /**
     * @param account
     * @return token
     */
    public String generateAccessToken(Accounts account) {

        Date expirationDate = new Date(System.currentTimeMillis() + expirationTimeToken);

        Map<String, Object> claims = new HashMap<>();
        claims.put("username", account.getUsername());
        claims.put("password", account.getPassword());
        claims.put("userId", account.getId());

        // Tạo JWT token
        return Jwts.builder()
                .setSubject(account.getUsername())
                .setIssuedAt(new Date())
                .setExpiration(expirationDate)
                .setClaims(claims)
                .signWith(SignatureAlgorithm.RS256, rsaPrivateKey)
                .compact();
    }


    /**
     * generateRefreshToken
     *
     * @param account
     * @return refreshToken
     */
    public String generateRefreshToken(Accounts account) {
        Date expirationDate = new Date(System.currentTimeMillis() + expirationTimeRefreshToken);

        // Tạo JWT token
        return Jwts.builder()
                .setSubject(account.getUsername())
                .setIssuedAt(new Date())
                .setExpiration(expirationDate)
                .claim("tokenType", "refresh")
                .signWith(SignatureAlgorithm.RS256, rsaPrivateKey)
                .compact();
    }


    /**
     * validate token jwt
     *
     * @param token
     * @param username
     * @return boolean
     */
    public boolean validateToken(String token, String username) {
        try {
            // Xác thực token bằng khóa công khai
            Claims claims = Jwts.parser()
                    .setSigningKey(rsaPublicKey)
                    .parseClaimsJws(token)
                    .getBody();

            // Kiểm tra username trong token có khớp với username được cung cấp không
            String tokenUsername = (String) claims.get("username");
            return username.equals(tokenUsername);
        } catch (Exception e) {
            log.error("Lỗi khi xác thực token: {}", e.getMessage());
            return false;
        }
    }

}
