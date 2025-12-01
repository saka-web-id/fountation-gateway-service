package id.web.saka.fountation.authorization;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebFluxSecurity
public class AuthorizationConfig {

    @Bean
    SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        // @formatter:off
        http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange((authorize) -> authorize
                        .pathMatchers("/").permitAll()
                        .pathMatchers("/login").permitAll()
                        .anyExchange().authenticated()
                )
                .oauth2Login(oauth -> oauth
                        .loginPage("/oauth2/authorization/auth0")
                )
                .oauth2ResourceServer(oauth2ResourceServer -> oauth2ResourceServer.jwt(withDefaults()));;
                /*.httpBasic(withDefaults())
                .formLogin((form) -> form
                        .loginPage("/login")
                );*/
        // @formatter:on
        return http.build();
    }

}
