package eu.europa.esig.dss.web.model;

import javax.validation.constraints.NotNull;

import eu.europa.esig.dss.enumerations.DigestAlgorithm;
import eu.europa.esig.dss.utils.Utils;

public class OriginalFile {

	@NotNull
	private String filename;

	private String base64Complete;

	private String base64Digest;

	private DigestAlgorithm digestAlgorithm;
	
	public OriginalFile() {
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public String getBase64Complete() {
		return base64Complete;
	}

	public void setBase64Complete(String base64Complete) {
		this.base64Complete = base64Complete;
	}

	public String getBase64Digest() {
		return base64Digest;
	}

	public void setBase64Digest(String base64Digest) {
		this.base64Digest = base64Digest;
	}

	public DigestAlgorithm getDigestAlgorithm() {
		return digestAlgorithm;
	}

	public void setDigestAlgorithm(DigestAlgorithm digestAlgorithm) {
		this.digestAlgorithm = digestAlgorithm;
	}

	public boolean isNotEmpty() {
		boolean filedCompleteFile = Utils.isStringNotEmpty(base64Complete) && Utils.isBase64Encoded(base64Complete);
		boolean filedDigest = digestAlgorithm != null && Utils.isStringNotEmpty(base64Digest) && Utils.isBase64Encoded(base64Digest);
		return filedCompleteFile || filedDigest;
	}

}
