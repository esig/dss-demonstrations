package eu.europa.esig.dss.web.controller;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;

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
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.SessionAttributes;

import eu.europa.esig.dss.diagnostic.CertificateWrapper;
import eu.europa.esig.dss.diagnostic.DiagnosticData;
import eu.europa.esig.dss.diagnostic.DiagnosticDataFacade;
import eu.europa.esig.dss.diagnostic.RevocationWrapper;
import eu.europa.esig.dss.diagnostic.TimestampWrapper;
import eu.europa.esig.dss.diagnostic.jaxb.XmlDiagnosticData;
import eu.europa.esig.dss.enumerations.DigestAlgorithm;
import eu.europa.esig.dss.enumerations.RevocationType;
import eu.europa.esig.dss.enumerations.TimestampType;
import eu.europa.esig.dss.enumerations.TokenExtractionStrategy;
import eu.europa.esig.dss.model.DSSDocument;
import eu.europa.esig.dss.model.MimeType;
import eu.europa.esig.dss.model.x509.CertificateToken;
import eu.europa.esig.dss.spi.DSSUtils;
import eu.europa.esig.dss.spi.x509.CertificateSource;
import eu.europa.esig.dss.spi.x509.CommonCertificateSource;
import eu.europa.esig.dss.utils.Utils;
import eu.europa.esig.dss.validation.CertificateVerifier;
import eu.europa.esig.dss.validation.CertificateVerifierBuilder;
import eu.europa.esig.dss.validation.DocumentValidator;
import eu.europa.esig.dss.validation.SignedDocumentValidator;
import eu.europa.esig.dss.validation.executor.ValidationLevel;
import eu.europa.esig.dss.validation.reports.Reports;
import eu.europa.esig.dss.web.WebAppUtils;
import eu.europa.esig.dss.web.editor.EnumPropertyEditor;
import eu.europa.esig.dss.web.exception.SourceNotFoundException;
import eu.europa.esig.dss.web.model.ValidationForm;
import eu.europa.esig.dss.web.service.FOPService;

@Controller
@SessionAttributes({ "simpleReportXml", "detailedReportXml", "diagnosticDataXml" })
@RequestMapping(value = "/validation")
public class ValidationController extends AbstractValidationController {

	private static final Logger LOG = LoggerFactory.getLogger(ValidationController.class);

	private static final String VALIDATION_TILE = "validation";
	private static final String VALIDATION_RESULT_TILE = "validation-result";

	private static final String[] ALLOWED_FIELDS = { "signedFile", "originalFiles[*].*", "digestToSend", "validationLevel", "defaultPolicy",
			"policyFile", "signingCertificate", "adjunctCertificates", "includeCertificateTokens", "includeTimestampTokens", "includeRevocationTokens",
			"includeSemantics" };

	@Autowired
	private FOPService fopService;

	@Autowired
	private Resource defaultPolicy;

	@InitBinder
	public void initBinder(WebDataBinder webDataBinder) {
		webDataBinder.registerCustomEditor(ValidationLevel.class, new EnumPropertyEditor(ValidationLevel.class));
	}

	@InitBinder
	public void setAllowedFields(WebDataBinder webDataBinder) {
		webDataBinder.setAllowedFields(ALLOWED_FIELDS);
	}

	@RequestMapping(method = RequestMethod.GET)
	public String showValidationForm(Model model, HttpServletRequest request) {
		ValidationForm validationForm = new ValidationForm();
		validationForm.setValidationLevel(ValidationLevel.ARCHIVAL_DATA);
		validationForm.setDefaultPolicy(true);
		model.addAttribute("validationForm", validationForm);
		return VALIDATION_TILE;
	}

