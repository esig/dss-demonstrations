package eu.europa.esig.dss.web.service;

import eu.europa.esig.dss.AbstractSignatureParameters;
import eu.europa.esig.dss.asic.cades.ASiCWithCAdESSignatureParameters;
import eu.europa.esig.dss.asic.cades.ASiCWithCAdESTimestampParameters;
import eu.europa.esig.dss.asic.cades.signature.ASiCWithCAdESService;
import eu.europa.esig.dss.asic.common.ASiCUtils;
import eu.europa.esig.dss.asic.xades.ASiCWithXAdESSignatureParameters;
import eu.europa.esig.dss.asic.xades.signature.ASiCWithXAdESService;
import eu.europa.esig.dss.cades.CAdESSignatureParameters;
import eu.europa.esig.dss.cades.signature.CAdESCounterSignatureParameters;
import eu.europa.esig.dss.cades.signature.CAdESService;
import eu.europa.esig.dss.enumerations.ASiCContainerType;
import eu.europa.esig.dss.enumerations.JWSSerializationType;
import eu.europa.esig.dss.enumerations.SigDMechanism;
import eu.europa.esig.dss.enumerations.SignatureAlgorithm;
import eu.europa.esig.dss.enumerations.SignatureForm;
import eu.europa.esig.dss.enumerations.SignaturePackaging;
import eu.europa.esig.dss.jades.JAdESSignatureParameters;
import eu.europa.esig.dss.jades.signature.JAdESCounterSignatureParameters;
import eu.europa.esig.dss.jades.signature.JAdESService;
import eu.europa.esig.dss.model.DSSDocument;
import eu.europa.esig.dss.model.DSSException;
import eu.europa.esig.dss.model.DigestDocument;
import eu.europa.esig.dss.model.SerializableCounterSignatureParameters;
import eu.europa.esig.dss.model.SignatureValue;
import eu.europa.esig.dss.model.ToBeSigned;
import eu.europa.esig.dss.model.x509.CertificateToken;
import eu.europa.esig.dss.pades.PAdESSignatureParameters;
import eu.europa.esig.dss.pades.PAdESTimestampParameters;
import eu.europa.esig.dss.pades.signature.PAdESService;
import eu.europa.esig.dss.signature.CounterSignatureService;
import eu.europa.esig.dss.signature.DocumentSignatureService;
import eu.europa.esig.dss.signature.MultipleDocumentsSignatureService;
import eu.europa.esig.dss.spi.DSSUtils;
import eu.europa.esig.dss.spi.x509.tsp.TSPSource;
import eu.europa.esig.dss.utils.Utils;
import eu.europa.esig.dss.validation.timestamp.TimestampToken;
import eu.europa.esig.dss.web.WebAppUtils;
import eu.europa.esig.dss.web.exception.ApplicationJsonRequestException;
import eu.europa.esig.dss.web.model.AbstractSignatureForm;
import eu.europa.esig.dss.web.model.CounterSignatureForm;
import eu.europa.esig.dss.web.model.ExtensionForm;
import eu.europa.esig.dss.web.model.SignatureDigestForm;
import eu.europa.esig.dss.web.model.SignatureDocumentForm;
import eu.europa.esig.dss.web.model.SignatureJAdESForm;
import eu.europa.esig.dss.web.model.SignatureMultipleDocumentsForm;
import eu.europa.esig.dss.web.model.TimestampForm;
import eu.europa.esig.dss.x509.tsp.MockTSPSource;
import eu.europa.esig.dss.xades.XAdESSignatureParameters;
import eu.europa.esig.dss.xades.signature.XAdESCounterSignatureParameters;
import eu.europa.esig.dss.xades.signature.XAdESService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.xml.bind.DatatypeConverter;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

@Component
public class SigningService {

	private static final Logger LOG = LoggerFactory.getLogger(SigningService.class);

	@Autowired
	private CAdESService cadesService;

	@Autowired
	private PAdESService padesService;

	@Autowired
	private XAdESService xadesService;

	@Autowired
	private JAdESService jadesService;

	@Autowired
	private ASiCWithCAdESService asicWithCAdESService;

