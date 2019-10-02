package eu.europa.esig.dss.web.validation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;

@Target({ElementType.METHOD,ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = Base64Validator.class)
@Documented
public @interface Base64 {

    String message() default "{error.digest.base64}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

}