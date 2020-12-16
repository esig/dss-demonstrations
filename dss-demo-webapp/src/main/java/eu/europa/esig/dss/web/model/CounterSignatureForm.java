package eu.europa.esig.dss.web.model;

import java.util.List;

import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.NotNull;

import org.springframework.web.multipart.MultipartFile;

public class CounterSignatureForm extends AbstractSignatureForm {

	private MultipartFile documentToCounterSign;

	@NotNull(message = "{error.signature.id.mandatory}")
	private String signatureIdToCounterSign;

	/** Detached contents */
	private List<OriginalFile> originalFiles;

	public MultipartFile getDocumentToCounterSign() {
		return documentToCounterSign;
	}

	public void setDocumentToCounterSign(MultipartFile documentToCounterSign) {
		this.documentToCounterSign = documentToCounterSign;
	}

	public String getSignatureIdToCounterSign() {
		return signatureIdToCounterSign;
	}

	public void setSignatureIdToCounterSign(String signatureIdToCounterSign) {
		this.signatureIdToCounterSign = signatureIdToCounterSign;
	}

	public List<OriginalFile> getOriginalFiles() {
		return originalFiles;
	}

	public void setOriginalFiles(List<OriginalFile> originalFiles) {
		this.originalFiles = originalFiles;
	}

	@AssertTrue(message = "{error.document.to.counter.sign.mandatory}")
	public boolean isDocumentToCounterSign() {
		return (documentToCounterSign != null) && (!documentToCounterSign.isEmpty());
	}

}
