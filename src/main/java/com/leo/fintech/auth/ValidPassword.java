package com.leo.fintech.auth;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = PasswordValidator.class)
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidPassword {
    String message() default "Invalid password";

    int minLength() default 8;
    boolean requireUppercase() default true;
    boolean requireNumber() default true;
    boolean requireSpecial() default true;

    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
