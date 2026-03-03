package id.web.saka.fountation.util.filter;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

@Component
public class KafkaLoggingFilter implements GlobalFilter, Ordered {

    Logger logger = Logger.getLogger(KafkaLoggingFilter.class.getName());

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public KafkaLoggingFilter(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        logger.info("KafkaLoggingFilter : " + exchange.getRequest().getURI().toString());

        long startTime = System.currentTimeMillis();

        return chain.filter(exchange).then(Mono.fromRunnable(() -> {
            long duration = System.currentTimeMillis() - startTime;

            String correlationId = exchange.getRequest().getHeaders().getFirst("X-Correlation-ID");

            // Build the log entry
            Map<String, Object> logEntry = new HashMap<>();
            logEntry.put("logGatewayCorrelationId", correlationId != null ? correlationId : "NOT_FOUND");
            logEntry.put("logGatewayMethod", exchange.getRequest().getMethod().name());
            logEntry.put("logGatewayEndPoint", exchange.getRequest().getPath().value());
            logEntry.put("logGatewayStatusCode", exchange.getResponse().getStatusCode().value());
            logEntry.put("logGatewayExecutionTime", duration);
            logEntry.put("logGatewayClientIp", exchange.getRequest().getRemoteAddress().getHostString());
            logEntry.put("logGatewayCreatedAt", Instant.now().toEpochMilli());

            // Send to Kafka "gateway-logs" topic
            kafkaTemplate.send("logs-gateway", logEntry);
        }));
    }

    @Override
    public int getOrder() {
        // beri angka -1 agar filter ini jalan di depan
        // sebelum filter lain memproses request-nya.
        // mencatat jika ada attach sebelum authentification
        return -1;
    }
}
