package id.web.saka.fountation.util.filter;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Component
public class CorrelationIdFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // 1. Cek apakah sudah ada ID dari luar (opsional), jika tidak buat baru
        String correlationId = exchange.getRequest().getHeaders().getFirst("X-Correlation-ID");
        if (correlationId == null || correlationId.isEmpty()) {
            correlationId = UUID.randomUUID().toString();
        }

        // 2. Masukkan ke dalam Header agar microservice di bawahnya bisa baca
        String finalId = correlationId;
        ServerWebExchange mutatedExchange = exchange.mutate()
                .request(r -> r.header("X-Correlation-ID", finalId))
                .build();

        // 3. Simpan di log Kafka Gateway
        // log ke kafka: "Request masuk dengan ID: " + finalId

        return chain.filter(mutatedExchange);
    }

    @Override
    public int getOrder() {
        return -2; // Harus lebih luar dari Logging (-1) agar ID sudah siap sebelum dicatat
    }

}
