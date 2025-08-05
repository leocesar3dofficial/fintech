package com.leo.fintech.common.validation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

@Documented
@Constraint(validatedBy = PasswordValidator.class)
@Target({ ElementType.FIELD })
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
