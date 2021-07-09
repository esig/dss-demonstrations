package eu.europa.esig.dss.standalone.task;

import java.io.IOException;
import java.security.KeyStore.PasswordProtection;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.FutureTask;

import eu.europa.esig.dss.model.DSSDocument;
import eu.europa.esig.dss.model.FileDocument;
import eu.europa.esig.dss.model.SignatureValue;
import eu.europa.esig.dss.model.ToBeSigned;
import eu.europa.esig.dss.model.x509.CertificateToken;
import eu.europa.esig.dss.spi.tsl.TrustedListsCertificateSource;
import eu.europa.esig.dss.standalone.RemoteDocumentSignatureServiceBuilder;
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
import eu.europa.esig.dss.ws.signature.dto.parameters.RemoteBLevelParameters;
import eu.europa.esig.dss.ws.signature.dto.parameters.RemoteSignatureParameters;
import javafx.application.Platform;
import javafx.concurrent.Task;

public class SigningTask extends Task<DSSDocument> {

	private final SignatureModel model;
	private final RemoteDocumentSignatureService service;

	public SigningTask (SignatureModel model, TrustedListsCertificateSource tslCertificateSource) {
		this.model = model;
		

		RemoteDocumentSignatureServiceBuilder builder = new RemoteDocumentSignatureServiceBuilder();
		builder.setTslCertificateSource(tslCertificateSource);
		service = builder.build();
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
		RemoteSignatureParameters parameters = buildParameters(signer);

		ToBeSigned toBeSigned = getDataToSign(toSignDocument, parameters);
		SignatureValue signatureValue = signDigest(token, signer, toBeSigned);
		DSSDocument signDocument = signDocument(toSignDocument, parameters, signatureValue);
		updateProgress(100, 100);

		return signDocument;
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

		return parameters;
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

	private SignatureValue signDigest(SignatureTokenConnection token, DSSPrivateKeyEntry signer, ToBeSigned toBeSigned) {
		updateProgress(50, 100);
		SignatureValue signatureValue = null;
		try {
			signatureValue = token.sign(toBeSigned, model.getDigestAlgorithm(), signer);
		} catch (Exception e) {
			throwException("Unable to sign the digest", e);
		}
		return signatureValue;
	}

	private DSSDocument signDocument(RemoteDocument toSignDocument, RemoteSignatureParameters parameters, SignatureValue signatureValue) {
		updateProgress(75, 100);
		DSSDocument signDocument = null;
		try {
			signDocument = RemoteDocumentConverter.toDSSDocument(service.signDocument(toSignDocument, parameters, 
					new SignatureValueDTO(signatureValue.getAlgorithm(), signatureValue.getValue())));
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
		throw new ApplicationException(exceptionMessage, e);
	}

}