	@RequestMapping(method = RequestMethod.POST)
	public String validate(@ModelAttribute("validationForm") @Valid ValidationForm validationForm, BindingResult result,
						   Model model, HttpServletRequest request) {
		LOG.trace("Validation BEGINS...");
		if (result.hasErrors()) {
			if (LOG.isDebugEnabled()) {
				List<ObjectError> allErrors = result.getAllErrors();
				for (ObjectError error : allErrors) {
					LOG.debug(error.getDefaultMessage());
				}
			}
			return VALIDATION_TILE;
		}

		SignedDocumentValidator documentValidator = SignedDocumentValidator
				.fromDocument(WebAppUtils.toDSSDocument(validationForm.getSignedFile()));
		documentValidator.setCertificateVerifier(getCertificateVerifier(validationForm));
		documentValidator.setTokenExtractionStrategy(TokenExtractionStrategy.fromParameters(validationForm.isIncludeCertificateTokens(),
				validationForm.isIncludeTimestampTokens(), validationForm.isIncludeRevocationTokens()));
		documentValidator.setIncludeSemantics(validationForm.isIncludeSemantics());

		setSigningCertificate(documentValidator, validationForm);
		setDetachedContents(documentValidator, validationForm);

		Locale locale = request.getLocale();
		LOG.trace("Requested locale : {}", locale);
		if (locale == null) {
			locale = Locale.getDefault();
			LOG.warn("The request locale is null! Use the default one : {}", locale);
		}
		documentValidator.setLocale(locale);

		Reports reports = validate(documentValidator, validationForm);
		setAttributesModels(model, reports);

		return VALIDATION_RESULT_TILE;
	}

	private void setDetachedContents(DocumentValidator documentValidator, ValidationForm validationForm) {
		List<DSSDocument> originalFiles = WebAppUtils.originalFilesToDSSDocuments(validationForm.getOriginalFiles());
		if (Utils.isCollectionNotEmpty(originalFiles)) {
			documentValidator.setDetachedContents(originalFiles);
		}
		documentValidator.setValidationLevel(validationForm.getValidationLevel());
	}

	private void setSigningCertificate(DocumentValidator documentValidator, ValidationForm validationForm) {
		CertificateToken signingCertificate = WebAppUtils.toCertificateToken(validationForm.getSigningCertificate());
		if (signingCertificate != null) {
			CertificateSource signingCertificateSource = new CommonCertificateSource();
			signingCertificateSource.addCertificate(signingCertificate);
			documentValidator.setSigningCertificateSource(signingCertificateSource);
		}
	}

