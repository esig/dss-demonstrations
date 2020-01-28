package eu.europa.esig.dss.web.model;

import org.springframework.web.multipart.MultipartFile;

import eu.europa.esig.dss.utils.Utils;

public class CertificateForm {
	
	private MultipartFile certificateFile;
	
	private String certificateBase64;
	
	public CertificateForm() {
	}

	public MultipartFile getCertificateFile() {
		return certificateFile;
	}

	public void setCertificateFile(MultipartFile certificateFile) {
		this.certificateFile = certificateFile;
	}

	public String getCertificateBase64() {
		return certificateBase64;
	}

	public void setCertificateBase64(String certificateBase64) {
		this.certificateBase64 = certificateBase64;
	}
	
	private boolean isCertificateFileNotEmpty() {
		return certificateFile != null && !certificateFile.isEmpty();
	}
	
	private boolean isCertificateBase64Valid() {
		return Utils.isStringNotBlank(certificateBase64) && Utils.isBase64Encoded(certificateBase64);
	}

	public boolean isValid() {
		return isCertificateFileNotEmpty() || isCertificateBase64Valid();
	}

}
