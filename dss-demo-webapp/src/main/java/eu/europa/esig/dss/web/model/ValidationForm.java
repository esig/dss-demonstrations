package eu.europa.esig.dss.web.model;

import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.AssertTrue;

import org.springframework.web.multipart.MultipartFile;

import eu.europa.esig.dss.utils.Utils;
import eu.europa.esig.dss.validation.executor.ValidationLevel;

public class ValidationForm {

	private MultipartFile signedFile;

	private List<OriginalDocumentForm> originalFiles = new ArrayList<OriginalDocumentForm>(10);

	private ValidationLevel validationLevel;

	private boolean defaultPolicy;

	private MultipartFile policyFile;
	
	private boolean includeCertificateTokens;
	
	private boolean includeRevocationTokens;
	
	private boolean includeTimestampTokens;

	public MultipartFile getSignedFile() {
		return signedFile;
	}

	public void setSignedFile(MultipartFile signedFile) {
		this.signedFile = signedFile;
	}

	public List<OriginalDocumentForm> getOriginalFiles() {
		return originalFiles;
	}

	public void setOriginalFiles(List<OriginalDocumentForm> originalFiles) {
		this.originalFiles = originalFiles;
	}

	public ValidationLevel getValidationLevel() {
		return validationLevel;
	}

	public void setValidationLevel(ValidationLevel validationLevel) {
		this.validationLevel = validationLevel;
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
	
	public boolean isIncludeCertificateTokens() {
		return includeCertificateTokens;
	}

	public void setIncludeCertificateTokens(boolean includeCertificateTokens) {
		this.includeCertificateTokens = includeCertificateTokens;
	}

	public boolean isIncludeRevocationTokens() {
		return includeRevocationTokens;
	}

	public void setIncludeRevocationTokens(boolean includeRevocationTokens) {
		this.includeRevocationTokens = includeRevocationTokens;
	}

	public boolean isIncludeTimestampTokens() {
		return includeTimestampTokens;
	}

	public void setIncludeTimestampTokens(boolean includeTimestampTokens) {
		this.includeTimestampTokens = includeTimestampTokens;
	}

	@AssertTrue(message = "{error.signed.file.mandatory}")
	public boolean isSignedFile() {
		return (signedFile != null) && (!signedFile.isEmpty());
	}

	@AssertTrue(message = "{error.signed.file.mandatory}")
	public boolean isOriginalFiles() {
		if (Utils.isCollectionNotEmpty(originalFiles)) {
			boolean atLeastOneOriginalDoc = false;
			for (OriginalDocumentForm originalDocumentForm : originalFiles) {
				if (originalDocumentForm.isNotEmpty()) {
					atLeastOneOriginalDoc = true;
					break;
				}
			}
			return atLeastOneOriginalDoc;
		}
		return true;
	}

}