	@Autowired
	private ASiCWithXAdESService asicWithXAdESService;

	@Autowired
	private TSPSource tspSource;

	public boolean isMockTSPSourceUsed() {
		return tspSource instanceof MockTSPSource;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public DSSDocument extend(ExtensionForm extensionForm) {
		LOG.info("Start extend signature");

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

		DSSDocument extendDocument = service.extendDocument(signedDocument, parameters);
		LOG.info("End extend signature");
		return extendDocument;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public ToBeSigned getDataToSign(SignatureDocumentForm form) {
		LOG.info("Start getDataToSign with one document");
		DocumentSignatureService service = getSignatureService(form.getContainerType(), form.getSignatureForm());

		AbstractSignatureParameters parameters = fillParameters(form);

		try {
			DSSDocument toSignDocument = WebAppUtils.toDSSDocument(form.getDocumentToSign());
			ToBeSigned toBeSigned = service.getDataToSign(toSignDocument, parameters);
			LOG.info("End getDataToSign with one document");
			return toBeSigned;
		} catch (Exception e) {
			throw new ApplicationJsonRequestException(e.getMessage());
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public ToBeSigned getDataToSign(SignatureDigestForm form) {
		LOG.info("Start getDataToSign with one digest");
		DocumentSignatureService service = getSignatureService(null, form.getSignatureForm());

		AbstractSignatureParameters parameters = fillParameters(form);

		try {
			DigestDocument toSignDigest = new DigestDocument(form.getDigestAlgorithm(), form.getDigestToSign(), form.getDocumentName());
			ToBeSigned toBeSigned = service.getDataToSign(toSignDigest, parameters);
			LOG.info("End getDataToSign with one digest");
			return toBeSigned;
		} catch (Exception e) {
			throw new ApplicationJsonRequestException(e.getMessage());
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public ToBeSigned getDataToSign(SignatureMultipleDocumentsForm form) {
		LOG.info("Start getDataToSign with multiple documents");
		MultipleDocumentsSignatureService service = getASiCSignatureService(form.getSignatureForm());

		AbstractSignatureParameters parameters = fillParameters(form);

		try {
			List<DSSDocument> toSignDocuments = WebAppUtils.toDSSDocuments(form.getDocumentsToSign());
			ToBeSigned toBeSigned = service.getDataToSign(toSignDocuments, parameters);
			LOG.info("End getDataToSign with multiple documents");
			return toBeSigned;
		} catch (Exception e) {
			throw new ApplicationJsonRequestException(e.getMessage());
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public ToBeSigned getDataToSign(SignatureJAdESForm form) {
		LOG.info("Start getDataToSign with one JAdES");
		
		MultipleDocumentsSignatureService service = jadesService;
		JAdESSignatureParameters parameters = fillParameters(form);

		try {
			List<DSSDocument> toSignDocuments = WebAppUtils.toDSSDocuments(form.getDocumentsToSign());
			ToBeSigned toBeSigned = service.getDataToSign(toSignDocuments, parameters);
				
			LOG.info("End getDataToSign with one JAdES");
			return toBeSigned;
		} catch (Exception e) {
			throw new ApplicationJsonRequestException(e.getMessage());
		}
	}    
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
    public ToBeSigned getDataToCounterSign(CounterSignatureForm form) {
        LOG.info("Start getDataToSign with one document");

        try {
	        DSSDocument signatureDocument = WebAppUtils.toDSSDocument(form.getDocumentToCounterSign());
			boolean zip = ASiCUtils.isZip(signatureDocument);
	        
			CounterSignatureService service = getCounterSignatureService(zip, form.getSignatureForm());
	        SerializableCounterSignatureParameters parameters = fillParameters(form);
	
	        ToBeSigned toBeSigned = service.getDataToBeCounterSigned(signatureDocument, parameters);
	
	        LOG.info("End getDataToSign with one document");
	        return toBeSigned;
		} catch (Exception e) {
			throw new ApplicationJsonRequestException(e.getMessage());
		}
    }

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public TimestampToken getContentTimestamp(SignatureDocumentForm form) {
		LOG.info("Start getContentTimestamp with one document");

		DocumentSignatureService service = getSignatureService(form.getContainerType(), form.getSignatureForm());
		AbstractSignatureParameters parameters = fillParameters(form);
		
		DSSDocument toSignDocument = WebAppUtils.toDSSDocument(form.getDocumentToSign());
		TimestampToken contentTimestamp = service.getContentTimestamp(toSignDocument, parameters);

		LOG.info("End getContentTimestamp with one document");
		return contentTimestamp;
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public TimestampToken getContentTimestamp(SignatureDigestForm form) {
		LOG.info("Start getContentTimestamp with one digest");

		DocumentSignatureService service = getSignatureService(null, form.getSignatureForm());
		AbstractSignatureParameters parameters = fillParameters(form);

		DigestDocument toSignDigest = new DigestDocument(form.getDigestAlgorithm(), form.getDigestToSign(), form.getDocumentName());
		TimestampToken contentTimestamp = service.getContentTimestamp(toSignDigest, parameters);

		LOG.info("End getContentTimestamp with one digest");
		return contentTimestamp;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public TimestampToken getContentTimestamp(SignatureMultipleDocumentsForm form) {
		LOG.info("Start getContentTimestamp with multiple documents");

		MultipleDocumentsSignatureService service = getASiCSignatureService(form.getSignatureForm());
		AbstractSignatureParameters parameters = fillParameters(form);

		TimestampToken contentTimestamp = service.getContentTimestamp(WebAppUtils.toDSSDocuments(form.getDocumentsToSign()), parameters);

		LOG.info("End getContentTimestamp with  multiple documents");
		return contentTimestamp;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public TimestampToken getContentTimestamp(SignatureJAdESForm form) {
		LOG.info("Start getContentTimestamp with JAdES");

		MultipleDocumentsSignatureService service = jadesService;
		JAdESSignatureParameters parameters = fillParameters(form);
		List<DSSDocument> toSignDocuments = WebAppUtils.toDSSDocuments(form.getDocumentsToSign());

		TimestampToken contentTimestamp = service.getContentTimestamp(toSignDocuments, parameters);

		LOG.info("End getContentTimestamp with JAdES");
		return contentTimestamp;
	}

	public DSSDocument timestamp(TimestampForm form) {
		List<DSSDocument> dssDocuments = WebAppUtils.toDSSDocuments(form.getOriginalFiles());

		LOG.info("Start timestamp with {} document(s)", dssDocuments.size());

		DSSDocument result = null;
		ASiCContainerType containerType = form.getContainerType();
		if (containerType == null) {
			if (dssDocuments.size() > 1) {
				throw new DSSException("Only one document is allowed for PAdES");
			}
			DSSDocument toTimestampDocument = dssDocuments.get(0);
			result = padesService.timestamp(toTimestampDocument, new PAdESTimestampParameters());
		} else {
			ASiCWithCAdESTimestampParameters parameters = new ASiCWithCAdESTimestampParameters();
			parameters.aSiC().setContainerType(containerType);
			result = asicWithCAdESService.timestamp(dssDocuments, parameters);
		}

		LOG.info("End timestamp with {} document(s)", dssDocuments.size());
		return result;
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
	
	private AbstractSignatureParameters fillParameters(SignatureDigestForm form) {
		AbstractSignatureParameters parameters = getSignatureParameters(null, form.getSignatureForm());
		parameters.setSignaturePackaging(SignaturePackaging.DETACHED);

		fillParameters(parameters, form);

		return parameters;
	}
	
	private JAdESSignatureParameters fillParameters(SignatureJAdESForm form) {
		JAdESSignatureParameters parameters = new JAdESSignatureParameters();
		parameters.setSignaturePackaging(form.getSignaturePackaging());
		parameters.setJwsSerializationType(form.getJwsSerializationType());
		parameters.setSigDMechanism(form.getSigDMechanism());
		parameters.setBase64UrlEncodedPayload(form.isBase64UrlEncodedPayload());
		parameters.setBase64UrlEncodedEtsiUComponents(form.isBase64UrlEncodedEtsiU());

		fillParameters(parameters, form);
		
		return parameters;
	}
	
    private SerializableCounterSignatureParameters fillParameters(CounterSignatureForm form) {
        SerializableCounterSignatureParameters parameters = getCounterSignatureParameters(form.getSignatureForm());
        parameters.setSignatureIdToCounterSign(form.getSignatureIdToCounterSign());

        if (parameters instanceof AbstractSignatureParameters) {
            fillParameters((AbstractSignatureParameters) parameters, form);
        }

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
			List<CertificateToken> certificateChain = new LinkedList<>();
			for (String base64Certificate : base64CertificateChain) {
				certificateChain.add(DSSUtils.loadCertificateFromBase64EncodedString(base64Certificate));
			}
			parameters.setCertificateChain(certificateChain);
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public DSSDocument signDocument(SignatureDocumentForm form) {
		LOG.info("Start signDocument with one document");
		DocumentSignatureService service = getSignatureService(form.getContainerType(), form.getSignatureForm());

		AbstractSignatureParameters parameters = fillParameters(form);

		try {
			DSSDocument toSignDocument = WebAppUtils.toDSSDocument(form.getDocumentToSign());
			SignatureAlgorithm sigAlgorithm = SignatureAlgorithm.getAlgorithm(form.getEncryptionAlgorithm(), form.getDigestAlgorithm());
			SignatureValue signatureValue = new SignatureValue(sigAlgorithm, Utils.fromBase64(form.getBase64SignatureValue()));
			DSSDocument signedDocument = service.signDocument(toSignDocument, parameters, signatureValue);
			LOG.info("End signDocument with one document");
			return signedDocument;
		} catch (Exception e) {
			throw new ApplicationJsonRequestException(e.getMessage());
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public DSSDocument signDigest(SignatureDigestForm form) {
		LOG.info("Start signDigest with one digest");
		DocumentSignatureService service = getSignatureService(null, form.getSignatureForm());

		AbstractSignatureParameters parameters = fillParameters(form);

		try {
			DigestDocument toSignDigest = new DigestDocument(form.getDigestAlgorithm(), form.getDigestToSign(), form.getDocumentName());
			SignatureAlgorithm sigAlgorithm = SignatureAlgorithm.getAlgorithm(form.getEncryptionAlgorithm(), form.getDigestAlgorithm());
			SignatureValue signatureValue = new SignatureValue(sigAlgorithm, Utils.fromBase64(form.getBase64SignatureValue()));
			DSSDocument signedDocument = service.signDocument(toSignDigest, parameters, signatureValue);
			LOG.info("End signDigest with one digest");
			return signedDocument;
		} catch (Exception e) {
			throw new ApplicationJsonRequestException(e.getMessage());
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public DSSDocument signDocument(SignatureMultipleDocumentsForm form) {
		LOG.info("Start signDocument with multiple documents");
		MultipleDocumentsSignatureService service = getASiCSignatureService(form.getSignatureForm());

		AbstractSignatureParameters parameters = fillParameters(form);

		try {
			List<DSSDocument> toSignDocuments = WebAppUtils.toDSSDocuments(form.getDocumentsToSign());
			SignatureAlgorithm sigAlgorithm = SignatureAlgorithm.getAlgorithm(form.getEncryptionAlgorithm(), form.getDigestAlgorithm());
			SignatureValue signatureValue = new SignatureValue(sigAlgorithm, Utils.fromBase64(form.getBase64SignatureValue()));
			DSSDocument signedDocument = service.signDocument(toSignDocuments, parameters, signatureValue);
			LOG.info("End signDocument with multiple documents");
			return signedDocument;
		} catch (Exception e) {
			throw new ApplicationJsonRequestException(e.getMessage());
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public DSSDocument signDocument(SignatureJAdESForm form) {
		LOG.info("Start signDocument with JAdES");
		
		MultipleDocumentsSignatureService service = jadesService;
		JAdESSignatureParameters parameters = fillParameters(form);

		try {
			List<DSSDocument> toSignDocuments = WebAppUtils.toDSSDocuments(form.getDocumentsToSign());
			SignatureAlgorithm sigAlgorithm = SignatureAlgorithm.getAlgorithm(form.getEncryptionAlgorithm(), form.getDigestAlgorithm());
			SignatureValue signatureValue = new SignatureValue(sigAlgorithm, DatatypeConverter.parseBase64Binary(form.getBase64SignatureValue()));
			DSSDocument signedDocument = service.signDocument(toSignDocuments, parameters, signatureValue);
	
			LOG.info("End signDocument with JAdES");
			return signedDocument;
		} catch (Exception e) {
			throw new ApplicationJsonRequestException(e.getMessage());
		}
	}
	
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public DSSDocument counterSignSignature(CounterSignatureForm form) {
        LOG.info("Start signDigest with one digest");

        try {
	        DSSDocument signatureDocument = WebAppUtils.toDSSDocument(form.getDocumentToCounterSign());
			boolean zip = ASiCUtils.isZip(signatureDocument);
	        
			CounterSignatureService service = getCounterSignatureService(zip, form.getSignatureForm());
	        SerializableCounterSignatureParameters parameters = fillParameters(form);
	
	        SignatureAlgorithm sigAlgorithm = SignatureAlgorithm.getAlgorithm(form.getEncryptionAlgorithm(), form.getDigestAlgorithm());
	        SignatureValue signatureValue = new SignatureValue(sigAlgorithm, DatatypeConverter.parseBase64Binary(form.getBase64SignatureValue()));
	        DSSDocument signedDocument = service.counterSignSignature(signatureDocument, parameters, signatureValue);
	
	        LOG.info("End signDocument with one document");
	        return signedDocument;
		} catch (Exception e) {
			throw new ApplicationJsonRequestException(e.getMessage());
		}
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
			case JAdES:
				service = jadesService;
				break;
			default:
				LOG.error("Unknow signature form : " + signatureForm);
			}
		}
		return service;
	}
	
    @SuppressWarnings("rawtypes")
	private CounterSignatureService getCounterSignatureService(boolean isZipContainer, SignatureForm signatureForm) {
        CounterSignatureService service = null;
		if (isZipContainer) {
            service = (CounterSignatureService) getASiCSignatureService(signatureForm);
        } else {
            switch (signatureForm) {
            case CAdES:
                service = cadesService;
                break;
            case XAdES:
                service = xadesService;
                break;
            case JAdES:
                service = jadesService;
                break;
            default:
                throw new DSSException("Not supported signature form for a counter signature : " + signatureForm);
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
				padesParams.setContentSize(9472 * 2); // double reserved space for signature
				parameters = padesParams;
				break;
			case XAdES:
				parameters = new XAdESSignatureParameters();
				break;
			case JAdES:
				JAdESSignatureParameters jadesParameters = new JAdESSignatureParameters();
				jadesParameters.setJwsSerializationType(JWSSerializationType.JSON_SERIALIZATION); // to allow T+ levels + parallel signing
	            jadesParameters.setSigDMechanism(SigDMechanism.OBJECT_ID_BY_URI_HASH); // to use by default
				parameters = jadesParameters;
				break;
			default:
				LOG.error("Unknown signature form : " + signatureForm);
			}
		}
		return parameters;
	}
	
    private SerializableCounterSignatureParameters getCounterSignatureParameters(SignatureForm signatureForm) {
        SerializableCounterSignatureParameters parameters = null;
        switch (signatureForm) {
            case CAdES:
                parameters = new CAdESCounterSignatureParameters();
                break;
            case XAdES:
                parameters = new XAdESCounterSignatureParameters();
                break;
            case JAdES:
            	JAdESCounterSignatureParameters jadesCounterSignatureParameters = new JAdESCounterSignatureParameters();
	            jadesCounterSignatureParameters.setJwsSerializationType(JWSSerializationType.FLATTENED_JSON_SERIALIZATION);
	            parameters = jadesCounterSignatureParameters;
                break;
            default:
                LOG.error("Not supported form for a counter signature : " + signatureForm);
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
			LOG.error("Unknow signature form : " + signatureForm);
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
			LOG.error("Unknow signature form for ASiC container: " + signatureForm);
		}
		return parameters;
	}

}