	private CertificateVerifier getCertificateVerifier(ValidationForm certValidationForm) {
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

	private Reports validate(DocumentValidator documentValidator, ValidationForm validationForm) {
		Reports reports = null;

		Date start = new Date();
		DSSDocument policyFile = WebAppUtils.toDSSDocument(validationForm.getPolicyFile());
		if (!validationForm.isDefaultPolicy() && (policyFile != null)) {
			try (InputStream is = policyFile.openStream()) {
				reports = documentValidator.validateDocument(is);
			} catch (IOException e) {
				LOG.error(e.getMessage(), e);
			}
		} else if (defaultPolicy != null) {
			try (InputStream is = defaultPolicy.getInputStream()) {
				reports = documentValidator.validateDocument(is);
			} catch (IOException e) {
				LOG.error("Unable to parse policy : " + e.getMessage(), e);
			}
		} else {
			LOG.error("Not correctly initialized");
		}

		Date end = new Date();
		long duration = end.getTime() - start.getTime();
		LOG.info("Validation process duration : {}ms", duration);

		return reports;
	}

	@RequestMapping(value = "/download-simple-report")
	public void downloadSimpleReport(HttpSession session, HttpServletResponse response) {
		try {
			String simpleReport = (String) session.getAttribute(XML_SIMPLE_REPORT_ATTRIBUTE);

			response.setContentType(MimeType.PDF.getMimeTypeString());
			response.setHeader("Content-Disposition", "attachment; filename=DSS-Simple-report.pdf");

			fopService.generateSimpleReport(simpleReport, response.getOutputStream());
		} catch (Exception e) {
			LOG.error("An error occurred while generating pdf for simple report : " + e.getMessage(), e);
		}
	}

	@RequestMapping(value = "/download-detailed-report")
	public void downloadDetailedReport(HttpSession session, HttpServletResponse response) {
		try {
			String detailedReport = (String) session.getAttribute(XML_DETAILED_REPORT_ATTRIBUTE);

			response.setContentType(MimeType.PDF.getMimeTypeString());
			response.setHeader("Content-Disposition", "attachment; filename=DSS-Detailed-report.pdf");

			fopService.generateDetailedReport(detailedReport, response.getOutputStream());
		} catch (Exception e) {
			LOG.error("An error occurred while generating pdf for detailed report : " + e.getMessage(), e);
		}
	}

	@RequestMapping(value = "/download-diagnostic-data")
	public void downloadDiagnosticData(HttpSession session, HttpServletResponse response) {
		String report = (String) session.getAttribute(XML_DIAGNOSTIC_DATA_ATTRIBUTE);

		response.setContentType(MimeType.XML.getMimeTypeString());
		response.setHeader("Content-Disposition", "attachment; filename=DSS-Diagnotic-data.xml");
		try {
			Utils.write(report.getBytes(StandardCharsets.UTF_8), response.getOutputStream());
		} catch (IOException e) {
			LOG.error("An error occurred while downloading diagnostic data : " + e.getMessage(), e);
		}
	}

	@RequestMapping(value = "/diag-data.svg")
	public @ResponseBody ResponseEntity<String> downloadSVG(HttpSession session, HttpServletResponse response) {
		String report = (String) session.getAttribute(XML_DIAGNOSTIC_DATA_ATTRIBUTE);

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.valueOf(MimeType.SVG.getMimeTypeString()));
		ResponseEntity<String> svgEntity = new ResponseEntity<String>(xsltService.generateSVG(report), headers,
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

		addTokenToResponse(response, filename, MimeType.CER, pemCert.getBytes());
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
		MimeType mimeType;
		byte[] binaries;

		if (RevocationType.CRL.equals(revocationData.getRevocationType())) {
			mimeType = MimeType.CRL;
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
			mimeType = MimeType.BINARY;
			filename += ".ocsp";
			binaries = revocationData.getBinaries();
		}

		addTokenToResponse(response, filename, mimeType, binaries);
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
		addTokenToResponse(response, filename, MimeType.TST, binaries);
	}

	public DiagnosticData getDiagnosticData(HttpSession session) {
		String diagnosticDataXml = (String) session.getAttribute(XML_DIAGNOSTIC_DATA_ATTRIBUTE);
		try {
			XmlDiagnosticData xmlDiagData = DiagnosticDataFacade.newFacade().unmarshall(diagnosticDataXml);
			return new DiagnosticData(xmlDiagData);
		} catch (Exception e) {
			LOG.error("An error occurred while generating DiagnosticData from XML : " + e.getMessage(), e);
		}
		return null;
	}

	protected void addTokenToResponse(HttpServletResponse response, String filename, MimeType mimeType, byte[] binaries) {
		response.setContentType(MimeType.TST.getMimeTypeString());
		response.setHeader("Content-Disposition", "attachment; filename=" + filename);
		try (InputStream is = new ByteArrayInputStream(binaries); OutputStream os = response.getOutputStream()) {
			Utils.copy(is, os);
		} catch (IOException e) {
			LOG.error("An error occurred while downloading a file : " + e.getMessage(), e);
		}
	}

	@ModelAttribute("validationLevels")
	public ValidationLevel[] getValidationLevels() {
		return new ValidationLevel[] { ValidationLevel.BASIC_SIGNATURES, ValidationLevel.LONG_TERM_DATA, ValidationLevel.ARCHIVAL_DATA };
	}

	@ModelAttribute("displayDownloadPdf")
	public boolean isDisplayDownloadPdf() {
		return true;
	}

	@ModelAttribute("digestAlgos")
	public DigestAlgorithm[] getDigestAlgorithms() {
		// see https://developer.mozilla.org/en-US/docs/Web/API/SubtleCrypto/digest
		return new DigestAlgorithm[] { DigestAlgorithm.SHA1, DigestAlgorithm.SHA256, DigestAlgorithm.SHA384,
				DigestAlgorithm.SHA512 };
	}

}