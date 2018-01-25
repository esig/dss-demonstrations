package eu.europa.esig.dss.web.model;

import javax.validation.constraints.AssertTrue;

import org.springframework.web.multipart.MultipartFile;

public class CertificateValidationForm {

	private MultipartFile certificateFile;

	public MultipartFile getCertificateFile() {
		return certificateFile;
	}

	public void setCertificateFile(MultipartFile certificateFile) {
		this.certificateFile = certificateFile;
	}

	@AssertTrue(message = "{error.certificate.mandatory}")
	public boolean isCertificateFile() {
		return (certificateFile != null) && (!certificateFile.isEmpty());
	}

}
