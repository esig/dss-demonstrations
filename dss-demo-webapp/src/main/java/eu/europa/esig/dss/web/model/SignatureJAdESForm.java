package eu.europa.esig.dss.web.model;

import eu.europa.esig.dss.enumerations.JWSSerializationType;
import eu.europa.esig.dss.enumerations.SigDMechanism;
import eu.europa.esig.dss.enumerations.SignatureForm;
import eu.europa.esig.dss.enumerations.SignaturePackaging;
import eu.europa.esig.dss.web.WebAppUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.NotNull;
import java.util.List;

public class SignatureJAdESForm extends AbstractSignatureForm {

	private List<MultipartFile> documentsToSign;

	@NotNull(message = "{error.signature.jws.serialization.type.mandatory}")
	private JWSSerializationType jwsSerializationType;

	@NotNull(message = "{error.signature.packaging.mandatory}")
	private SignaturePackaging signaturePackaging;
	
	private SigDMechanism sigDMechanism;
	
	private boolean base64UrlEncodedPayload;

	private boolean base64UrlEncodedEtsiU;
	
	public SignatureJAdESForm() {
		setSignatureForm(SignatureForm.JAdES);
	}

	public List<MultipartFile> getDocumentsToSign() {
		return documentsToSign;
	}

	public void setDocumentsToSign(List<MultipartFile> documentsToSign) {
		this.documentsToSign = documentsToSign;
	}

	public JWSSerializationType getJwsSerializationType() {
		return jwsSerializationType;
	}

	public void setJwsSerializationType(JWSSerializationType jwsSerializationType) {
		this.jwsSerializationType = jwsSerializationType;
	}

	public SignaturePackaging getSignaturePackaging() {
		return signaturePackaging;
	}

	public void setSignaturePackaging(SignaturePackaging signaturePackaging) {
		this.signaturePackaging = signaturePackaging;
	}

	public SigDMechanism getSigDMechanism() {
		return sigDMechanism;
	}

	public void setSigDMechanism(SigDMechanism sigDMechanism) {
		this.sigDMechanism = sigDMechanism;
	}

	public boolean isBase64UrlEncodedPayload() {
		return base64UrlEncodedPayload;
	}

	public void setBase64UrlEncodedPayload(boolean base64UrlEncodedPayload) {
		this.base64UrlEncodedPayload = base64UrlEncodedPayload;
	}

	public boolean isBase64UrlEncodedEtsiU() {
		return base64UrlEncodedEtsiU;
	}

	public void setBase64UrlEncodedEtsiU(boolean base64UrlEncodedEtsiU) {
		this.base64UrlEncodedEtsiU = base64UrlEncodedEtsiU;
	}

	@AssertTrue(message = "{error.to.sign.files.mandatory}")
	public boolean isDocumentsToSign() {
		return WebAppUtils.isCollectionNotEmpty(documentsToSign);
	}

	@AssertTrue(message = "{error.to.sign.one.file.enveloping}")
	public boolean isDocumentsToSignConfigurationValid() {
		return !SignaturePackaging.ENVELOPING.equals(signaturePackaging) || (documentsToSign != null && documentsToSign.size() == 1);
	}

	@AssertTrue(message = "{error.jades.sigDMechanism.mandatory}")
	public boolean isSigDMechanismValid() {
		return !SignaturePackaging.DETACHED.equals(signaturePackaging) || sigDMechanism != null;
	}

}