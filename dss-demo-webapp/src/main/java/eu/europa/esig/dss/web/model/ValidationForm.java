package eu.europa.esig.dss.web.model;

import eu.europa.esig.dss.utils.Utils;
import eu.europa.esig.dss.validation.executor.ValidationLevel;
import eu.europa.esig.dss.web.validation.AssertMultipartFile;
import jakarta.validation.constraints.AssertTrue;
import org.springframework.web.multipart.MultipartFile;

import java.util.Date;
import java.util.List;

public class ValidationForm {

	@AssertMultipartFile
	private MultipartFile signedFile;

	@AssertMultipartFile
	private List<OriginalFile> originalFiles;

	private Date validationTime;

	private int timezoneDifference;

	private ValidationLevel validationLevel;

	private boolean defaultPolicy;

	@AssertMultipartFile
	private MultipartFile policyFile;
	
	@AssertMultipartFile
	private MultipartFile signingCertificate;

	@AssertMultipartFile
	private List<MultipartFile> adjunctCertificates;

	@AssertMultipartFile
	private List<MultipartFile> evidenceRecordFiles;
	
	private boolean includeCertificateTokens;
	
	private boolean includeRevocationTokens;
	
	private boolean includeTimestampTokens;

	private boolean includeSemantics;

	private boolean includeUserFriendlyIdentifiers = true;

	public MultipartFile getSignedFile() {
		return signedFile;
	}

	public void setSignedFile(MultipartFile signedFile) {
		this.signedFile = signedFile;
	}

	public List<OriginalFile> getOriginalFiles() {
		return originalFiles;
	}

	public void setOriginalFiles(List<OriginalFile> originalFiles) {
		this.originalFiles = originalFiles;
	}

	public Date getValidationTime() {
		return validationTime;
	}

	public void setValidationTime(Date validationTime) {
		this.validationTime = validationTime;
	}

	public int getTimezoneDifference() {
		return timezoneDifference;
	}

	public void setTimezoneDifference(int timezoneDifference) {
		this.timezoneDifference = timezoneDifference;
	}

	public List<MultipartFile> getEvidenceRecordFiles() {
		return evidenceRecordFiles;
	}

	public void setEvidenceRecordFiles(List<MultipartFile> evidenceRecordFiles) {
		this.evidenceRecordFiles = evidenceRecordFiles;
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

	public MultipartFile getSigningCertificate() {
		return signingCertificate;
	}

	public void setSigningCertificate(MultipartFile signingCertificate) {
		this.signingCertificate = signingCertificate;
	}

	public List<MultipartFile> getAdjunctCertificates() {
		return adjunctCertificates;
	}

	public void setAdjunctCertificates(List<MultipartFile> adjunctCertificates) {
		this.adjunctCertificates = adjunctCertificates;
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

	public boolean isIncludeSemantics() {
		return includeSemantics;
	}

	public void setIncludeSemantics(boolean includeSemantics) {
		this.includeSemantics = includeSemantics;
	}

	public boolean isIncludeUserFriendlyIdentifiers() {
		return includeUserFriendlyIdentifiers;
	}

	public void setIncludeUserFriendlyIdentifiers(boolean includeUserFriendlyIdentifiers) {
		this.includeUserFriendlyIdentifiers = includeUserFriendlyIdentifiers;
	}

	@AssertTrue(message = "{error.signed.file.mandatory}")
	public boolean isSignedFile() {
		return (signedFile != null) && (!signedFile.isEmpty());
	}

	@AssertTrue(message = "{error.original.file.empty}")
	public boolean areOriginalFiles() {
		if (Utils.isCollectionNotEmpty(originalFiles)) {
			boolean atLeastOneOriginalDoc = false;
			for (OriginalFile originalDocument : originalFiles) {
				if (originalDocument.isNotEmpty()) {
					atLeastOneOriginalDoc = true;
					break;
				}
			}
			return atLeastOneOriginalDoc;
		}
		return true;
	}

}
