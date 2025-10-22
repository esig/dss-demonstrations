package eu.europa.esig.dss.web.model;

import eu.europa.esig.dss.web.model.serversign.ServerSignResponseBody;
import jakarta.validation.constraints.NotNull;

public class SignResponse extends ServerSignResponseBody {

	private static final long serialVersionUID = -5628924401091562956L;

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
