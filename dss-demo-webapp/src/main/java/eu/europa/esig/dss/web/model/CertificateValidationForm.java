package eu.europa.esig.dss.web.model;

import java.util.Date;
import java.util.List;

import javax.validation.constraints.AssertTrue;

import org.springframework.web.multipart.MultipartFile;

public class CertificateValidationForm {

	private Date validationTime;

	private MultipartFile certificateFile;

	private List<MultipartFile> certificateChainFiles;
	
	private boolean includeCertificateTokens;
	
	private boolean includeRevocationTokens;

	public Date getValidationTime() {
		return validationTime;
	}

	public void setValidationTime(Date validationTime) {
		this.validationTime = validationTime;
	}

	public MultipartFile getCertificateFile() {
		return certificateFile;
	}

	public void setCertificateFile(MultipartFile certificateFile) {
		this.certificateFile = certificateFile;
	}

	public List<MultipartFile> getCertificateChainFiles() {
		return certificateChainFiles;
	}

	public void setCertificateChainFiles(List<MultipartFile> certificateChainFiles) {
		this.certificateChainFiles = certificateChainFiles;
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

	@AssertTrue(message = "{error.certificate.mandatory}")
	public boolean isCertificateFile() {
		return (certificateFile != null) && (!certificateFile.isEmpty());
	}

}
