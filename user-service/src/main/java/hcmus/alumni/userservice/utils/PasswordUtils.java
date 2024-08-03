package hcmus.alumni.userservice.utils;

import java.util.regex.Pattern;

public class PasswordUtils {
    private static final String PASSWORD_PATTERN = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@#$%^&*!]).{8,}$";

    public static boolean isPasswordStrong(String password) {
        Pattern pattern = Pattern.compile(PASSWORD_PATTERN);
        return pattern.matcher(password).matches();
    }
}
