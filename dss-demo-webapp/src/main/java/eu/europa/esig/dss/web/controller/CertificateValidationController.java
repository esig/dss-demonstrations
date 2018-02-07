package eu.europa.esig.dss.web.controller;

import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.multipart.MultipartFile;

import eu.europa.esig.dss.DSSException;
import eu.europa.esig.dss.DSSUtils;
import eu.europa.esig.dss.utils.Utils;
import eu.europa.esig.dss.validation.CertificateValidator;
import eu.europa.esig.dss.validation.CertificateVerifier;
import eu.europa.esig.dss.validation.reports.CertificateReports;
import eu.europa.esig.dss.web.model.CertificateValidationForm;
import eu.europa.esig.dss.web.service.XSLTService;
import eu.europa.esig.dss.x509.CertificateSource;
import eu.europa.esig.dss.x509.CertificateToken;
import eu.europa.esig.dss.x509.CommonCertificateSource;

@Controller
@SessionAttributes({ "simpleReportXml", "detailedReportXml" })
@RequestMapping(value = "/certificate-validation")
public class CertificateValidationController {

	private static final Logger LOG = LoggerFactory.getLogger(CertificateValidationController.class);

	private static final String VALIDATION_TILE = "certificate_validation";
	private static final String VALIDATION_RESULT_TILE = "validation_result";

	private static final String SIMPLE_REPORT_ATTRIBUTE = "simpleReportXml";
	private static final String DETAILED_REPORT_ATTRIBUTE = "detailedReportXml";

	@Autowired
	private CertificateVerifier certificateVerifier;

	@Autowired
	private XSLTService xsltService;

	// @Autowired
	// private FOPService fopService;

	@RequestMapping(method = RequestMethod.GET)
	public String showValidationForm(Model model, HttpServletRequest request) {
		model.addAttribute("certValidationForm", new CertificateValidationForm());
		return VALIDATION_TILE;
	}

	@RequestMapping(method = RequestMethod.POST)
	public String validate(@ModelAttribute("certValidationForm") @Valid CertificateValidationForm certValidationForm, BindingResult result, Model model) {
		if (result.hasErrors()) {
			return VALIDATION_TILE;
		}

		CertificateToken certificate = getCertificate(certValidationForm.getCertificateFile());

		List<MultipartFile> certificateChainFiles = certValidationForm.getCertificateChainFiles();
		if (Utils.isCollectionNotEmpty(certificateChainFiles)) {
			CertificateSource adjunctCertSource = new CommonCertificateSource();
			for (MultipartFile file : certificateChainFiles) {
				CertificateToken certificateChainItem = getCertificate(file);
				if (certificateChainItem != null) {
					adjunctCertSource.addCertificate(certificateChainItem);
				}
			}
			certificateVerifier.setAdjunctCertSource(adjunctCertSource);
		}

		CertificateValidator certificateValidator = CertificateValidator.fromCertificate(certificate);
		certificateValidator.setCertificateVerifier(certificateVerifier);

		CertificateReports reports = certificateValidator.validate();

		reports.print();

		String xmlSimpleReport = reports.getXmlSimpleReport();
		model.addAttribute(SIMPLE_REPORT_ATTRIBUTE, xmlSimpleReport);
		model.addAttribute("simpleReport", xsltService.generateSimpleCertificateReport(xmlSimpleReport));

		String xmlDetailedReport = reports.getXmlDetailedReport();
		model.addAttribute(DETAILED_REPORT_ATTRIBUTE, xmlDetailedReport);
		model.addAttribute("detailedReport", xsltService.generateDetailedReport(xmlDetailedReport));

		model.addAttribute("diagnosticTree", reports.getXmlDiagnosticData());

		return VALIDATION_RESULT_TILE;
	}

	private CertificateToken getCertificate(MultipartFile file) {
		try {
			return DSSUtils.loadCertificate(file.getBytes());
		} catch (DSSException | IOException e) {
			LOG.warn("Cannot convert file to X509 Certificate", e);
			return null;
		}

	}

	// @RequestMapping(value = "/download-simple-report")
	// public void downloadSimpleReport(HttpSession session, HttpServletResponse response) {
	// try {
	// String simpleReport = (String) session.getAttribute(SIMPLE_REPORT_ATTRIBUTE);
	//
	// response.setContentType(MimeType.PDF.getMimeTypeString());
	// response.setHeader("Content-Disposition", "attachment; filename=DSS-Simple-report.pdf");
	//
	// fopService.generateSimpleReport(simpleReport, response.getOutputStream());
	// } catch (Exception e) {
	// logger.error("An error occured while generating pdf for simple report : " + e.getMessage(), e);
	// }
	// }
	//
	// @RequestMapping(value = "/download-detailed-report")
	// public void downloadDetailedReport(HttpSession session, HttpServletResponse response) {
	// try {
	// String detailedReport = (String) session.getAttribute(DETAILED_REPORT_ATTRIBUTE);
	//
	// response.setContentType(MimeType.PDF.getMimeTypeString());
	// response.setHeader("Content-Disposition", "attachment; filename=DSS-Detailed-report.pdf");
	//
	// fopService.generateDetailedReport(detailedReport, response.getOutputStream());
	// } catch (Exception e) {
	// logger.error("An error occured while generating pdf for detailed report : " + e.getMessage(), e);
	// }
	// }
	//
	// @ModelAttribute("validationLevels")
	// public ValidationLevel[] getValidationLevels() {
	// return new ValidationLevel[] { ValidationLevel.BASIC_SIGNATURES, ValidationLevel.LONG_TERM_DATA,
	// ValidationLevel.ARCHIVAL_DATA };
	// }

}