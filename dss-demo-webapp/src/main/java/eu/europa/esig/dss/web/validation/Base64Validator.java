package eu.europa.esig.dss.web.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import eu.europa.esig.dss.utils.Utils;

public class Base64Validator implements ConstraintValidator<Base64, String> {
    
    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        return Utils.isBase64Encoded(value);
    }

	@Override
	public void initialize(Base64 constraintAnnotation) {		
	}
}