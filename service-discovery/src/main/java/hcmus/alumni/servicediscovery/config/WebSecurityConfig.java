package hcmus.alumni.servicediscovery.config;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

import static org.springframework.security.config.Customizer.withDefaults;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig {
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable());
                // .authorizeHttpRequests(req -> 
                //     req.requestMatchers(HttpMethod.GET, "/eureka/**").authenticated() // eureka client)
                //         .requestMatchers(HttpMethod.POST, "/eureka/**").authenticated() // eureka client)
                //         .requestMatchers(HttpMethod.DELETE, "/eureka/**").authenticated() // eureka client)
                //         .anyRequest().authenticated()).httpBasic(withDefaults()); // dashboard authorization

        return http.build();
    }
}
