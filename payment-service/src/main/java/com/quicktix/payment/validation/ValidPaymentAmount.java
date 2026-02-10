package com.quicktix.payment.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

/**
 * Custom validation annotation for payment amount
 */
@Target({ ElementType.FIELD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = PaymentAmountValidator.class)
@Documented
public @interface ValidPaymentAmount {
    String message() default "Invalid payment amount";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
