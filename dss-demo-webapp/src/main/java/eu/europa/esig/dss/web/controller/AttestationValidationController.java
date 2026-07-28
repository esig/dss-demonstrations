package eu.europa.esig.dss.web.controller;

import eu.europa.esig.dss.attestation.common.validation.DefaultAttestationDocumentValidator;
import eu.europa.esig.dss.attestation.mdoc.validation.AbstractMdocDocumentValidator;
import eu.europa.esig.dss.attestation.mdoc.validation.MdocValidationParameters;
import eu.europa.esig.dss.diagnostic.CertificateWrapper;
import eu.europa.esig.dss.diagnostic.DiagnosticData;
import eu.europa.esig.dss.diagnostic.DiagnosticDataFacade;
import eu.europa.esig.dss.diagnostic.RevocationWrapper;
import eu.europa.esig.dss.diagnostic.TimestampWrapper;
import eu.europa.esig.dss.diagnostic.jaxb.XmlDiagnosticData;
import eu.europa.esig.dss.enumerations.MimeTypeEnum;
import eu.europa.esig.dss.enumerations.RevocationType;
import eu.europa.esig.dss.enumerations.TimestampType;
import eu.europa.esig.dss.enumerations.TokenExtractionStrategy;
import eu.europa.esig.dss.enumerations.ValidationLevel;
import eu.europa.esig.dss.model.DSSException;
import eu.europa.esig.dss.model.identifier.OriginalIdentifierProvider;
import eu.europa.esig.dss.model.identifier.TokenIdentifierProvider;
import eu.europa.esig.dss.model.x509.CertificateToken;
import eu.europa.esig.dss.spi.DSSUtils;
import eu.europa.esig.dss.spi.attestation.revocation.AttestationRevocationSource;
import eu.europa.esig.dss.spi.policy.SignaturePolicyProvider;
import eu.europa.esig.dss.spi.validation.CertificateVerifier;
import eu.europa.esig.dss.spi.validation.CertificateVerifierBuilder;
import eu.europa.esig.dss.spi.x509.CertificateSource;
import eu.europa.esig.dss.spi.x509.CommonCertificateSource;
import eu.europa.esig.dss.utils.Utils;
import eu.europa.esig.dss.validation.DocumentValidator;
import eu.europa.esig.dss.validation.identifier.UserFriendlyIdentifierProvider;
import eu.europa.esig.dss.validation.policy.ValidationPolicyLoader;
import eu.europa.esig.dss.validation.reports.Reports;
import eu.europa.esig.dss.web.WebAppUtils;
import eu.europa.esig.dss.web.editor.EnumPropertyEditor;
import eu.europa.esig.dss.web.exception.InternalServerException;
import eu.europa.esig.dss.web.exception.SourceNotFoundException;
import eu.europa.esig.dss.web.model.AttestationValidationForm;
import eu.europa.esig.dss.web.service.FOPService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.List;
import java.util.Locale;

@Controller
@RequestMapping(value = "/attestation-validation")
public class AttestationValidationController extends AbstractValidationController {

	private static final Logger LOG = LoggerFactory.getLogger(AttestationValidationController.class);

	private static final String VALIDATION_TILE = "attestation-validation";
	private static final String VALIDATION_RESULT_TILE = "validation-result";

	private static final String[] ALLOWED_FIELDS = { "attestationFile", "sessionTranscript", "validationTime",
			"timezoneDifference", "defaultPolicy", "policyFile", "cryptographicSuite", "signingCertificate",
			"adjunctCertificates", "includeCertificateTokens", "includeTimestampTokens", "includeRevocationTokens",
			"includeUserFriendlyIdentifiers", "includeSemantics" };

    @Autowired
    private FOPService fopService;

    @Autowired
    private SignaturePolicyProvider signaturePolicyProvider;

    @Autowired
    private AttestationRevocationSource attestationRevocationSource;

    @Autowired
    private Resource defaultAttestationPolicy;

    @Override
    @InitBinder
    public void initBinder(WebDataBinder webDataBinder) {
        super.initBinder(webDataBinder);
        webDataBinder.registerCustomEditor(ValidationLevel.class, new EnumPropertyEditor(ValidationLevel.class));
    }

	@InitBinder
	public void setAllowedFields(WebDataBinder webDataBinder) {
		webDataBinder.setAllowedFields(ALLOWED_FIELDS);
	}

    @GetMapping
	public String showValidationForm(Model model, HttpServletRequest request) {
		AttestationValidationForm validationForm = new AttestationValidationForm();
		validationForm.setDefaultPolicy(true);
		model.addAttribute("attestationValidationForm", validationForm);
		setCryptographicSuiteSamples(model);
		return VALIDATION_TILE;
	}

