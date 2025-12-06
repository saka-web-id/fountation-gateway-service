package id.web.saka.fountation.authorization;

import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import reactor.core.publisher.Mono;


@Controller
public class AuthorizationController {

    @GetMapping("/")
    public Mono<String> index(Model model) {
        return  Mono.just("login");
    }

    @GetMapping("/login")
    public Mono<String> login(Model model) {

        model.addAttribute("pageTitle", "Home Page");

        return Mono.just("login");
    }



    @GetMapping("/token")
    public Mono<String> getTokens(Model model, OAuth2AuthenticationToken authToken, @RegisteredOAuth2AuthorizedClient("auth0") OAuth2AuthorizedClient client) {

        String accessToken = client.getAccessToken().getTokenValue();

        // ID Token from Authentication principal (OidcUser)
        OidcUser oidcUser = (OidcUser) authToken.getPrincipal();
        String idToken = oidcUser.getIdToken().getTokenValue();

        System.out.println("Access TOKEN: " + accessToken);
        System.out.println("ID TOKEN: " + idToken);

        return Mono.just("User Service TOKEN");
    }

}
