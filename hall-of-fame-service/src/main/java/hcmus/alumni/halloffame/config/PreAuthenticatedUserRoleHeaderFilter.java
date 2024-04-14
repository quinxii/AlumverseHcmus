package hcmus.alumni.halloffame.config;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class PreAuthenticatedUserRoleHeaderFilter extends OncePerRequestFilter {
	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		// TODO Auto-generated method stub
		Enumeration<String> roles = request.getHeaders("roles");
		String userId = request.getHeader("userId");

		// Create authentication object with extracted roles
		Authentication authentication = new UsernamePasswordAuthenticationToken(userId, null, getAuthorities(roles));

		// Set the authentication object to SecurityContextHolder
		SecurityContextHolder.getContext().setAuthentication(authentication);

		// Proceed with the filter chain
		filterChain.doFilter(request, response);
	}

	public Collection<? extends GrantedAuthority> getAuthorities(Enumeration<String> roles) {
		List<SimpleGrantedAuthority> authorities = new ArrayList<>();
		while (roles.hasMoreElements()) {
			authorities.add(new SimpleGrantedAuthority(roles.nextElement()));
		}

		return authorities;
	}
}