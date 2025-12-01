package id.web.saka.fountation.authorization;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import reactor.core.publisher.Mono;

@Controller
public class AuthorizationController {

    @GetMapping("/")
    public Mono<String> index() {
        return  Mono.just("login");
    }

    @GetMapping("/login")
    public Mono<String> login(Model model) {

        model.addAttribute("pageTitle", "Home Page");

        return Mono.just("login");
    }

}