    @PostMapping
	public String validate(@ModelAttribute("attestationValidationForm") @Valid AttestationValidationForm validationForm, BindingResult result,
                           Model model, HttpServletRequest request) {
		if (result.hasErrors()) {
            if (LOG.isDebugEnabled()) {
                List<ObjectError> allErrors = result.getAllErrors();
                for (ObjectError error : allErrors) {
                    LOG.debug(error.getDefaultMessage());
                }
            }
            return VALIDATION_TILE;
        }

        LOG.info("Start Attestation validation");

        DefaultAttestationDocumentValidator attestationPresentationValidator = DefaultAttestationDocumentValidator
				.fromDocument(WebAppUtils.toDSSDocument(validationForm.getAttestationFile()));
        attestationPresentationValidator.setCertificateVerifier(getCertificateVerifier(validationForm));
        attestationPresentationValidator.setTokenExtractionStrategy(TokenExtractionStrategy.fromParameters(validationForm.isIncludeCertificateTokens(),
				validationForm.isIncludeTimestampTokens(), validationForm.isIncludeRevocationTokens(), false));
        attestationPresentationValidator.setIncludeSemantics(validationForm.isIncludeSemantics());
        attestationPresentationValidator.setSignaturePolicyProvider(signaturePolicyProvider);
        attestationPresentationValidator.setValidationTime(getValidationTime(validationForm.getValidationTime(), validationForm.getTimezoneDifference()));
        attestationPresentationValidator.setAttestationRevocationSource(attestationRevocationSource);

        if ((attestationPresentationValidator instanceof AbstractMdocDocumentValidator mdocValidator)
                && (validationForm.getSessionTranscript() != null)) {
            MdocValidationParameters mdocValidationParameters = new MdocValidationParameters();
            mdocValidationParameters.setSessionTranscript(WebAppUtils.toDSSDocument(validationForm.getSessionTranscript()));
            mdocValidator.setAttestationValidationParameters(mdocValidationParameters);
        }

		TokenIdentifierProvider identifierProvider = validationForm.isIncludeUserFriendlyIdentifiers() ?
				new UserFriendlyIdentifierProvider() : new OriginalIdentifierProvider();
        attestationPresentationValidator.setTokenIdentifierProvider(identifierProvider);

		setSigningCertificate(attestationPresentationValidator, validationForm);

		Locale locale = request.getLocale();
		LOG.trace("Requested locale : {}", locale);
		if (locale == null) {
			locale = Locale.getDefault();
			LOG.warn("The request locale is null! Use the default one : {}", locale);
		}
        attestationPresentationValidator.setLocale(locale);

		Reports reports = validate(attestationPresentationValidator, validationForm);
		setAttributesModels(model, reports);

        LOG.info("End Attestation validation");

		return VALIDATION_RESULT_TILE;
	}

	private void setSigningCertificate(DocumentValidator documentValidator, AttestationValidationForm validationForm) {
		CertificateToken signingCertificate = WebAppUtils.toCertificateToken(validationForm.getSigningCertificate());
		if (signingCertificate != null) {
			CertificateSource signingCertificateSource = new CommonCertificateSource();
			signingCertificateSource.addCertificate(signingCertificate);
			documentValidator.setSigningCertificateSource(signingCertificateSource);
		}
	}

	private CertificateVerifier getCertificateVerifier(AttestationValidationForm certValidationForm) {
		CertificateSource adjunctCertSource = WebAppUtils.toCertificateSource(certValidationForm.getAdjunctCertificates());

		CertificateVerifier cv;
		if (adjunctCertSource == null) {
			// reuse the default one
			cv = certificateVerifier;
		} else {
			cv = new CertificateVerifierBuilder(certificateVerifier).buildCompleteCopy();
			cv.setAdjunctCertSources(adjunctCertSource);
		}

		return cv;
	}

