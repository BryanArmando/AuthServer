package com.microservice.authserve.security;

import com.microservice.authserve.dto.RequestDTO;
import com.microservice.authserve.entity.AuthUser;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.crypto.spec.SecretKeySpec;
import java.security.Key;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.JwtParser;

@Component
public class JwtProvider {
    @Value("${jwt-secret-word}")
    private String secret;
    private final Key LlAVE_SECRETA = new SecretKeySpec(secret.getBytes(), SignatureAlgorithm.HS256.getJcaName());
    @Autowired
    private RouteValidator routeValidator;
    public String createToken(AuthUser authUser){
        Map<String, Object> claims = new HashMap<>();
        claims = Jwts.claims().setSubject(authUser.getEmail());
        claims.put("id", authUser.getId());
        claims.put("role", authUser.getRole());
        Date now = new Date();
        Date expired = new Date(now.getTime() + 3600000);
        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(expired)
                .signWith(LlAVE_SECRETA)
                .compact();
    }

    public boolean validate(String token, RequestDTO dto) {
        try {
            Jwts.parserBuilder().setSigningKey(LlAVE_SECRETA).build().parseClaimsJwt(token);
        }catch (Exception e) {
            return false;
        }
        if (!isAdmin(token) && routeValidator.isAdminPath(dto)){
            return false;
        }
        return true;
    }

    public String getEmailFromUser(String token){
        JwtParser parser = Jwts.parserBuilder().setSigningKey(LlAVE_SECRETA).build();
        return parser.parseClaimsJwt(token).getBody().getSubject();
    }

    private boolean isAdmin(String token){
        JwtParser parser = Jwts.parserBuilder().setSigningKey(LlAVE_SECRETA).build();
        return parser.parseClaimsJwt(token).getBody().get("role").equals("admin");
    }

}
