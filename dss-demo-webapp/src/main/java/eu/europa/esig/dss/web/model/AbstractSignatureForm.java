package eu.europa.esig.dss.web.model;

import eu.europa.esig.dss.enumerations.DigestAlgorithm;
import eu.europa.esig.dss.enumerations.EncryptionAlgorithm;
import eu.europa.esig.dss.enumerations.SignatureForm;
import eu.europa.esig.dss.enumerations.SignatureLevel;
import eu.europa.esig.dss.ws.dto.TimestampDTO;

import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.List;


public abstract class AbstractSignatureForm {

	// @AssertTrue(message = "{error.nexu.not.found}")
	private boolean nexuDetected;

	private Date signingDate;

	private boolean signWithExpiredCertificate;

	private boolean addContentTimestamp;

	@NotNull(message = "{error.signature.form.mandatory}")
	private SignatureForm signatureForm;

	@NotNull(message = "{error.signature.level.mandatory}")
	private SignatureLevel signatureLevel;

	@NotNull(message = "{error.digest.algo.mandatory}")
	private DigestAlgorithm digestAlgorithm;

	private byte[] certificate;

	private List<byte[]> certificateChain;

	private EncryptionAlgorithm encryptionAlgorithm;

	private byte[] signatureValue;

	private TimestampDTO contentTimestamp;

	public boolean isNexuDetected() {
		return nexuDetected;
	}

	public void setNexuDetected(boolean nexuDetected) {
		this.nexuDetected = nexuDetected;
	}

	public Date getSigningDate() {
		return signingDate;
	}

	public void setSigningDate(Date signingDate) {
		this.signingDate = signingDate;
	}

	public boolean isSignWithExpiredCertificate() {
		return signWithExpiredCertificate;
	}

	public void setSignWithExpiredCertificate(boolean signWithExpiredCertificate) {
		this.signWithExpiredCertificate = signWithExpiredCertificate;
	}

	public boolean isAddContentTimestamp() {
		return addContentTimestamp;
	}

	public void setAddContentTimestamp(boolean addContentTimestamp) {
		this.addContentTimestamp = addContentTimestamp;
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

	public DigestAlgorithm getDigestAlgorithm() {
		return digestAlgorithm;
	}

	public void setDigestAlgorithm(DigestAlgorithm digestAlgorithm) {
		this.digestAlgorithm = digestAlgorithm;
	}

	public byte[] getCertificate() {
		return certificate;
	}

	public void setCertificate(byte[] certificate) {
		this.certificate = certificate;
	}

	public List<byte[]> getCertificateChain() {
		return certificateChain;
	}

	public void setCertificateChain(List<byte[]> certificateChain) {
		this.certificateChain = certificateChain;
	}

	public EncryptionAlgorithm getEncryptionAlgorithm() {
		return encryptionAlgorithm;
	}

	public void setEncryptionAlgorithm(EncryptionAlgorithm encryptionAlgorithm) {
		this.encryptionAlgorithm = encryptionAlgorithm;
	}

	public byte[] getSignatureValue() {
		return signatureValue;
	}

	public void setSignatureValue(byte[] signatureValue) {
		this.signatureValue = signatureValue;
	}

	public TimestampDTO getContentTimestamp() {
		return contentTimestamp;
	}

	public void setContentTimestamp(TimestampDTO contentTimestamp) {
		this.contentTimestamp = contentTimestamp;
	}

}
