package com.leo.fintech.config;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PasswordValidator implements ConstraintValidator<ValidPassword, String> {

    private int minLength;
    private boolean requireUppercase;
    private boolean requireNumber;
    private boolean requireSpecial;

    @Override
    public void initialize(ValidPassword constraintAnnotation) {
        this.minLength = constraintAnnotation.minLength();
        this.requireUppercase = constraintAnnotation.requireUppercase();
        this.requireNumber = constraintAnnotation.requireNumber();
        this.requireSpecial = constraintAnnotation.requireSpecial();
    }

    @Override
    public boolean isValid(String password, ConstraintValidatorContext context) {
        if (password == null || password.length() < minLength) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Password must be at least " + minLength + " characters long.")
                    .addConstraintViolation();
            return false;
        }
        if (requireNumber && !password.matches(".*\\d.*")) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Password must contain at least one number.")
                    .addConstraintViolation();
            return false;
        }
        if (requireUppercase && !password.matches(".*[A-Z].*")) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Password must contain at least one uppercase letter.")
                    .addConstraintViolation();
            return false;
        }
        if (requireSpecial && !password.matches(".*[!@#$%^&*()_+=\\[\\]{};':\"\\\\|,.<>/?-].*")) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Password must contain at least one special character.")
                    .addConstraintViolation();
            return false;
        }

        return true;
    }
}
