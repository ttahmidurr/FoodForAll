package com.foodforall.util;

import java.util.regex.Pattern;

public class ValidationUtil {
    
    private static final String EMAIL_REGEX = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
    private static final Pattern EMAIL_PATTERN = Pattern.compile(EMAIL_REGEX);
    
    private static final String PHONE_REGEX = "^\\+?[0-9]{10,15}$";
    private static final Pattern PHONE_PATTERN = Pattern.compile(PHONE_REGEX);
    
    public static boolean isValidEmail(String email) {
        if (email == null) {
            return false;
        }
        return EMAIL_PATTERN.matcher(email).matches();
    }
    
    public static boolean isValidPhone(String phone) {
        if (phone == null) {
            return false;
        }
        return PHONE_PATTERN.matcher(phone).matches();
    }
    
    public static boolean isStrongPassword(String password) {
        if (password == null || password.length() < 8) {
            return false;
        }
        
        boolean hasUppercase = false;
        boolean hasLowercase = false;
        boolean hasDigit = false;
        boolean hasSpecialChar = false;
        
        for (char c : password.toCharArray()) {
            if (Character.isUpperCase(c)) {
                hasUppercase = true;
            } else if (Character.isLowerCase(c)) {
                hasLowercase = true;
            } else if (Character.isDigit(c)) {
                hasDigit = true;
            } else {
                hasSpecialChar = true;
            }
        }
        
        return hasUppercase && hasLowercase && hasDigit && hasSpecialChar;
    }
    
    public static boolean isValidUsername(String username) {
        return username != null && username.length() >= 3 && !username.contains(" ");
    }
    
    public static boolean isValidName(String name) {
        return name != null && !name.trim().isEmpty();
    }
    
    public static boolean isPositiveNumber(String number) {
        try {
            double value = Double.parseDouble(number);
            return value > 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }
    
    public static boolean isPositiveInteger(String number) {
        try {
            int value = Integer.parseInt(number);
            return value > 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
