package eu.europa.esig.dss.web.model;

import eu.europa.esig.dss.enumerations.DigestAlgorithm;
import eu.europa.esig.dss.utils.Utils;
import org.springframework.web.multipart.MultipartFile;

/**
 * Represents an uploaded document.
 * Either {@code completeFile} or {@code filename} and {@code base64Digest} and {@code digestAlgorithm} shall be present.
 *
 */
public class OriginalFile {

	private MultipartFile completeFile;

	private String filename;

	private String base64Digest;

	private DigestAlgorithm digestAlgorithm;
	
	public OriginalFile() {
	}

	public MultipartFile getCompleteFile() {
		return completeFile;
	}

	public void setCompleteFile(MultipartFile completeFile) {
		this.completeFile = completeFile;
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
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
		boolean filledCompleteFile = completeFile != null && !completeFile.isEmpty();
		boolean filledFilename = Utils.isStringNotEmpty(filename);
		boolean filledDigest = digestAlgorithm != null && Utils.isStringNotEmpty(base64Digest) && Utils.isBase64Encoded(base64Digest);
		return filledCompleteFile || (filledFilename && filledDigest);
	}

}
