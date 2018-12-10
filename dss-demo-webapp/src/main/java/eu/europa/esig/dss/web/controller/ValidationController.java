package eu.europa.esig.dss.web.controller;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;

import eu.europa.esig.dss.DSSASN1Utils;
import eu.europa.esig.dss.DSSDocument;
import eu.europa.esig.dss.DSSUtils;
import eu.europa.esig.dss.MimeType;
import eu.europa.esig.dss.utils.Utils;
import eu.europa.esig.dss.validation.CertificateVerifier;
import eu.europa.esig.dss.validation.SignedDocumentValidator;
import eu.europa.esig.dss.validation.executor.ValidationLevel;
import eu.europa.esig.dss.validation.reports.Reports;
import eu.europa.esig.dss.validation.reports.wrapper.CertificateWrapper;
import eu.europa.esig.dss.validation.reports.wrapper.DiagnosticData;
import eu.europa.esig.dss.validation.reports.wrapper.RevocationWrapper;
import eu.europa.esig.dss.validation.reports.wrapper.TimestampWrapper;
import eu.europa.esig.dss.web.WebAppUtils;
import eu.europa.esig.dss.web.editor.EnumPropertyEditor;
import eu.europa.esig.dss.web.exception.BadRequestException;
import eu.europa.esig.dss.web.model.ValidationForm;
import eu.europa.esig.dss.web.service.FOPService;
import eu.europa.esig.dss.web.service.XSLTService;

@Controller
@SessionAttributes({ "simpleReportXml", "detailedReportXml", "diagnosticTreeObject" })
@RequestMapping(value = "/validation")
public class ValidationController {

	private static final Logger logger = LoggerFactory.getLogger(ValidationController.class);

	private static final String VALIDATION_TILE = "validation";
	private static final String VALIDATION_RESULT_TILE = "validation_result";

	private static final String SIMPLE_REPORT_ATTRIBUTE = "simpleReportXml";
	private static final String DETAILED_REPORT_ATTRIBUTE = "detailedReportXml";
	private static final String DIAGNOSTIC_DATA = "diagnosticTreeObject";

	@Autowired
	private CertificateVerifier certificateVerifier;

	@Autowired
	private XSLTService xsltService;

	@Autowired
	private FOPService fopService;

	@Autowired
	private Resource defaultPolicy;

