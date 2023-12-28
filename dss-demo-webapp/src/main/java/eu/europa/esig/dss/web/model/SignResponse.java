package eu.europa.esig.dss.web.model;

import jakarta.validation.constraints.NotNull;

public class SignResponse {

	@NotNull
	private byte[] signatureValue;

	public SignResponse() {
	}

	public byte[] getSignatureValue() {
		return signatureValue;
	}

	public void setSignatureValue(byte[] signatureValue) {
		this.signatureValue = signatureValue;
	}

}
