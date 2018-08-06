package eu.europa.esig.dss.web.controller;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
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
import eu.europa.esig.dss.web.exception.BadRequestException;
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

	@InitBinder
	public void initBinder(WebDataBinder webDataBinder) {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
		dateFormat.setLenient(false);
		webDataBinder.registerCustomEditor(Date.class, new CustomDateEditor(dateFormat, true));
	}

	@RequestMapping(method = RequestMethod.GET)
	public String showValidationForm(Model model, HttpServletRequest request) {
		CertificateValidationForm certificateValidationForm = new CertificateValidationForm();
		certificateValidationForm.setValidationTime(new Date());
		model.addAttribute("certValidationForm", certificateValidationForm);
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

		LOG.info("Start certificate validation");

		CertificateValidator certificateValidator = CertificateValidator.fromCertificate(certificate);
		certificateValidator.setCertificateVerifier(certificateVerifier);
		certificateValidator.setValidationTime(certValidationForm.getValidationTime());

		CertificateReports reports = certificateValidator.validate();

		// reports.print();

		String xmlSimpleReport = reports.getXmlSimpleReport();
		model.addAttribute(SIMPLE_REPORT_ATTRIBUTE, xmlSimpleReport);
		model.addAttribute("simpleReport", xsltService.generateSimpleCertificateReport(xmlSimpleReport));

		String xmlDetailedReport = reports.getXmlDetailedReport();
		model.addAttribute(DETAILED_REPORT_ATTRIBUTE, xmlDetailedReport);
		model.addAttribute("detailedReport", xsltService.generateDetailedReport(xmlDetailedReport));

		model.addAttribute("diagnosticTree", reports.getXmlDiagnosticData());

		LOG.info("End certificate validation");

		return VALIDATION_RESULT_TILE;
	}

	private CertificateToken getCertificate(MultipartFile file) {
		try {
			if (file != null && !file.isEmpty()) {
				return DSSUtils.loadCertificate(file.getBytes());
			}
		} catch (DSSException | IOException e) {
			LOG.warn("Cannot convert file to X509 Certificate", e);
			throw new BadRequestException("Unsupported certificate format for file '" + file.getOriginalFilename() + "'");
		}
		return null;
	}

	@ModelAttribute("displayDownloadPdf")
	public boolean isDisplayDownloadPdf() {
		return false;
	}

}