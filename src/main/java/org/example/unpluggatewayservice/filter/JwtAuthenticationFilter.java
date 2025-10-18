package org.example.unpluggatewayservice.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

@Component
public class JwtAuthenticationFilter implements GlobalFilter {
    private final SecretKey key;

    public JwtAuthenticationFilter(@Value("${jwt.secret}") String secret){
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain){
        String path = exchange.getRequest().getPath().value();

        // 추가한 부분 (인증이 필요 없는 경로 목록)
        if (isPublicPath(path)) {
            return chain.filter(exchange);
        }// 여기까지 ...

        // API 요청 허용
        if (path.startsWith("/user/signup") || path.startsWith("/user/login") || path.startsWith("/user/refresh") || path.startsWith("/user/check/**") || path.startsWith("/user/logout")){
            return chain.filter(exchange);
        }

        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")){
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        String token = authHeader.substring(7);
        try {
            Claims claims = Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
            String username = claims.getSubject();

            var request = exchange.getRequest().mutate()
                    .header("X-Auth-Username", username)
                    .build();

            var mutatedExchange = exchange.mutate().request(request).build();
            return chain.filter(mutatedExchange);
        } catch (Exception e){
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }
    }

    // 추가한 부분 (WebSocket 포함한 공개 경로들)
    private boolean isPublicPath(String path) {
        return path.startsWith("/ws");   // WebSocket handshake 예외 추가
    }// 여기까지 ...
}
