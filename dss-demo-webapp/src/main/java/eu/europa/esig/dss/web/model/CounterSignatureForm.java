package eu.europa.esig.dss.web.model;

import eu.europa.esig.dss.web.validation.AssertMultipartFile;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public class CounterSignatureForm extends AbstractSignatureForm {

	@AssertMultipartFile
	private MultipartFile documentToCounterSign;

	@NotNull(message = "{error.signature.id.mandatory}")
	private String signatureIdToCounterSign;

	/** Detached contents */
	@AssertMultipartFile
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
