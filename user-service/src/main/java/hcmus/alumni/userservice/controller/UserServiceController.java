package hcmus.alumni.userservice.controller;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import hcmus.alumni.userservice.model.UserModel;
import hcmus.alumni.userservice.repository.UserRepository;
import hcmus.alumni.userservice.utils.PasswordUtils;

@RestController
@RequestMapping("/userservice")
public class UserServiceController {

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/login")
    public UserModel login(@RequestParam String email, @RequestParam String pass) {
        // Find user by email
        UserModel user = userRepository.findByEmail(email);
        if (user != null && passwordMatches(pass, user.getPass())) {
            user.setLastLogin(new Date());
            userRepository.save(user);
            return user;
        }
        return user;
    }

    private boolean passwordMatches(String password, String hashedPassword) {
        // Compare input password with hashed password
        String hashedInput = PasswordUtils.hashPassword(password);
        return hashedInput.equals(hashedPassword);
    }

}