	@InitBinder
	public void initBinder(WebDataBinder binder) {
		binder.registerCustomEditor(ValidationLevel.class, new EnumPropertyEditor(ValidationLevel.class));
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
	public String validate(@ModelAttribute("validationForm") @Valid ValidationForm validationForm, BindingResult result, Model model) {
		if (result.hasErrors()) {
			return VALIDATION_TILE;
		}

		SignedDocumentValidator documentValidator = SignedDocumentValidator.fromDocument(WebAppUtils.toDSSDocument(validationForm.getSignedFile()));
		
		CertificateVerifier cv = certificateVerifier;
		cv.setIncludeCertificateRevocationValues(validationForm.isIncludeRawRevocationData());
		cv.setIncludeTimestampTokenValues(validationForm.isIncludeRawTimestampTokens());
		documentValidator.setCertificateVerifier(cv);

		List<DSSDocument> originalFiles = WebAppUtils.toDSSDocuments(validationForm.getOriginalFiles());
		if (Utils.isCollectionNotEmpty(originalFiles)) {
			documentValidator.setDetachedContents(originalFiles);
		}
		documentValidator.setValidationLevel(validationForm.getValidationLevel());

		Reports reports = null;

		DSSDocument policyFile = WebAppUtils.toDSSDocument(validationForm.getPolicyFile());
		if (!validationForm.isDefaultPolicy() && (policyFile != null)) {
			try (InputStream is = policyFile.openStream()) {
				reports = documentValidator.validateDocument(is);
			} catch (IOException e) {
				logger.error(e.getMessage(), e);
			}
		} else if (defaultPolicy != null) {
			try (InputStream is = defaultPolicy.getInputStream()) {
				reports = documentValidator.validateDocument(is);
			} catch (IOException e) {
				logger.error("Unable to parse policy : " + e.getMessage(), e);
			}
		} else {
			logger.error("Not correctly initialized");
		}

		// reports.print();

		String xmlSimpleReport = reports.getXmlSimpleReport();
		model.addAttribute(SIMPLE_REPORT_ATTRIBUTE, xmlSimpleReport);
		model.addAttribute("simpleReport", xsltService.generateSimpleReport(xmlSimpleReport));

		String xmlDetailedReport = reports.getXmlDetailedReport();
		model.addAttribute(DETAILED_REPORT_ATTRIBUTE, xmlDetailedReport);
		model.addAttribute("detailedReport", xsltService.generateDetailedReport(xmlDetailedReport));

		DiagnosticData diagnosticData = reports.getDiagnosticData();
		model.addAttribute(DIAGNOSTIC_DATA, diagnosticData);
		model.addAttribute("diagnosticTree", reports.getXmlDiagnosticData());
		
		model.addAttribute("revocationEnabled", cv.isIncludeCertificateRevocationValues());
		if(cv.isIncludeTimestampTokenValues()) {
			Set<TimestampWrapper> allTimestamps = diagnosticData.getAllTimestamps();
			model.addAttribute("allTimestamps", allTimestamps);
		}
		List<CertificateWrapper> usedCertificates = diagnosticData.getUsedCertificates();
		model.addAttribute("usedCertificates", usedCertificates);
		
		return VALIDATION_RESULT_TILE;
	}

	@RequestMapping(value = "/download-simple-report")
	public void downloadSimpleReport(HttpSession session, HttpServletResponse response) {
		try {
			String simpleReport = (String) session.getAttribute(SIMPLE_REPORT_ATTRIBUTE);

			response.setContentType(MimeType.PDF.getMimeTypeString());
			response.setHeader("Content-Disposition", "attachment; filename=DSS-Simple-report.pdf");

			fopService.generateSimpleReport(simpleReport, response.getOutputStream());
		} catch (Exception e) {
			logger.error("An error occured while generating pdf for simple report : " + e.getMessage(), e);
		}
	}

	@RequestMapping(value = "/download-detailed-report")
	public void downloadDetailedReport(HttpSession session, HttpServletResponse response) {
		try {
			String detailedReport = (String) session.getAttribute(DETAILED_REPORT_ATTRIBUTE);

			response.setContentType(MimeType.PDF.getMimeTypeString());
			response.setHeader("Content-Disposition", "attachment; filename=DSS-Detailed-report.pdf");

			fopService.generateDetailedReport(detailedReport, response.getOutputStream());
		} catch (Exception e) {
			logger.error("An error occured while generating pdf for detailed report : " + e.getMessage(), e);
		}
	}
	
	@RequestMapping(value = "/download-certificate")
	public void downloadCertificate(@RequestParam(value="id") String id, HttpSession session, HttpServletResponse response) {
		DiagnosticData diagnosticData = (DiagnosticData) session.getAttribute(DIAGNOSTIC_DATA);
		CertificateWrapper certificate = diagnosticData.getUsedCertificateById(id);
		if(certificate == null) {
			String message = "Certificate " + id + " not found";
			logger.warn(message);
			throw new BadRequestException(message);
		}
		String pemCert = DSSUtils.convertToPEM(DSSUtils.loadCertificate(certificate.getBinaries()));
		String filename = DSSASN1Utils.getHumanReadableName(DSSUtils.loadCertificate(certificate.getBinaries())).replace(" ", "_")+".cer";
		
		response.setContentType(MimeType.CER.getMimeTypeString());
		response.setHeader("Content-Disposition", "attachment; filename="+filename);
		try {
			Utils.copy(new ByteArrayInputStream(pemCert.getBytes()), response.getOutputStream());
		} catch (IOException e) {
			logger.error("An error occured while downloading certificate : " + e.getMessage(), e);
		}
	}
	
	@RequestMapping(value = "/download-revocation")
	public void downloadRevocationData(@RequestParam(value="id") String id, @RequestParam(value="format", required=false) String format, HttpSession session, HttpServletResponse response) {
		DiagnosticData diagnosticData = (DiagnosticData) session.getAttribute(DIAGNOSTIC_DATA);
		RevocationWrapper revocationData = diagnosticData.getRevocationDataById(id);
		if(revocationData == null) {
			String message = "Revocation data " + id + " not found";
			logger.warn(message);
			throw new BadRequestException(message);
		}
		String certId = revocationData.getSigningCertificateId();
		String filename = "";
				
		CertificateWrapper cert = diagnosticData.getUsedCertificateById(certId);
		if(cert != null) {
			filename+=DSSASN1Utils.getHumanReadableName(DSSUtils.loadCertificate(cert.getBinaries()))+"_";
		}
		filename += revocationData.getSource().replace("Token", "");
		String mimeType;
		byte[] is;
		
		if(revocationData.getSource().contains("CRL")) {
			mimeType = MimeType.CRL.getMimeTypeString();
			filename += ".crl";
			
			if(Utils.areStringsEqualIgnoreCase(format ,"pem")) {
				String pem = "-----BEGIN CRL-----\n";
				pem += Utils.toBase64(revocationData.getBinaries());
				pem += "\n-----END CRL-----";
				is = pem.getBytes();
			} else {
				is = revocationData.getBinaries();
			}
		} else {
			mimeType = MimeType.BINARY.getMimeTypeString();
			filename += ".ocsp";
			is = revocationData.getBinaries();
		}
		response.setContentType(mimeType);
		response.setHeader("Content-Disposition", "attachment; filename="+filename.replace(" ", "_"));
		try {
			Utils.copy(new ByteArrayInputStream(is), response.getOutputStream());
		} catch (IOException e) {
			logger.error("An error occured while downloading revocation data : " + e.getMessage(), e);
		}
	}
	
	@RequestMapping(value = "/download-timestamp")
	public void downloadTimestamp(@RequestParam(value="id") String id, @RequestParam(value="format", required=false) String format, HttpSession session, HttpServletResponse response) {
		DiagnosticData diagnosticData = (DiagnosticData) session.getAttribute(DIAGNOSTIC_DATA);
		TimestampWrapper timestamp = diagnosticData.getTimestampById(id);
		if(timestamp == null) {
			String message = "Timestamp " + id + " not found";
			logger.warn(message);
			throw new BadRequestException(message);
		}
		String certId = timestamp.getSigningCertificateId();
		CertificateWrapper cert = diagnosticData.getUsedCertificateById(certId);
		String filename = "";
		
		if(cert != null) {
			filename+=DSSASN1Utils.getHumanReadableName(DSSUtils.loadCertificate(cert.getBinaries()))+"_";
		}
		filename += timestamp.getType();
		
		response.setContentType(MimeType.TST.getMimeTypeString());
		response.setHeader("Content-Disposition", "attachment; filename="+filename.replace(" ", "_")+".tst");
		byte[] is;
		
		if(Utils.areStringsEqualIgnoreCase(format, "pem")) {
			String pem = "-----BEGIN TIMESTAMP-----\n";
			pem += Utils.toBase64(timestamp.getBinaries());
			pem += "\n-----END TIMESTAMP-----";
			is = pem.getBytes();
		} else {
			is = timestamp.getBinaries();
		}
		
		try {
			Utils.copy(new ByteArrayInputStream(is), response.getOutputStream());
		} catch (IOException e) {
			logger.error("An error occured while downloading timestamp : " + e.getMessage(), e);
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
}