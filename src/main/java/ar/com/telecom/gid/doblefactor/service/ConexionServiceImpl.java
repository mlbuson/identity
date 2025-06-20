package ar.com.telecom.gid.doblefactor.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jwt.SignedJWT;
import ar.com.telecom.gid.doblefactor.web.Usuario;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import java.io.IOException;
import java.util.*;

@Service
public class ConexionServiceImpl implements ConexionService {

    @Value("${spring.security.oauth2.client.registration.teco.client-id}")
    String clientId;

    @Value("${spring.security.oauth2.client.registration.teco.client-secret}")
    String clientSecret;

    @Value("${spring.security.oauth2.client.provider.teco.authorization-uri}")
    String baseUrl;

    @Value("${spring.security.oauth2.client.registration.teco.redirect-uri}")
    String redirectUri;

    @Value("${spring.security.oauth2.client.provider.teco.token-uri}")
    String tokenUri;

    @Value("${spring.security.oauth2.client.provider.teco.end-session-uri}")
    String endOIDCSessionUri;

    @Value("${spring.security.oauth2.client.provider.teco.customer-id}")
    String customerId;

    Logger log = LoggerFactory.getLogger(ConexionServiceImpl.class);

    @Override
    public String getUrlConexion(){
        return baseUrl + "?client_id=" + clientId + "&scope=openid+email+profile&response_type=code&redirect_uri="
                + redirectUri + "&customerId=" + customerId + "&prompt=login";
    }

    @Override
    public String getToken(String authorizationCode) throws IOException{
        String token = "";
        byte[] decodedBytes = Base64.getDecoder().decode(clientSecret.getBytes());
        String secret = new String(decodedBytes);
        ObjectMapper mapper = new ObjectMapper();
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "code");
        body.add("code", authorizationCode);
        body.add("redirect_uri", redirectUri);
        body.add("client_id", clientId);
        body.add("client_secret", secret);
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(tokenUri, request, String.class);
        if (response.getStatusCode() == HttpStatus.OK) {
            JsonNode arrayNode = mapper.readTree(response.getBody());
            token = arrayNode.get("id_token").asText();
        } else {
            System.out.println("Error al obtener el token: " + response.getStatusCode());
        }
        return token;
    }

    @Override
    public Map<String,Object> getClaims(String token) {
        Map<String,Object> claims = new HashMap<>();
        try{
            SignedJWT signedJWT = SignedJWT.parse(token);
            claims = signedJWT.getJWTClaimsSet().getClaims();
        } catch(Exception e){
            System.out.println(e.getMessage());
        }
        return claims;
    }

    @Override
    public Usuario getUsuario(Map<String, Object> claims) throws IOException {
        Usuario usuario = new Usuario();
        ObjectMapper mapper = new ObjectMapper();
        usuario.setLegajo(claims.get("User") != null ? claims.get("User").toString() : "");
        usuario.setNombre(claims.get("given_name") != null ? claims.get("given_name").toString() : "");
        usuario.setApellido(claims.get("family_name") != null ? claims.get("family_name").toString() : "");
        usuario.setNombreCompleto(claims.get("DisplayName") != null ? claims.get("DisplayName").toString() : "");
        usuario.setEmail(claims.get("email") != null ? claims.get("email").toString() : "");
        usuario.setTelFijo(claims.get("telFijo") != null ? claims.get("telFijo").toString() : "");
        usuario.setTelMovil(claims.get("telMovil") != null ? claims.get("telMovil").toString() : "");
        usuario.setUrlPhoto(claims.get("picture") != null ? claims.get("picture").toString() : "");
        Object perfList = claims.get("groups");
        usuario.setPerfil(perfList != null ? (List<String>) perfList : new ArrayList<>());
        return usuario;
    }

    @Override
    public void cerrarSesionCyberArk(String token){
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json");
        headers.add("Accept", "*/*");
        headers.add("Authorization", "Bearer " + token);
        HttpEntity<?> entity = new HttpEntity<>(headers);
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(endOIDCSessionUri)
                .queryParam("id_token_hint", token);
        restTemplate.postForEntity(builder.toUriString(), entity, String.class);
    }

}
