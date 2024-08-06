package hcmus.alumni.apigateway;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import com.fasterxml.jackson.databind.ObjectMapper;

import hcmus.alumni.apigateway.exception.ErrorResponse;
import hcmus.alumni.apigateway.filter.RouteValidator;
import hcmus.alumni.apigateway.utils.JwtUtils;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import reactor.core.publisher.Mono;

@Component
public class AuthenticationGatewayFilterFactory
		extends AbstractGatewayFilterFactory<AuthenticationGatewayFilterFactory.Config> {

	@Autowired
	private RouteValidator routeValidator;

	@Autowired
	private JwtUtils jwtUtils;

	public AuthenticationGatewayFilterFactory() {
		super(Config.class);
	}

	public static class Config {
	}

	@Override
	public GatewayFilter apply(Config config) {
		return ((exchange, chain) -> {
			ServerHttpRequest req = exchange.getRequest();
			if (routeValidator.isPublic.test(req)) {
				return chain.filter(exchange);
			}

			if (routeValidator.isSemiPublic.test(req)) {
				return handleJwt(exchange, chain, req, true);
			}

			return handleJwt(exchange, chain, req, false);
		});
	}

	private Mono<Void> handleJwt(ServerWebExchange exchange, GatewayFilterChain chain, ServerHttpRequest req,
			boolean isOptional) {
		List<String> authHeaders = req.getHeaders().get(HttpHeaders.AUTHORIZATION);
		System.out.println();
		if (authHeaders != null && !authHeaders.isEmpty()) {
			String authHeader = authHeaders.get(0);
			if (authHeader != null && authHeader.startsWith("Bearer ")) {
				authHeader = authHeader.substring(7);
			}

			try {
				if (jwtUtils.validateToken(authHeader)) {
					req = updateRequest(exchange, authHeader);
				}
			} catch (MalformedJwtException ex) {
				System.err.println("Invalid JWT token");
				return this.onError(exchange, "JWT Token không hợp lệ", HttpStatus.UNAUTHORIZED);
			} catch (ExpiredJwtException ex) {
				System.err.println("Expired JWT token");
				return this.onError(exchange, "JWT Token đã hết hạn", HttpStatus.UNAUTHORIZED);
			} catch (UnsupportedJwtException ex) {
				System.err.println("Unsupported JWT token");
				return this.onError(exchange, "JWT Token không được hỗ trợ", HttpStatus.UNAUTHORIZED);
			} catch (IllegalArgumentException ex) {
				System.err.println("JWT claims string is empty.");
				return this.onError(exchange, "JWT claims bị trống", HttpStatus.UNAUTHORIZED);
			}
		} else {
			if (isOptional) {
				return chain.filter(exchange);
			}
			return this.onError(exchange, "Thiếu Authorization header", HttpStatus.UNAUTHORIZED);
		}
		return chain.filter(exchange.mutate().request(req).build());
	}

	private Mono<Void> onError(ServerWebExchange exchange, String errorMessage, HttpStatus httpStatus) {
		ServerHttpResponse response = exchange.getResponse();
		response.setStatusCode(httpStatus);
		response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

		ErrorResponse errorResponse = new ErrorResponse(httpStatus.value(), errorMessage);

		byte[] bytes;
		try {
			bytes = new ObjectMapper().writeValueAsBytes(errorResponse);
		} catch (Exception e) {
			bytes = ("{\"error\": {\"code\": " + httpStatus.value() + ", \"message\": \"Unexpected error\"}}")
					.getBytes(StandardCharsets.UTF_8);
		}

		DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(bytes);
		return exchange.getResponse().writeWith(Mono.just(buffer));
	}

	private ServerHttpRequest updateRequest(ServerWebExchange exchange, String token) {
		Claims claims = jwtUtils.getAllClaimsFromToken(token);
		@SuppressWarnings("unchecked")
		ArrayList<String> roles = (ArrayList<String>) claims.get("roles");
		ServerHttpRequest modifiedRequest = exchange.getRequest().mutate().headers(httpHeaders -> {
			httpHeaders.add("userId", String.valueOf(claims.get("sub")));
			httpHeaders.addAll("roles", roles);
		}).build();
		return modifiedRequest;
	}
}