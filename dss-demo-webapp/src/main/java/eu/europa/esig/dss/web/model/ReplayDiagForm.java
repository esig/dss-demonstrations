package eu.europa.esig.dss.web.model;

import javax.validation.constraints.AssertTrue;

import org.springframework.web.multipart.MultipartFile;

public class ReplayDiagForm {

	private MultipartFile diagnosticFile;
	
	private boolean resetDate;
	
	private boolean defaultPolicy;

	private MultipartFile policyFile;

	public MultipartFile getDiagnosticFile() {
		return diagnosticFile;
	}

	public void setDiagnosticFile(MultipartFile diagnosticFile) {
		this.diagnosticFile = diagnosticFile;
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
