package hcmus.alumni.userservice.config;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import hcmus.alumni.userservice.repository.RoleRepository;

@Component
public class PreAuthenticatedUserRoleHeaderFilter extends OncePerRequestFilter {
	@Autowired
	private RoleRepository roleRepository;

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		
		String userId = request.getHeader("userId");

		// Create authentication object with extracted roles
		Authentication authentication = new UsernamePasswordAuthenticationToken(userId, null, getAuthorities(userId));

		// Set the authentication object to SecurityContextHolder
		SecurityContextHolder.getContext().setAuthentication(authentication);

		// Proceed with the filter chain
		filterChain.doFilter(request, response);
	}

	public Collection<? extends GrantedAuthority> getAuthorities(String userId) {
		List<String> permissions = roleRepository.getPermissions(userId, "User");
		permissions.addAll(roleRepository.getPermissions(userId, "AlumniVerify"));
		List<SimpleGrantedAuthority> authorities = new ArrayList<>();

		for (String permission : permissions) {
			authorities.add(new SimpleGrantedAuthority(permission));
		}

		return authorities;
	}
}