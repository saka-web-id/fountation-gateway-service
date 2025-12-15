package id.web.saka.fountation.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.ResponseCookie;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@Configuration
public class CookieConfig  {

    @Bean
    public WebFilter sameSiteCookieFilter() {
        return (ServerWebExchange exchange, WebFilterChain chain) ->
                chain.filter(exchange).then(Mono.fromRunnable(() -> {

                    var response = exchange.getResponse();

                    response.getCookies().forEach((name, cookies) -> {
                        cookies.forEach(cookie -> {
                            ResponseCookie modified = ResponseCookie.from(name, cookie.getValue())
                                    .domain(cookie.getDomain())
                                    .path(cookie.getPath())
                                    .maxAge(cookie.getMaxAge())
                                    .httpOnly(cookie.isHttpOnly())
                                    .secure(cookie.isSecure())
                                    .sameSite("None")   // ✅ WebFlux-compatible SameSite
                                    .build();

                            response.addCookie(modified);
                        });
                    });
                }));
    }

}