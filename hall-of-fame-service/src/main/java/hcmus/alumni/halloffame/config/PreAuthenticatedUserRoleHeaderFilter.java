package hcmus.alumni.halloffame.config;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
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

import hcmus.alumni.halloffame.repository.HallOfFameRepository;

@Component
public class PreAuthenticatedUserRoleHeaderFilter extends OncePerRequestFilter {
	@Autowired
    private HallOfFameRepository hofRepository;
	
	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		
		
        String userId = request.getHeader("userId");

		Authentication authentication = new UsernamePasswordAuthenticationToken(userId, null, getAuthorities(userId));

		SecurityContextHolder.getContext().setAuthentication(authentication);

		filterChain.doFilter(request, response);
	}

	public Collection<? extends GrantedAuthority> getAuthorities(String userId) {
		List<String> permissions = hofRepository.getPermissions(userId, "Hof");
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();


        for (String permission : permissions) {
            authorities.add(new SimpleGrantedAuthority(permission));
        }


		return authorities;
	}
}