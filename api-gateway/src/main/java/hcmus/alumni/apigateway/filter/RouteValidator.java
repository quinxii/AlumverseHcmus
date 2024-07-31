package hcmus.alumni.apigateway.filter;

import org.springframework.http.HttpMethod;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.function.Predicate;

@Component
public class RouteValidator {

	public static final List<ApiEndpoint> openApiEndpoints = List.of(
			new ApiEndpoint(HttpMethod.POST, "/auth/login"),
			new ApiEndpoint(HttpMethod.POST, "/auth/signup"),
			new ApiEndpoint(HttpMethod.POST, "/auth/send-authorize-code"),
			new ApiEndpoint(HttpMethod.POST, "/auth/verify-authorize-code"),
			new ApiEndpoint(HttpMethod.POST, "/auth/forgot-password"),
			new ApiEndpoint(HttpMethod.POST, "/auth/verify-reset-code"));

	public static final List<ApiEndpoint> semiOpenApiEndpoints = List.of(
			// News
			new ApiEndpoint(HttpMethod.GET, "/news"),
			new ApiEndpoint(HttpMethod.GET, "/news/{id}"),
			new ApiEndpoint(HttpMethod.GET, "/news/{id}/related"),
			new ApiEndpoint(HttpMethod.GET, "/news/hot"),
			new ApiEndpoint(HttpMethod.GET, "/news/{id}/comments"),
			new ApiEndpoint(HttpMethod.GET, "/news/comments/{commentId}/children"),
			new ApiEndpoint(HttpMethod.GET, "/news/{newsId}/comments/{commentId}"),
			// Events
			new ApiEndpoint(HttpMethod.GET, "/events"),
			new ApiEndpoint(HttpMethod.GET, "/events/{id}"),
			new ApiEndpoint(HttpMethod.GET, "/events/hot"),
			new ApiEndpoint(HttpMethod.GET, "/events/is-participated"),
			new ApiEndpoint(HttpMethod.GET, "/events/{id}/participants"),
			new ApiEndpoint(HttpMethod.GET, "/events/{id}/comments"),
			new ApiEndpoint(HttpMethod.GET, "/events/comments/{commentId}/children"),
			new ApiEndpoint(HttpMethod.GET, "/events/{eventId}/comments/{commentId}"),
			// Hof
			new ApiEndpoint(HttpMethod.GET, "/hof"),
			new ApiEndpoint(HttpMethod.GET, "/hof/{id}"),
			new ApiEndpoint(HttpMethod.GET, "/hof/rand"));

	public Predicate<ServerHttpRequest> isPublic = request -> openApiEndpoints.stream()
			.anyMatch(endpoint -> matchEndpoint(request, endpoint));

	public Predicate<ServerHttpRequest> isSemiPublic = request -> semiOpenApiEndpoints.stream()
			.anyMatch(endpoint -> matchEndpoint(request, endpoint));

	private boolean matchEndpoint(ServerHttpRequest request, ApiEndpoint endpoint) {
		String requestPath = request.getURI().getPath();
		HttpMethod requestMethod = request.getMethod();

		if (requestMethod == null || !requestMethod.equals(endpoint.getMethod())) {
			return false;
		}

		String regex = endpoint.getPath().replaceAll("\\{[^/]+\\}", "[^/]+");
		return requestPath.matches(regex);
	}

}