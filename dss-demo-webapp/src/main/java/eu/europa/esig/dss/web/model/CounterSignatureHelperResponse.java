package eu.europa.esig.dss.web.model;

import java.util.List;

import eu.europa.esig.dss.enumerations.SignatureForm;
import eu.europa.esig.dss.enumerations.SignatureLevel;

public class CounterSignatureHelperResponse {
	
	private List<String> signatureIds;
	
	private SignatureForm signatureForm;

	private List<SignatureLevel> signatureLevels;

	public List<String> getSignatureIds() {
		return signatureIds;
	}

	public void setSignatureIds(List<String> signatureIds) {
		this.signatureIds = signatureIds;
	}
	
	public SignatureForm getSignatureForm() {
		return signatureForm;
	}

	public void setSignatureForm(SignatureForm signatureForm) {
		this.signatureForm = signatureForm;
	}

	public List<SignatureLevel> getSignatureLevels() {
		return signatureLevels;
	}

	public void setSignatureLevels(List<SignatureLevel> signatureLevels) {
		this.signatureLevels = signatureLevels;
	}

}