package hcmus.alumni.userservice.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

@Configuration
@EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true, jsr250Enabled = true)
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {
	@Autowired
	PreAuthenticatedUserRoleHeaderFilter authFilter;

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http.antMatcher("/**").csrf().disable().addFilterBefore(authFilter, BasicAuthenticationFilter.class)
				.authorizeRequests().anyRequest().authenticated();
	}

}