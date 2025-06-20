package ar.com.telecom.gid.doblefactor.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.InMemoryOAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.endpoint.OAuth2AccessTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.OAuth2AuthorizationCodeGrantRequest;
import org.springframework.security.oauth2.client.endpoint.RestClientAuthorizationCodeTokenResponseClient;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.client.web.HttpSessionOAuth2AuthorizationRequestRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import java.util.Base64;


@Configuration
public class OauthConfig {

    @Value("${spring.security.oauth2.client.registration.teco.client-id}")
    String clientId;
    @Value("${spring.security.oauth2.client.registration.teco.client-secret}")
    String clientSecret;
    @Value("${spring.security.oauth2.client.registration.teco.redirect-uri}")
    String redirectUri;
    @Value("${spring.security.oauth2.client.provider.teco.authorization-uri}")
    String authorizationUri;
    @Value("${spring.security.oauth2.client.provider.teco.token-uri}")
    String tokenUri;
    @Value("${spring.security.oauth2.client.provider.teco.user-info-uri}")
    String userInfoUri;
    @Value("${spring.security.oauth2.client.provider.teco.jwk-set-uri}")
    String jwkSetUri;

    @Bean
    public AuthorizationRequestRepository<OAuth2AuthorizationRequest> authorizationRequestRepository() {
        return new HttpSessionOAuth2AuthorizationRequestRepository();
    }

    @Bean
    public OAuth2AccessTokenResponseClient<OAuth2AuthorizationCodeGrantRequest> accessTokenResponseClient() {
        return new RestClientAuthorizationCodeTokenResponseClient();
    }

    @Bean
    public ClientRegistrationRepository clientRegistrationRepository() {
        ClientRegistration registration = this.getRegistration();
        return new InMemoryClientRegistrationRepository(registration);
    }

    @Bean
    public OAuth2AuthorizedClientService authorizedClientService() {
        return new InMemoryOAuth2AuthorizedClientService(clientRegistrationRepository());
    }

    private ClientRegistration getRegistration() {
        String secret = "";
        if(clientSecret != null) {
            byte[] decodedBytes = Base64.getDecoder().decode(clientSecret.getBytes());
            secret = new String(decodedBytes);
        }
        return ClientRegistration.withRegistrationId("teco")
                .clientId(clientId)
                .clientSecret(secret)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .redirectUri(redirectUri)
                .scope("openid", "profile", "email")
                .authorizationUri(authorizationUri)
                .tokenUri(tokenUri)
                .userInfoUri(userInfoUri)
                .jwkSetUri(jwkSetUri)
                .clientName("teco")
                .build();
    }
}