	private Reports validate(DocumentValidator documentValidator, AttestationValidationForm validationForm) {
		Reports reports = null;

        Date start = new Date();
        ValidationPolicyLoader validationPolicyLoader;
        MultipartFile policyFile = validationForm.getPolicyFile();
        if (!validationForm.isDefaultPolicy() && policyFile != null && !policyFile.isEmpty()) {
            try (InputStream is = policyFile.getInputStream()) {
                validationPolicyLoader = ValidationPolicyLoader.fromValidationPolicy(is);
            } catch (IOException e) {
                throw new DSSException("Unable to load validation policy!", e);
            }
        } else if (defaultAttestationPolicy != null) {
            try (InputStream is = defaultAttestationPolicy.getInputStream()) {
                validationPolicyLoader = ValidationPolicyLoader.fromValidationPolicy(is);
            } catch (IOException e) {
                throw new InternalServerException(String.format("Unable to parse policy: %s", e.getMessage()), e);
            }
        } else {
            throw new IllegalStateException("Validation policy is not correctly initialized!");
        }

        MultipartFile cryptographicSuiteFile = validationForm.getCryptographicSuite();
        if (cryptographicSuiteFile != null && !cryptographicSuiteFile.isEmpty()) {
            try (InputStream is = cryptographicSuiteFile.getInputStream()) {
                validationPolicyLoader = validationPolicyLoader.withCryptographicSuite(is);
            } catch (IOException e) {
                throw new DSSException("Unable to load cryptographic suite!", e);
            }
        }

        reports = documentValidator.validateDocument(validationPolicyLoader.create());

		Date end = new Date();
	    long duration = end.getTime() - start.getTime();
		LOG.info("Validation process duration : {}ms", duration);

		return reports;
	}

	@RequestMapping(value = "/download-simple-report")
	public void downloadSimpleReport(HttpSession session, HttpServletResponse response) {
		final String simpleReport = (String) session.getAttribute(XML_SIMPLE_REPORT_ATTRIBUTE);
		final String simpleCertificateReport = (String) session.getAttribute(XML_SIMPLE_CERTIFICATE_REPORT_ATTRIBUTE);
		if (Utils.isStringNotEmpty(simpleReport)) {
			try {
				response.setContentType(MimeTypeEnum.PDF.getMimeTypeString());
				response.setHeader("Content-Disposition", "attachment; filename=DSS-Simple-report.pdf");
				fopService.generateSimpleReport(simpleReport, response.getOutputStream());
			} catch (Exception e) {
				LOG.error("An error occurred while generating pdf for simple report : " + e.getMessage(), e);
			}
		} else if (Utils.isStringNotEmpty(simpleCertificateReport)) {
			try {
				response.setContentType(MimeTypeEnum.PDF.getMimeTypeString());
				response.setHeader("Content-Disposition", "attachment; filename=DSS-Simple-certificate-report.pdf");
				fopService.generateSimpleCertificateReport(simpleCertificateReport, response.getOutputStream());
			} catch (Exception e) {
				LOG.error("An error occurred while generating pdf for simple certificate report : " + e.getMessage(), e);
			}
		} else {
			throw new SourceNotFoundException("Simple report not found");
		}
	}

	@RequestMapping(value = "/download-detailed-report")
	public void downloadDetailedReport(HttpSession session, HttpServletResponse response) {
		final String detailedReport = (String) session.getAttribute(XML_DETAILED_REPORT_ATTRIBUTE);
		if (detailedReport == null) {
			throw new SourceNotFoundException("Detailed report not found");
		}
		try {
			response.setContentType(MimeTypeEnum.PDF.getMimeTypeString());
			response.setHeader("Content-Disposition", "attachment; filename=DSS-Detailed-report.pdf");
			fopService.generateDetailedReport(detailedReport, response.getOutputStream());
		} catch (Exception e) {
			LOG.error("An error occurred while generating pdf for detailed report : " + e.getMessage(), e);
		}
	}

	@RequestMapping(value = "/download-diagnostic-data")
    public void downloadDiagnosticData(HttpSession session, HttpServletResponse response) {
        String diagnosticData = (String) session.getAttribute(XML_DIAGNOSTIC_DATA_ATTRIBUTE);
		if (diagnosticData == null) {
			throw new SourceNotFoundException("Diagnostic data not found");
		}

		try (InputStream is = new ByteArrayInputStream(diagnosticData.getBytes());
			 OutputStream os = response.getOutputStream()) {
			response.setContentType(MimeTypeEnum.XML.getMimeTypeString());
			response.setHeader("Content-Disposition", "attachment; filename=DSS-Diagnostic-data.xml");
			Utils.copy(is, os);

		} catch (IOException e) {
			LOG.error("An error occurred while downloading diagnostic data : " + e.getMessage(), e);
		}
    }

