package eu.europa.esig.dss.web.service;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import eu.europa.esig.dss.ASiCContainerType;
import eu.europa.esig.dss.AbstractSignatureParameters;
import eu.europa.esig.dss.DSSDocument;
import eu.europa.esig.dss.DSSUtils;
import eu.europa.esig.dss.SignatureAlgorithm;
import eu.europa.esig.dss.SignatureForm;
import eu.europa.esig.dss.SignatureValue;
import eu.europa.esig.dss.ToBeSigned;
import eu.europa.esig.dss.asic.ASiCWithCAdESSignatureParameters;
import eu.europa.esig.dss.asic.ASiCWithXAdESSignatureParameters;
import eu.europa.esig.dss.asic.signature.ASiCWithCAdESService;
import eu.europa.esig.dss.asic.signature.ASiCWithXAdESService;
import eu.europa.esig.dss.cades.CAdESSignatureParameters;
import eu.europa.esig.dss.cades.signature.CAdESService;
import eu.europa.esig.dss.pades.PAdESSignatureParameters;
import eu.europa.esig.dss.pades.signature.PAdESService;
import eu.europa.esig.dss.signature.DocumentSignatureService;
import eu.europa.esig.dss.signature.MultipleDocumentsSignatureService;
import eu.europa.esig.dss.utils.Utils;
import eu.europa.esig.dss.validation.TimestampToken;
import eu.europa.esig.dss.web.WebAppUtils;
import eu.europa.esig.dss.web.model.AbstractSignatureForm;
import eu.europa.esig.dss.web.model.ExtensionForm;
import eu.europa.esig.dss.web.model.SignatureDocumentForm;
import eu.europa.esig.dss.web.model.SignatureMultipleDocumentsForm;
import eu.europa.esig.dss.x509.CertificateToken;
import eu.europa.esig.dss.xades.XAdESSignatureParameters;
import eu.europa.esig.dss.xades.signature.XAdESService;

@Component
public class SigningService {

	private static final Logger logger = LoggerFactory.getLogger(SigningService.class);

	@Autowired
	private CAdESService cadesService;

	@Autowired
	private PAdESService padesService;

	@Autowired
	private XAdESService xadesService;

	@Autowired
	private ASiCWithCAdESService asicWithCAdESService;

