package id.web.saka.fountation.config;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import io.netty.resolver.DefaultAddressResolverGroup;
import org.springframework.boot.web.reactive.function.client.WebClientCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;

import java.time.Duration;

@Configuration
public class WebClientConfig {

    @Bean
    public HttpClient httpClient() {
        ConnectionProvider provider = ConnectionProvider.builder("fountation-gateway-pool")
                .maxIdleTime(Duration.ofSeconds(60)) // Increased from 20s
                .maxLifeTime(Duration.ofMinutes(5))  // Increased from 1m to match Gateway
                .evictInBackground(Duration.ofSeconds(30))
                .metrics(true) // Useful for Prometheus later!
                .build();

        return HttpClient.create(provider)
                //.protocol(reactor.netty.http.HttpProtocol.HTTP11) // TODO Testing in Home ISP only. remove when in production.
                .resolver(DefaultAddressResolverGroup.INSTANCE)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 30000) // 10s connect timeout
                .responseTimeout(Duration.ofMinutes(2))
                .doOnConnected(conn ->
                        conn.addHandlerLast(new ReadTimeoutHandler(10))
                                .addHandlerLast(new WriteTimeoutHandler(10)));
    }

    @Bean
    public WebClientCustomizer webClientCustomizer(HttpClient httpClient) {
        return webClientBuilder -> webClientBuilder.clientConnector(new ReactorClientHttpConnector(httpClient));
    }
}