	@RequestMapping(value = "/diag-data.svg")
	public @ResponseBody ResponseEntity<String> downloadSVG(HttpSession session, HttpServletResponse response) {
		String diagnosticData = (String) session.getAttribute(XML_DIAGNOSTIC_DATA_ATTRIBUTE);
		if (diagnosticData == null) {
			throw new SourceNotFoundException("Diagnostic data not found");
		}

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.valueOf(MimeTypeEnum.SVG.getMimeTypeString()));
		ResponseEntity<String> svgEntity = new ResponseEntity<>(xsltService.generateSVG(diagnosticData), headers,
				HttpStatus.OK);
		return svgEntity;
	}

    @RequestMapping(value = "/download-certificate")
    public void downloadCertificate(@RequestParam(value = "id") String id, HttpSession session, HttpServletResponse response) {
        DiagnosticData diagnosticData = getDiagnosticData(session);
        CertificateWrapper certificate = diagnosticData.getUsedCertificateById(id);
        if (certificate == null) {
            String message = "Certificate " + id + " not found";
            LOG.warn(message);
            throw new SourceNotFoundException(message);
        }
        String pemCert = DSSUtils.convertToPEM(DSSUtils.loadCertificate(certificate.getBinaries()));
		String filename = DSSUtils.getNormalizedString(certificate.getReadableCertificateName()) + ".cer";

        addTokenToResponse(response, filename, pemCert.getBytes());
    }

    @RequestMapping(value = "/download-revocation")
    public void downloadRevocationData(@RequestParam(value = "id") String id, @RequestParam(value = "format") String format, HttpSession session,
            HttpServletResponse response) {
        DiagnosticData diagnosticData = getDiagnosticData(session);
        RevocationWrapper revocationData = diagnosticData.getRevocationById(id);
        if (revocationData == null) {
            String message = "Revocation data " + id + " not found";
            LOG.warn(message);
            throw new SourceNotFoundException(message);
        }
        String filename = revocationData.getId();
        byte[] binaries;

        if (RevocationType.CRL.equals(revocationData.getRevocationType())) {
            filename += ".crl";

            if (Utils.areStringsEqualIgnoreCase(format, "pem")) {
                String pem = "-----BEGIN CRL-----\n";
                pem += Utils.toBase64(revocationData.getBinaries());
                pem += "\n-----END CRL-----";
                binaries = pem.getBytes();
            } else {
            	binaries = revocationData.getBinaries();
            }
        } else {
            filename += ".ocsp";
            binaries = revocationData.getBinaries();
        }

        addTokenToResponse(response, filename, binaries);
    }

    @RequestMapping(value = "/download-timestamp")
    public void downloadTimestamp(@RequestParam(value = "id") String id, @RequestParam(value = "format") String format, HttpSession session,
            HttpServletResponse response) {
        DiagnosticData diagnosticData = getDiagnosticData(session);
        TimestampWrapper timestamp = diagnosticData.getTimestampById(id);
        if (timestamp == null) {
            String message = "Timestamp " + id + " not found";
            LOG.warn(message);
            throw new SourceNotFoundException(message);
        }
        TimestampType type = timestamp.getType();

        byte[] binaries;
        if (Utils.areStringsEqualIgnoreCase(format, "pem")) {
            String pem = "-----BEGIN TIMESTAMP-----\n";
            pem += Utils.toBase64(timestamp.getBinaries());
            pem += "\n-----END TIMESTAMP-----";
            binaries = pem.getBytes();
        } else {
        	binaries = timestamp.getBinaries();
        }

        String filename = type.name() + ".tst";
        addTokenToResponse(response, filename, binaries);
    }

    protected DiagnosticData getDiagnosticData(HttpSession session) {
        String diagnosticDataXml = (String) session.getAttribute(XML_DIAGNOSTIC_DATA_ATTRIBUTE);
        if (diagnosticDataXml == null) {
			throw new SourceNotFoundException("No found diagnostic data");
		}
        try {
            XmlDiagnosticData xmlDiagData = DiagnosticDataFacade.newFacade().unmarshall(diagnosticDataXml);
            return new DiagnosticData(xmlDiagData);
        } catch (Exception e) {
        	LOG.error("An error occurred while generating DiagnosticData from XML : " + e.getMessage(), e);
        }
        return null;
    }

    protected void addTokenToResponse(HttpServletResponse response, String filename, byte[] binaries) {
    	response.setContentType(MimeTypeEnum.TST.getMimeTypeString());
        response.setHeader("Content-Disposition", "attachment; filename=" + filename);
        try (InputStream is = new ByteArrayInputStream(binaries); OutputStream os = response.getOutputStream()) {
            Utils.copy(is, os);
        } catch (IOException e) {
            LOG.error("An error occurred while downloading a file : " + e.getMessage(), e);
        }
    }

	@ModelAttribute("displayDownloadPdf")
	public boolean isDisplayDownloadPdf() {
		return true;
	}

}