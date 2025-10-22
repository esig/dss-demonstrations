package eu.europa.esig.dss.web.model;

import eu.europa.esig.dss.enumerations.EncryptionAlgorithm;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;
import java.util.List;

@SuppressWarnings("serial")
public class DataToSignParams implements Serializable {

	@NotNull
	private byte[] signingCertificate;
	@NotNull
	private List<byte[]> certificateChain;
	@NotNull
	private EncryptionAlgorithm encryptionAlgorithm;

	public DataToSignParams() {
	}

	public byte[] getSigningCertificate() {
		return signingCertificate;
	}

	public void setSigningCertificate(byte[] signingCertificate) {
		this.signingCertificate = signingCertificate;
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

}