	@Autowired
	private ASiCWithXAdESService asicWithXAdESService;

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public DSSDocument extend(ExtensionForm extensionForm) {

		ASiCContainerType containerType = extensionForm.getContainerType();
		SignatureForm signatureForm = extensionForm.getSignatureForm();

		DSSDocument signedDocument = WebAppUtils.toDSSDocument(extensionForm.getSignedFile());
		List<DSSDocument> originalDocuments = WebAppUtils.toDSSDocuments(extensionForm.getOriginalFiles());

		DocumentSignatureService service = getSignatureService(containerType, signatureForm);

		AbstractSignatureParameters parameters = getSignatureParameters(containerType, signatureForm);
		parameters.setSignatureLevel(extensionForm.getSignatureLevel());

		if (Utils.isCollectionNotEmpty(originalDocuments)) {
			parameters.setDetachedContents(originalDocuments);
		}

		DSSDocument extendedDoc = (DSSDocument) service.extendDocument(signedDocument, parameters);
		return extendedDoc;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public ToBeSigned getDataToSign(SignatureDocumentForm form) {
		logger.info("Start getDataToSign with one document");
		DocumentSignatureService service = getSignatureService(form.getContainerType(), form.getSignatureForm());

		AbstractSignatureParameters parameters = fillParameters(form);

		ToBeSigned toBeSigned = null;
		try {
			DSSDocument toSignDocument = WebAppUtils.toDSSDocument(form.getDocumentToSign());
			toBeSigned = service.getDataToSign(toSignDocument, parameters);
		} catch (Exception e) {
			logger.error("Unable to execute getDataToSign : " + e.getMessage(), e);
		}
		logger.info("End getDataToSign with one document");
		return toBeSigned;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public ToBeSigned getDataToSign(SignatureMultipleDocumentsForm form) {
		logger.info("Start getDataToSign with multiple documents");
		MultipleDocumentsSignatureService service = getASiCSignatureService(form.getSignatureForm());

		AbstractSignatureParameters parameters = fillParameters(form);

		ToBeSigned toBeSigned = null;
		try {
			List<DSSDocument> toSignDocuments = WebAppUtils.toDSSDocuments(form.getDocumentsToSign());
			toBeSigned = service.getDataToSign(toSignDocuments, parameters);
		} catch (Exception e) {
			logger.error("Unable to execute getDataToSign : " + e.getMessage(), e);
		}
		logger.info("End getDataToSign with multiple documents");
		return toBeSigned;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public TimestampToken getContentTimestamp(SignatureDocumentForm form) {
		logger.info("Start getContentTimestamp with one document");

		DocumentSignatureService service = getSignatureService(form.getContainerType(), form.getSignatureForm());
		AbstractSignatureParameters parameters = fillParameters(form);
		DSSDocument toSignDocument = WebAppUtils.toDSSDocument(form.getDocumentToSign());

		TimestampToken contentTimestamp = service.getContentTimestamp(toSignDocument, parameters);

		logger.info("End getContentTimestamp with one document");
		return contentTimestamp;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public TimestampToken getContentTimestamp(SignatureMultipleDocumentsForm form) {
		logger.info("Start getContentTimestamp with multiple documents");

		MultipleDocumentsSignatureService service = getASiCSignatureService(form.getSignatureForm());
		AbstractSignatureParameters parameters = fillParameters(form);

		TimestampToken contentTimestamp = service.getContentTimestamp(WebAppUtils.toDSSDocuments(form.getDocumentsToSign()), parameters);

		logger.info("End getContentTimestamp with  multiple documents");
		return contentTimestamp;
	}

	private AbstractSignatureParameters fillParameters(SignatureMultipleDocumentsForm form) {
		AbstractSignatureParameters finalParameters = getASiCSignatureParameters(form.getContainerType(), form.getSignatureForm());

		fillParameters(finalParameters, form);

		return finalParameters;
	}

	private AbstractSignatureParameters fillParameters(SignatureDocumentForm form) {
		AbstractSignatureParameters parameters = getSignatureParameters(form.getContainerType(), form.getSignatureForm());
		parameters.setSignaturePackaging(form.getSignaturePackaging());

		fillParameters(parameters, form);

		return parameters;
	}

	private void fillParameters(AbstractSignatureParameters parameters, AbstractSignatureForm form) {
		parameters.setSignatureLevel(form.getSignatureLevel());
		parameters.setDigestAlgorithm(form.getDigestAlgorithm());
		// parameters.setEncryptionAlgorithm(form.getEncryptionAlgorithm()); retrieved from certificate
		parameters.bLevel().setSigningDate(form.getSigningDate());

		parameters.setSignWithExpiredCertificate(form.isSignWithExpiredCertificate());

		if (form.getContentTimestamp() != null) {
			parameters.setContentTimestamps(Arrays.asList(WebAppUtils.toTimestampToken(form.getContentTimestamp())));
		}

		CertificateToken signingCertificate = DSSUtils.loadCertificateFromBase64EncodedString(form.getBase64Certificate());
		parameters.setSigningCertificate(signingCertificate);

		List<String> base64CertificateChain = form.getBase64CertificateChain();
		if (Utils.isCollectionNotEmpty(base64CertificateChain)) {
			List<CertificateToken> certificateChain = new LinkedList<CertificateToken>();
			for (String base64Certificate : base64CertificateChain) {
				certificateChain.add(DSSUtils.loadCertificateFromBase64EncodedString(base64Certificate));
			}
			parameters.setCertificateChain(certificateChain);
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public DSSDocument signDocument(SignatureDocumentForm form) {
		logger.info("Start signDocument with one document");
		DocumentSignatureService service = getSignatureService(form.getContainerType(), form.getSignatureForm());

		AbstractSignatureParameters parameters = fillParameters(form);

		DSSDocument signedDocument = null;
		try {
			DSSDocument toSignDocument = WebAppUtils.toDSSDocument(form.getDocumentToSign());
			SignatureAlgorithm sigAlgorithm = SignatureAlgorithm.getAlgorithm(form.getEncryptionAlgorithm(), form.getDigestAlgorithm());
			SignatureValue signatureValue = new SignatureValue(sigAlgorithm, Utils.fromBase64(form.getBase64SignatureValue()));
			signedDocument = (DSSDocument) service.signDocument(toSignDocument, parameters, signatureValue);
		} catch (Exception e) {
			logger.error("Unable to execute signDocument : " + e.getMessage(), e);
		}
		logger.info("End signDocument with one document");
		return signedDocument;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public DSSDocument signDocument(SignatureMultipleDocumentsForm form) {
		logger.info("Start signDocument with multiple documents");
		MultipleDocumentsSignatureService service = getASiCSignatureService(form.getSignatureForm());

		AbstractSignatureParameters parameters = fillParameters(form);

		DSSDocument signedDocument = null;
		try {
			List<DSSDocument> toSignDocuments = WebAppUtils.toDSSDocuments(form.getDocumentsToSign());
			SignatureAlgorithm sigAlgorithm = SignatureAlgorithm.getAlgorithm(form.getEncryptionAlgorithm(), form.getDigestAlgorithm());
			SignatureValue signatureValue = new SignatureValue(sigAlgorithm, Utils.fromBase64(form.getBase64SignatureValue()));
			signedDocument = (DSSDocument) service.signDocument(toSignDocuments, parameters, signatureValue);
		} catch (Exception e) {
			logger.error("Unable to execute signDocument : " + e.getMessage(), e);
		}
		logger.info("End signDocument with multiple documents");
		return signedDocument;
	}

	@SuppressWarnings("rawtypes")
	private DocumentSignatureService getSignatureService(ASiCContainerType containerType, SignatureForm signatureForm) {
		DocumentSignatureService service = null;
		if (containerType != null) {
			service = (DocumentSignatureService) getASiCSignatureService(signatureForm);
		} else {
			switch (signatureForm) {
			case CAdES:
				service = cadesService;
				break;
			case PAdES:
				service = padesService;
				break;
			case XAdES:
				service = xadesService;
				break;
			default:
				logger.error("Unknow signature form : " + signatureForm);
			}
		}
		return service;
	}

	private AbstractSignatureParameters getSignatureParameters(ASiCContainerType containerType, SignatureForm signatureForm) {
		AbstractSignatureParameters parameters = null;
		if (containerType != null) {
			parameters = getASiCSignatureParameters(containerType, signatureForm);
		} else {
			switch (signatureForm) {
			case CAdES:
				parameters = new CAdESSignatureParameters();
				break;
			case PAdES:
				PAdESSignatureParameters padesParams = new PAdESSignatureParameters();
				padesParams.setSignatureSize(9472 * 2); // double reserved space for signature
				parameters = padesParams;
				break;
			case XAdES:
				parameters = new XAdESSignatureParameters();
				break;
			default:
				logger.error("Unknow signature form : " + signatureForm);
			}
		}
		return parameters;
	}

	@SuppressWarnings("rawtypes")
	private MultipleDocumentsSignatureService getASiCSignatureService(SignatureForm signatureForm) {
		MultipleDocumentsSignatureService service = null;
		switch (signatureForm) {
		case CAdES:
			service = asicWithCAdESService;
			break;
		case XAdES:
			service = asicWithXAdESService;
			break;
		default:
			logger.error("Unknow signature form : " + signatureForm);
		}
		return service;
	}

	private AbstractSignatureParameters getASiCSignatureParameters(ASiCContainerType containerType, SignatureForm signatureForm) {
		AbstractSignatureParameters parameters = null;
		switch (signatureForm) {
		case CAdES:
			ASiCWithCAdESSignatureParameters asicCadesParams = new ASiCWithCAdESSignatureParameters();
			asicCadesParams.aSiC().setContainerType(containerType);
			parameters = asicCadesParams;
			break;
		case XAdES:
			ASiCWithXAdESSignatureParameters asicXadesParams = new ASiCWithXAdESSignatureParameters();
			asicXadesParams.aSiC().setContainerType(containerType);
			parameters = asicXadesParams;
			break;
		default:
			logger.error("Unknow signature form for ASiC container: " + signatureForm);
		}
		return parameters;
	}

}
