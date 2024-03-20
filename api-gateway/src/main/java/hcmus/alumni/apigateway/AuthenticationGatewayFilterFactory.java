package hcmus.alumni.apigateway;

import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import hcmus.alumni.apigateway.filter.RouteValidator;
import hcmus.alumni.apigateway.utils.JwtUtils;

@Component
public class AuthenticationGatewayFilterFactory extends AbstractGatewayFilterFactory<AuthenticationGatewayFilterFactory.Config> {

	@Autowired
	private RouteValidator routeValidator;

	@Autowired
	private JwtUtils jwtUtils;

	public AuthenticationGatewayFilterFactory() {
		super(Config.class);
	}

	public static class Config {}

	@Override
	public GatewayFilter apply(Config config) {
		return ((exchange, chain) -> {
			ServerHttpRequest req = exchange.getRequest();
            if (routeValidator.isSecured.test(req)) {
                //header contains token or not
                if (!req.getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
                	return this.onError(exchange, HttpStatus.UNAUTHORIZED);
                }

                String authHeader = exchange.getRequest().getHeaders().get(HttpHeaders.AUTHORIZATION).get(0);
                if (authHeader != null && authHeader.startsWith("Bearer ")) {
                    authHeader = authHeader.substring(7);
                }
                try {
                    jwtUtils.validateToken(authHeader);
                    req = updateRequest(exchange, authHeader);
                } catch (Exception e) {
                    return this.onError(exchange, HttpStatus.FORBIDDEN);
                }
                
            }
            return chain.filter(exchange.mutate().request(req).build());
		});
	}

	private Mono<Void> onError(ServerWebExchange exchange, HttpStatus httpStatus) {
		ServerHttpResponse response = exchange.getResponse();
		response.setStatusCode(httpStatus);
		return response.setComplete();
	}

	private ServerHttpRequest updateRequest(ServerWebExchange exchange, String token) {
		Claims claims = jwtUtils.getAllClaimsFromToken(token);
		return exchange.getRequest().mutate().header("userId", String.valueOf(claims.get("sub"))).build();
	}
}
