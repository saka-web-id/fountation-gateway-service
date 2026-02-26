package id.web.saka.fountation.config;

import org.springframework.http.HttpMethod;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.config.Customizer;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.RedirectServerAuthenticationSuccessHandler;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .cors(Customizer.withDefaults())
                .authorizeExchange(exchanges -> exchanges
                        .pathMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .pathMatchers(
                                "/api/v0/health",
                                "/api/v0/login",
                                "/api/v0/user/health",
                                "/api/v0/user/registration/**",
                                "/api/v0/oauth2/**",
                                "/api/v0/public/**"
                        ).permitAll()
                        /*.pathMatchers("/login/oauth2/**").permitAll()
                        .pathMatchers("/api/v0/user/detail").permitAll()*/
                        .anyExchange().authenticated()
                )
                .oauth2Login(
                        oauth2 -> oauth2
                                .authenticationSuccessHandler(new RedirectServerAuthenticationSuccessHandler("https://192.168.1.51/dashboard"))
                                /*.loginPage("/oauth2/authorization/auth0")
                                .authenticationSuccessHandler((webFilterExchange, authentication) -> {
                                    webFilterExchange.getExchange().getResponse()
                                            .setStatusCode(HttpStatus.FOUND);
                                    webFilterExchange.getExchange().getResponse()
                                            .getHeaders()
                                            .setLocation(URI.create("/api/v0/user/login"));
                                    return webFilterExchange.getExchange().getResponse().setComplete();
                                })*/
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> {
                            // You can configure JwtDecoder or leave default
                        })
                ) // optional
                .build();
    }


}
