package hcmus.alumni.authservice.config;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import hcmus.alumni.authservice.model.UserModel;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CustomUserDetails implements UserDetails {
	private static final long serialVersionUID = 1L;
	UserModel user;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        List<String> roles = user.getRolesName();
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
         
        for (String role : roles) {
            authorities.add(new SimpleGrantedAuthority(role));
        }
        System.out.println(authorities.toString());
         
        return authorities;
    }

    @Override
    public String getPassword() {
        return user.getPass();
    }

    @Override
    public String getUsername() {
        return user.getEmail();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
