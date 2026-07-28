package eu.europa.esig.dss.web.model;

import eu.europa.esig.dss.web.validation.AssertMultipartFile;
import jakarta.validation.constraints.AssertTrue;
import org.springframework.web.multipart.MultipartFile;

import java.util.Date;
import java.util.List;

public class AttestationValidationForm {

	@AssertMultipartFile
	private MultipartFile attestationFile;

    @AssertMultipartFile
    private MultipartFile sessionTranscript;

	private Date validationTime;

	private int timezoneDifference;

	private boolean defaultPolicy;

	@AssertMultipartFile
	private MultipartFile policyFile;

	@AssertMultipartFile
	private MultipartFile cryptographicSuite;

	@AssertMultipartFile
	private MultipartFile signingCertificate;

	@AssertMultipartFile
	private List<MultipartFile> adjunctCertificates;

	private boolean includeCertificateTokens;

	private boolean includeRevocationTokens;

	private boolean includeTimestampTokens;

	private boolean includeSemantics;

	private boolean includeUserFriendlyIdentifiers = true;

    public MultipartFile getAttestationFile() {
        return attestationFile;
    }

    public void setAttestationFile(final MultipartFile attestationFile) {
        this.attestationFile = attestationFile;
    }

    public MultipartFile getSessionTranscript() {
        return sessionTranscript;
    }

    public void setSessionTranscript(final MultipartFile sessionTranscript) {
        this.sessionTranscript = sessionTranscript;
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

	public MultipartFile getCryptographicSuite() {
		return cryptographicSuite;
	}

	public void setCryptographicSuite(MultipartFile cryptographicSuite) {
		this.cryptographicSuite = cryptographicSuite;
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

    @AssertTrue(message = "{error.attestation.file.mandatory}")
    public boolean isAttestationFormValid() {
        return (attestationFile != null) && (!attestationFile.isEmpty());
    }

}
