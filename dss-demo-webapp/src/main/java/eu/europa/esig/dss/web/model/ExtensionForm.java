package eu.europa.esig.dss.web.model;

import eu.europa.esig.dss.enumerations.SignatureProfile;
import eu.europa.esig.dss.web.validation.AssertMultipartFile;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public class ExtensionForm {

	@AssertMultipartFile
	private MultipartFile signedFile;

	@AssertMultipartFile
	private List<MultipartFile> originalFiles;

	@NotNull(message = "{error.signature.level.mandatory}")
	private SignatureProfile signatureProfile;

	public MultipartFile getSignedFile() {
		return signedFile;
	}

	public void setSignedFile(MultipartFile signedFile) {
		this.signedFile = signedFile;
	}

	public List<MultipartFile> getOriginalFiles() {
		return originalFiles;
	}

	public void setOriginalFiles(List<MultipartFile> originalFiles) {
		this.originalFiles = originalFiles;
	}

	public SignatureProfile getSignatureProfile() {
		return signatureProfile;
	}

	public void setSignatureProfile(SignatureProfile signatureProfile) {
		this.signatureProfile = signatureProfile;
	}

	@AssertTrue(message = "{error.signed.file.mandatory}")
	public boolean isSignedFile() {
		return (signedFile != null) && (!signedFile.isEmpty());
	}

}
