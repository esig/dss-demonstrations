package eu.europa.esig.dss.web.model;

import eu.europa.esig.dss.spi.client.http.Protocol;
import eu.europa.esig.dss.utils.Utils;
import jakarta.validation.constraints.AssertTrue;
import org.springframework.web.multipart.MultipartFile;

import java.util.Date;

public class QwacValidationForm {
	
	// the URL to be validated
	private String url;

	private MultipartFile tlsCertificate;

	private MultipartFile tlsBindingSignature;

	private Date validationTime;

	private int timezoneDifference;

	private boolean defaultPolicy;

	private MultipartFile policyFile;
	
    private boolean includeCertificateTokens;
    
    private boolean includeRevocationTokens;

	private boolean includeUserFriendlyIdentifiers = true;

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public MultipartFile getTlsCertificate() {
		return tlsCertificate;
	}

	public void setTlsCertificate(MultipartFile tlsCertificate) {
		this.tlsCertificate = tlsCertificate;
	}

	public MultipartFile getTlsBindingSignature() {
		return tlsBindingSignature;
	}

	public void setTlsBindingSignature(MultipartFile tlsBindingSignature) {
		this.tlsBindingSignature = tlsBindingSignature;
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

	public boolean isIncludeUserFriendlyIdentifiers() {
		return includeUserFriendlyIdentifiers;
	}

	public void setIncludeUserFriendlyIdentifiers(boolean includeUserFriendlyIdentifiers) {
		this.includeUserFriendlyIdentifiers = includeUserFriendlyIdentifiers;
	}

	@AssertTrue(message = "{error.url.invalid}")
	public boolean isUrlValid() {
		return Protocol.isHttpUrl(Utils.trim(url));
	}

}
