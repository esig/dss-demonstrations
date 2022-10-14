package eu.europa.esig.dss.standalone.task;

import eu.europa.esig.dss.DomUtils;
import eu.europa.esig.dss.definition.xmldsig.XMLDSigElement;
import eu.europa.esig.dss.enumerations.JWSSerializationType;
import eu.europa.esig.dss.enumerations.SigDMechanism;
import eu.europa.esig.dss.enumerations.SignatureForm;
import eu.europa.esig.dss.model.DSSDocument;
import eu.europa.esig.dss.model.FileDocument;
import eu.europa.esig.dss.model.SignatureValue;
import eu.europa.esig.dss.model.ToBeSigned;
import eu.europa.esig.dss.model.x509.CertificateToken;
import eu.europa.esig.dss.spi.tsl.TrustedListsCertificateSource;
import eu.europa.esig.dss.standalone.RemoteDocumentSignatureServiceBuilder;
import eu.europa.esig.dss.standalone.RemoteTrustedListSignatureServiceBuilder;
import eu.europa.esig.dss.standalone.enumeration.SignatureOption;
import eu.europa.esig.dss.standalone.exception.ApplicationException;
import eu.europa.esig.dss.standalone.model.SignatureModel;
import eu.europa.esig.dss.token.DSSPrivateKeyEntry;
import eu.europa.esig.dss.token.MSCAPISignatureToken;
import eu.europa.esig.dss.token.Pkcs11SignatureToken;
import eu.europa.esig.dss.token.Pkcs12SignatureToken;
import eu.europa.esig.dss.token.SignatureTokenConnection;
import eu.europa.esig.dss.utils.Utils;
import eu.europa.esig.dss.ws.converter.DTOConverter;
import eu.europa.esig.dss.ws.converter.RemoteDocumentConverter;
import eu.europa.esig.dss.ws.dto.RemoteCertificate;
import eu.europa.esig.dss.ws.dto.RemoteDocument;
import eu.europa.esig.dss.ws.dto.SignatureValueDTO;
import eu.europa.esig.dss.ws.signature.common.RemoteDocumentSignatureService;
import eu.europa.esig.dss.ws.signature.common.RemoteTrustedListSignatureService;
import eu.europa.esig.dss.ws.signature.dto.parameters.RemoteBLevelParameters;
import eu.europa.esig.dss.ws.signature.dto.parameters.RemoteSignatureParameters;
import eu.europa.esig.dss.ws.signature.dto.parameters.RemoteTrustedListSignatureParameters;
import eu.europa.esig.dss.xades.DSSXMLUtils;
import eu.europa.esig.dss.xades.definition.XAdESNamespaces;
import eu.europa.esig.trustedlist.TrustedListUtils;
import eu.europa.esig.xmldsig.XmlDSigUtils;
import javafx.application.Platform;
import javafx.concurrent.Task;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.transform.dom.DOMSource;
import java.io.IOException;
import java.security.KeyStore.PasswordProtection;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.FutureTask;

public class SigningTask extends Task<DSSDocument> {

	private final static String TRUSTED_LIST_PARENT_ELEMENT = "TrustServiceStatusList";

	private final static String TRUSTED_LIST_NAMESPACE = "http://uri.etsi.org/02231/v2#";

	private final SignatureModel model;
	private final RemoteDocumentSignatureService service;

	private final RemoteTrustedListSignatureService trustedListSignatureService;

	public SigningTask(SignatureModel model, TrustedListsCertificateSource tslCertificateSource) {
		this.model = model;

		RemoteDocumentSignatureServiceBuilder signatureServiceBuilder = new RemoteDocumentSignatureServiceBuilder();
		signatureServiceBuilder.setTslCertificateSource(tslCertificateSource);
		this.service = signatureServiceBuilder.build();

		RemoteTrustedListSignatureServiceBuilder trustedListServiceBuilder = new RemoteTrustedListSignatureServiceBuilder();
		this.trustedListSignatureService = trustedListServiceBuilder.build();
	}

