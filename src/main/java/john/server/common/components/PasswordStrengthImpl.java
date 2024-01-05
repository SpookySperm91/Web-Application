package john.server.common.components;

import john.server.common.components.interfaces.PasswordStrength;
import john.server.common.dto.ResponseLayer;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

// CHECK PROVIDED PASSWORD; Return response to the Controller
@Component
public class PasswordStrengthImpl implements PasswordStrength {


    public ResponseLayer checkPassword(String password) {
        if (password == null || password.isEmpty()) {
            return new ResponseLayer(false, "Password is empty", HttpStatus.LENGTH_REQUIRED);
        }
        if (password.length() > 30) {
            return new ResponseLayer(false, "Password exceeds maximum characters", HttpStatus.NOT_ACCEPTABLE);
        }
        if (password.length() < 8) {
            return new ResponseLayer(false, "Password is weak(tip: password length should be more than 8)", HttpStatus.NOT_ACCEPTABLE);
        }

        boolean hasLower = false, hasUpper = false, hasDigit = false, specialChar = false;
        Set<Character> set = new HashSet<>(
                Arrays.asList('!', '@', '#', '$', '%', '^', '&', '*', '(', ')', '-', '+', '='));

        for (char i : password.toCharArray()) {
            if (Character.isLowerCase(i))
                hasLower = true;
            if (Character.isUpperCase(i))
                hasUpper = true;
            if (Character.isDigit(i))
                hasDigit = true;
            if (set.contains(i))
                specialChar = true;
        }

        if (hasLower && hasUpper // Both case should true if no characters or numbers
                || hasDigit || specialChar) {
            return new ResponseLayer(true);
        } else {
            return new ResponseLayer(false, "Password is weak(tip: add special characters or numbers; *#$45)", HttpStatus.NOT_ACCEPTABLE);
        }
    }
}
