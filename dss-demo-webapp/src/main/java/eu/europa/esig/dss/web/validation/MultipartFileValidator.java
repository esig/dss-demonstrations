package eu.europa.esig.dss.web.validation;

import eu.europa.esig.dss.web.config.MultipartResolverProvider;
import eu.europa.esig.dss.web.model.OriginalFile;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public class MultipartFileValidator implements ConstraintValidator<AssertMultipartFile, Object> {

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }
        return validateFileSize(value);
    }

    private boolean validateFileSize(Object file) {
        if (file instanceof MultipartFile) {
            MultipartFile multipartFile = (MultipartFile) file;
            return multipartFile.isEmpty() || multipartFile.getSize() <= MultipartResolverProvider.getInstance().getMaxFileSize();

        } else if (file instanceof OriginalFile) {
            OriginalFile originalFile = (OriginalFile) file;
            return !originalFile.isNotEmpty() || originalFile.getCompleteFile() == null
                    || validateFileSize(originalFile.getCompleteFile());

        } else if (file instanceof List<?>) {
            for (Object entry : (List<?>) file) {
                if (!validateFileSize(entry)) {
                    return false;
                }
            }

        } else {
            throw new UnsupportedOperationException(
                    String.format("Unsupported object class '%s'!", file.getClass().getName()));
        }
        return true;
    }

}