	@Override
	protected DSSDocument call() throws Exception {
		updateProgress(0, 100);
		SignatureTokenConnection token = getToken(model);

		updateProgress(5, 100);
		List<DSSPrivateKeyEntry> keys = token.getKeys();

		updateProgress(10, 100);

		DSSPrivateKeyEntry signer = getSigner(keys);

		FileDocument fileToSign = new FileDocument(model.getFileToSign());
		RemoteDocument toSignDocument = RemoteDocumentConverter.toRemoteDocument(fileToSign);

		DSSDocument signedDocument;
		if (isTLSigning(fileToSign)) {
			RemoteTrustedListSignatureParameters parameters = buildTrustedListParameters(signer);

			ToBeSigned toBeSigned = getDataToSignTrustedList(toSignDocument, parameters);
			SignatureValueDTO signatureValue = sign(token, signer, toBeSigned);
			signedDocument = signTrustedList(toSignDocument, parameters, signatureValue);

		} else {
			RemoteSignatureParameters parameters = buildParameters(signer);

			ToBeSigned toBeSigned = getDataToSign(toSignDocument, parameters);
			SignatureValueDTO signatureValue = sign(token, signer, toBeSigned);
			signedDocument = signDocument(toSignDocument, parameters, signatureValue);
		}

		updateProgress(100, 100);

		return signedDocument;
	}

	private RemoteSignatureParameters buildParameters(DSSPrivateKeyEntry signer) {
		updateProgress(20, 100);

		RemoteSignatureParameters parameters = new RemoteSignatureParameters();
		parameters.setAsicContainerType(model.getAsicContainerType());
		parameters.setDigestAlgorithm(model.getDigestAlgorithm());
		parameters.setSignatureLevel(model.getSignatureLevel());
		parameters.setSignaturePackaging(model.getSignaturePackaging());
		RemoteBLevelParameters bLevelParams = new RemoteBLevelParameters();
		bLevelParams.setSigningDate(new Date());
		parameters.setBLevelParams(bLevelParams);
		parameters.setSigningCertificate(new RemoteCertificate(signer.getCertificate().getEncoded()));
		parameters.setEncryptionAlgorithm(signer.getEncryptionAlgorithm());
		CertificateToken[] certificateChain = signer.getCertificateChain();
		if (Utils.isArrayNotEmpty(certificateChain)) {
			List<RemoteCertificate> certificateChainList = new ArrayList<>();
			for (CertificateToken certificateToken : certificateChain) {
				certificateChainList.add(new RemoteCertificate(certificateToken.getEncoded()));
			}
			parameters.setCertificateChain(certificateChainList);
		}
		if (isXmlManifestSigning()) {
			parameters.setManifestSignature(true);
		}
		if (SignatureForm.JAdES.equals(model.getSignatureForm())) {
			parameters.setJwsSerializationType(JWSSerializationType.JSON_SERIALIZATION); // allow extension
			parameters.setSigDMechanism(SigDMechanism.OBJECT_ID_BY_URI_HASH); // to be used by default
		}

		return parameters;
	}

	private RemoteTrustedListSignatureParameters buildTrustedListParameters(DSSPrivateKeyEntry signer) {
		updateProgress(20, 100);

		RemoteTrustedListSignatureParameters parameters = new RemoteTrustedListSignatureParameters();
		RemoteBLevelParameters bLevelParams = new RemoteBLevelParameters();
		bLevelParams.setSigningDate(new Date());
		parameters.setBLevelParameters(bLevelParams);

		parameters.setSigningCertificate(new RemoteCertificate(signer.getCertificate().getEncoded()));
		parameters.setReferenceDigestAlgorithm(model.getDigestAlgorithm());

		return parameters;
	}

	private boolean isTLSigning(FileDocument toBeSigned) {
		SignatureOption signatureOption = model.getSignatureOption();
		if (SignatureOption.TL_SIGNING.equals(signatureOption)) {
			if (DomUtils.isDOM(toBeSigned)) {
				Document document = DomUtils.buildDOM(toBeSigned);
				Element documentElement = document.getDocumentElement();
				if (TRUSTED_LIST_PARENT_ELEMENT.equals(documentElement.getLocalName()) &&
						TRUSTED_LIST_NAMESPACE.equals(documentElement.getNamespaceURI())) {
					List<String> errors = DSSXMLUtils.validateAgainstXSD(TrustedListUtils.getInstance(), new DOMSource(document));
					if (Utils.isCollectionEmpty(errors)) {
						return true;
					} else {
						throwException(String.format("The provided file is not a valid Trusted List! %s", errors.toString()), null);
					}
				} else {
					throwException("The provided file is not a Trusted List!", null);
				}
			} else {
				throwException("The provided file is not an XML!", null);
			}
		}
		return false;
	}

