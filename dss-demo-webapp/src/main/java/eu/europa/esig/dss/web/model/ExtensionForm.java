package eu.europa.esig.dss.web.model;

import eu.europa.esig.dss.enumerations.ASiCContainerType;
import eu.europa.esig.dss.enumerations.SignatureForm;
import eu.europa.esig.dss.enumerations.SignatureLevel;
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

	private ASiCContainerType containerType;

	@NotNull(message = "{error.signature.form.mandatory}")
	private SignatureForm signatureForm;

	@NotNull(message = "{error.signature.level.mandatory}")
	private SignatureLevel signatureLevel;

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

	public ASiCContainerType getContainerType() {
		return containerType;
	}

	public void setContainerType(ASiCContainerType containerType) {
		this.containerType = containerType;
	}

	public SignatureForm getSignatureForm() {
		return signatureForm;
	}

	public void setSignatureForm(SignatureForm signatureForm) {
		this.signatureForm = signatureForm;
	}

	public SignatureLevel getSignatureLevel() {
		return signatureLevel;
	}

	public void setSignatureLevel(SignatureLevel signatureLevel) {
		this.signatureLevel = signatureLevel;
	}

	@AssertTrue(message = "{error.signed.file.mandatory}")
	public boolean isSignedFile() {
		return (signedFile != null) && (!signedFile.isEmpty());
	}

}
