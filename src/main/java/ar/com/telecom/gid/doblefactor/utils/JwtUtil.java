package ar.com.telecom.gid.doblefactor.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import ar.com.telecom.gid.doblefactor.web.Usuario;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.security.Key;
import java.util.Date;


@Component
public class JwtUtil {

    private Key key;

    public JwtUtil(@Value("${jwt.secret}") String secret) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes());
    }

    public String createToken(String token, Usuario usuario) {
        return Jwts.builder()
            .setSubject(usuario.getLegajo()) // or whatever identifies the user
            .claim("originalToken", token)
            .claim("cyberArkToken", token)
            .claim("legajo", usuario.getLegajo())
            .claim("nombre",usuario.getNombre())
            .claim("apellido",usuario.getApellido())
            .claim("nombreCompleto",usuario.getNombreCompleto())
            .claim("email", usuario.getEmail())
            .claim("telFijo", usuario.getTelFijo())
            .claim("telMovil", usuario.getTelMovil())
            .claim("perfil", usuario.getPerfil())
            .claim("urlPhoto", usuario.getUrlPhoto())
            .setIssuedAt(new Date())
            .setExpiration(new Date(System.currentTimeMillis() + 3600000)) // 1 hour
            .signWith(key, SignatureAlgorithm.HS256)
            .compact();
    }

    public Claims parseToken(String jwt) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(jwt)
                .getBody();
    }

    public boolean isTokenValid(String jwt) {
        try {
            parseToken(jwt);
            return true;
        } catch (Exception e) {
            return false;
        }
    }


}