	private boolean isXmlManifestSigning() {
		SignatureOption signatureOption = model.getSignatureOption();
		if (SignatureOption.XML_MANIFEST_SIGNING.equals(signatureOption)) {
			FileDocument fileToSign = new FileDocument(model.getFileToSign());
			if (DomUtils.isDOM(fileToSign)) {
				Element document = DomUtils.buildDOM(fileToSign).getDocumentElement();
				if (XMLDSigElement.MANIFEST.isSameTagName(document.getLocalName()) &&
						XAdESNamespaces.XMLDSIG.isSameUri(document.getNamespaceURI())) {
					List<String> errors = DSSXMLUtils.validateAgainstXSD(XmlDSigUtils.getInstance(), new DOMSource(document));
					if (Utils.isCollectionEmpty(errors)) {
						return true;
					} else {
						throwException(String.format("The provided file is not a valid XML Manifest! %s", errors.toString()), null);
					}
					return true;
				} else {
					throwException("The provided file is not an XML Manifest!", null);
				}
			} else {
				throwException("The provided file is not an XML!", null);
			}
		}
		return false;
	}

	private ToBeSigned getDataToSign(RemoteDocument toSignDocument, RemoteSignatureParameters parameters) {
		updateProgress(25, 100);
		ToBeSigned toBeSigned = null;
		try {
			toBeSigned = DTOConverter.toToBeSigned(service.getDataToSign(toSignDocument, parameters));
		} catch (Exception e) {
			throwException("Unable to compute the digest to sign", e);
		}
		return toBeSigned;
	}

	private ToBeSigned getDataToSignTrustedList(RemoteDocument toSignDocument, RemoteTrustedListSignatureParameters parameters) {
		updateProgress(25, 100);
		ToBeSigned toBeSigned = null;
		try {
			toBeSigned = DTOConverter.toToBeSigned(trustedListSignatureService.getDataToSign(toSignDocument, parameters));
		} catch (Exception e) {
			throwException("Unable to compute the digest to sign", e);
		}
		return toBeSigned;
	}

	private SignatureValueDTO sign(SignatureTokenConnection token, DSSPrivateKeyEntry signer, ToBeSigned toBeSigned) {
		updateProgress(50, 100);
		SignatureValue signatureValue = null;
		try {
			signatureValue = token.sign(toBeSigned, model.getDigestAlgorithm(), signer);
		} catch (Exception e) {
			throwException("Unable to sign the digest", e);
		}
		return new SignatureValueDTO(signatureValue.getAlgorithm(), signatureValue.getValue());
	}

	private DSSDocument signDocument(RemoteDocument toSignDocument, RemoteSignatureParameters parameters,
									 SignatureValueDTO signatureValue) {
		updateProgress(75, 100);
		DSSDocument signDocument = null;
		try {
			signDocument = RemoteDocumentConverter.toDSSDocument(
					service.signDocument(toSignDocument, parameters, signatureValue));
		} catch (Exception e) {
			throwException("Unable to sign the document", e);
		}
		return signDocument;
	}

	private DSSDocument signTrustedList(RemoteDocument toSignDocument, RemoteTrustedListSignatureParameters parameters,
										SignatureValueDTO signatureValue) {
		updateProgress(75, 100);
		DSSDocument signDocument = null;
		try {
			signDocument = RemoteDocumentConverter.toDSSDocument(
					trustedListSignatureService.signDocument(toSignDocument, parameters, signatureValue));
		} catch (Exception e) {
			throwException("Unable to sign the document", e);
		}
		return signDocument;
	}

	private DSSPrivateKeyEntry getSigner(List<DSSPrivateKeyEntry> keys) throws Exception {
		DSSPrivateKeyEntry selectedKey = null;
		if (Utils.isCollectionEmpty(keys)) {
			throwException("No certificate found", null);
		} else if (Utils.collectionSize(keys) == 1) {
			selectedKey = keys.get(0);
		} else {
			FutureTask<DSSPrivateKeyEntry> future = new FutureTask<>(new SelectCertificateTask(keys));
			Platform.runLater(future);
			selectedKey = future.get();
			if (selectedKey == null) {
				throwException("No selected certificate", null);
			}
		}
		return selectedKey;
	}

	private SignatureTokenConnection getToken(SignatureModel model) throws IOException {
		switch (model.getTokenType()) {
		case PKCS11:
			return new Pkcs11SignatureToken(model.getPkcsFile().getAbsolutePath(), new PasswordProtection(model.getPassword().toCharArray()));
		case PKCS12:
			return new Pkcs12SignatureToken(model.getPkcsFile(), new PasswordProtection(model.getPassword().toCharArray()));
		case MSCAPI:
			return new MSCAPISignatureToken();
		default:
			throw new IllegalArgumentException("Unsupported token type " + model.getTokenType());
		}
	}

	private void throwException(String message, Exception e) {
		String exceptionMessage = message + ((e != null) ? " : " + e.getMessage() : "");
		updateMessage(exceptionMessage);
		failed();
		updateProgress(0, 100);
		throw new ApplicationException(exceptionMessage, e);
	}

}
