package hcmus.alumni.authservice.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import hcmus.alumni.authservice.model.UserModel;
import hcmus.alumni.authservice.repository.UserRepository;

public class CustomUserDetailsService implements UserDetailsService {
    @Autowired
    private UserRepository userRepository;

    @Override
    public CustomUserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // Kiểm tra xem user có tồn tại trong database không?
        UserModel user = userRepository.findByEmail(email);
        if (user == null) {
            throw new UsernameNotFoundException("Email not found!");
        }
        return new CustomUserDetails(user);
    }
}
