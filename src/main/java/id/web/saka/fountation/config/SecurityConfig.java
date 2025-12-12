package id.web.saka.fountation.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import reactor.core.publisher.Mono;

import java.net.URI;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(exchanges -> exchanges
                        .pathMatchers("/api/v0/health").permitAll()
                        .pathMatchers("/api/v0/login").permitAll()
                        .pathMatchers("/api/v0/user/health").permitAll()
                        .pathMatchers("/api/v0/user/registration/**").permitAll()
                        .pathMatchers("/api/v0/oauth2/**").permitAll()
                        .pathMatchers("/api/v0/public/**").permitAll()
                        .anyExchange().authenticated()
                )
                .oauth2Login(
                        oauth2 -> oauth2
                                .loginPage("/oauth2/authorization/auth0")
                                .authenticationSuccessHandler((webFilterExchange, authentication) -> {
                                    return webFilterExchange.getExchange().getResponse()
                                            .setComplete()
                                            .then(Mono.fromRunnable(() -> {
                                                webFilterExchange.getExchange().getResponse()
                                                        .getHeaders()
                                                        .setLocation(URI.create("/user/login"));
                                            }));
                                })
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> {
                            // You can configure JwtDecoder or leave default
                        })
                ) // optional
                .build();
    }


}
