package id.web.saka.fountation.config;

import org.springframework.http.HttpMethod;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.config.Customizer;
import org.springframework.security.oauth2.client.authentication.OAuth2LoginReactiveAuthenticationManager;
import org.springframework.security.oauth2.client.endpoint.OAuth2AuthorizationCodeGrantRequest;
import org.springframework.security.oauth2.client.endpoint.ReactiveOAuth2AccessTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.WebClientReactiveAuthorizationCodeTokenResponseClient;
import org.springframework.security.oauth2.client.userinfo.DefaultReactiveOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.ReactiveOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.RedirectServerAuthenticationSuccessHandler;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.util.retry.Retry;

import java.time.Duration;

import org.springframework.security.oauth2.client.oidc.userinfo.OidcReactiveOAuth2UserService;
import org.springframework.security.oauth2.client.oidc.authentication.OidcAuthorizationCodeReactiveAuthenticationManager;
import org.springframework.security.authentication.DelegatingReactiveAuthenticationManager;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Bean
    public ReactiveOAuth2AccessTokenResponseClient<OAuth2AuthorizationCodeGrantRequest> accessTokenResponseClient(WebClient.Builder builder) {
        WebClientReactiveAuthorizationCodeTokenResponseClient client = new WebClientReactiveAuthorizationCodeTokenResponseClient();
        client.setWebClient(builder
                .clone()
                .filter((request, next) -> next.exchange(request)
                        .retryWhen(Retry.backoff(3, Duration.ofSeconds(2))
                                .filter(throwable -> throwable instanceof java.io.IOException)))
                .build());
        return client;
    }

    @Bean
    public ReactiveOAuth2UserService<OAuth2UserRequest, OAuth2User> oauth2UserService(WebClient.Builder builder) {
        DefaultReactiveOAuth2UserService userService = new DefaultReactiveOAuth2UserService();
        userService.setWebClient(builder
                .clone()
                .filter((request, next) -> next.exchange(request)
                        .retryWhen(Retry.backoff(3, Duration.ofSeconds(2))
                                .filter(throwable -> throwable instanceof java.io.IOException)))
                .build());
        return userService;
    }

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http,
                                                       ReactiveOAuth2AccessTokenResponseClient<OAuth2AuthorizationCodeGrantRequest> accessTokenResponseClient,
                                                       ReactiveOAuth2UserService<OAuth2UserRequest, OAuth2User> oauth2UserService) {

        OAuth2LoginReactiveAuthenticationManager oauth2Manager =
                new OAuth2LoginReactiveAuthenticationManager(accessTokenResponseClient, oauth2UserService);

        // 2. Create the OIDC Manager
        OidcReactiveOAuth2UserService oidcUserService = new OidcReactiveOAuth2UserService();
        oidcUserService.setOauth2UserService(oauth2UserService); // CRITICAL: Use the custom user service
        
        OidcAuthorizationCodeReactiveAuthenticationManager oidcManager = 
                new OidcAuthorizationCodeReactiveAuthenticationManager(accessTokenResponseClient, oidcUserService);

        ReactiveAuthenticationManager authenticationManager =
                new DelegatingReactiveAuthenticationManager(oauth2Manager, oidcManager);

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
                                .authenticationManager(authenticationManager)
                                .authenticationSuccessHandler(new RedirectServerAuthenticationSuccessHandler("https://192.168.1.51/dashboard"))
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(Customizer.withDefaults())
                )
                .build();
    }


}


