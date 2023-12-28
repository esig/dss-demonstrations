package eu.europa.esig.dss.web.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD,ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = MultipartFileValidator.class)
public @interface AssertMultipartFile {

    String message() default "{error.file.size}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

}
