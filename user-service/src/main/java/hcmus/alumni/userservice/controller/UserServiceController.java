package hcmus.alumni.userservice.controller;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import hcmus.alumni.userservice.model.UserModel;
import hcmus.alumni.userservice.repository.UserRepository;

@RestController
@RequestMapping("/userservice")
public class UserServiceController {

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/login")
    public UserModel login(@RequestParam String email, @RequestParam String pass) {
        // Find user by email
        UserModel user = userRepository.findByEmailAndPass(email,pass);
//        if (user != null && passwordMatches(password, user.getPassword())) {
    	if (user != null && pass.equals(user.getPass())) {
            // Update last login time
            user.setLastLogin(new Date());
            userRepository.save(user);
            return user;
        }
        return null;
    }

    private boolean passwordMatches(String password, String hashedPassword) {
        // Compare input password with hashed password using BCrypt
        return new BCryptPasswordEncoder().matches(password, hashedPassword);
    }
}

