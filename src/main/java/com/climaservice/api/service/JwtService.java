package com.climaservice.api.service;

import com.climaservice.api.entity.Usuario;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.climaservice.api.entity.Usuario;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

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

    public String extrairEmail(String token) {

        return extrairClaims(token).getSubject();
    }

    public boolean tokenValido(String token, Usuario usuario) {

        String email = extrairEmail(token);

        return email.equalsIgnoreCase(usuario.getEmail()) && !tokenExpirado(token);
    }

    private boolean tokenExpirado(String token) {

        Date expiracao = extrairClaims(token).getExpiration();

        return expiracao.before(new Date());
    }

    private Claims extrairClaims(String token) {

        return Jwts.parser().verifyWith(getChave()).build().parseSignedClaims(token).getPayload();
    }
}