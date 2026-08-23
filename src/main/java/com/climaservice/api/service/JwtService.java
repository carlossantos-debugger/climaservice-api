package com.climaservice.api.service;

import com.climaservice.api.entity.Usuario;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;

@Service
public class JwtService {

    private final String secret;
    private final long expiration;

    public JwtService(@Value("${jwt.secret}") String secret, @Value("${jwt.expiration}") long expiration) {

        this.secret = secret;
        this.expiration = expiration;
    }

    public String gerarToken(Usuario usuario) {

        Instant agora = Instant.now();

        Instant expiracao = agora.plusMillis(expiration);

        return Jwts.builder().subject(usuario.getEmail()).claim("role", usuario.getRole().name()).issuedAt(Date.from(agora)).expiration(Date.from(expiracao)).signWith(getChave()).compact();
    }

    private SecretKey getChave() {

        byte[] bytes = Decoders.BASE64.decode(secret);

        return Keys.hmacShaKeyFor(bytes);
    }
}