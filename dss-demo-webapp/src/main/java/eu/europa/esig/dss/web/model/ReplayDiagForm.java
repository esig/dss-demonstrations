package eu.europa.esig.dss.web.model;

import eu.europa.esig.dss.enumerations.ValidationLevel;
import eu.europa.esig.dss.web.validation.AssertMultipartFile;
import jakarta.validation.constraints.AssertTrue;
import org.springframework.web.multipart.MultipartFile;

public class ReplayDiagForm {

	@AssertMultipartFile
	private MultipartFile diagnosticFile;
	
	private ValidationLevel validationLevel;

	private boolean resetDate;
	
	private boolean defaultPolicy;

	@AssertMultipartFile
	private MultipartFile policyFile;

	public MultipartFile getDiagnosticFile() {
		return diagnosticFile;
	}

	public void setDiagnosticFile(MultipartFile diagnosticFile) {
		this.diagnosticFile = diagnosticFile;
	}

	public ValidationLevel getValidationLevel() {
		return validationLevel;
	}

	public void setValidationLevel(ValidationLevel validationLevel) {
		this.validationLevel = validationLevel;
	}

	public boolean isResetDate() {
		return resetDate;
	}

	public void setResetDate(boolean resetDate) {
		this.resetDate = resetDate;
	}

	public boolean isDefaultPolicy() {
		return defaultPolicy;
	}

	public void setDefaultPolicy(boolean defaultPolicy) {
		this.defaultPolicy = defaultPolicy;
	}

	public MultipartFile getPolicyFile() {
		return policyFile;
	}

	public void setPolicyFile(MultipartFile policyFile) {
		this.policyFile = policyFile;
	}
	
	@AssertTrue(message = "{error.diagnostic.file.mandatory}")
	public boolean isDiagnosticFile() {
		return (diagnosticFile != null) && (!diagnosticFile.isEmpty());
	}
}
