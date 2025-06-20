package ar.com.telecom.gid.doblefactor.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityFilter {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(authorize -> authorize
                    .requestMatchers("/authorizeOauth", "/oauth2/**", "/logoutCyberArk", "/jwt**","/error/**").permitAll()
                    .anyRequest().authenticated()
            );
        return http.build();
    }

}
