package eu.europa.esig.dss.web.model;

import org.hibernate.validator.constraints.NotEmpty;

import eu.europa.esig.dss.web.validation.Base64;

public class SignatureDigestForm extends AbstractSignatureForm {

	@Base64
	@NotEmpty(message = "{error.to.sign.digest.mandatory}")
	private String digestToSign;
	
	public String getDigestToSign() {
		return digestToSign;
	}

	public void setDigestToSign(String digestToSign) {
		this.digestToSign = digestToSign;
	}
	
}
