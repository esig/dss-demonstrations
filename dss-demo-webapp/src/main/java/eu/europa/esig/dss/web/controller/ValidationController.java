package eu.europa.esig.dss.web.controller;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

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

import eu.europa.esig.dss.DSSDocument;
import eu.europa.esig.dss.MimeType;
import eu.europa.esig.dss.utils.Utils;
import eu.europa.esig.dss.validation.CertificateVerifier;
import eu.europa.esig.dss.validation.SignedDocumentValidator;
import eu.europa.esig.dss.validation.executor.ValidationLevel;
import eu.europa.esig.dss.validation.reports.Reports;
import eu.europa.esig.dss.validation.reports.wrapper.DiagnosticData;
import eu.europa.esig.dss.web.WebAppUtils;
import eu.europa.esig.dss.web.editor.EnumPropertyEditor;
import eu.europa.esig.dss.web.model.ValidationForm;
import eu.europa.esig.dss.web.service.FOPService;

@Controller
@SessionAttributes({ "simpleReportXml", "detailedReportXml", "diagnosticTreeObject" })
@RequestMapping(value = "/validation")
public class ValidationController extends AbstractValidationController {

	private static final Logger logger = LoggerFactory.getLogger(ValidationController.class);

	private static final String VALIDATION_TILE = "validation";
	private static final String VALIDATION_RESULT_TILE = "validation_result";

	@Autowired
	private CertificateVerifier certificateVerifier;

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
		
		setSignatureValidationAttributesModel(model, reports);

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
		setCertificateResponse(id, diagnosticData, response);
	}
	
	@RequestMapping(value = "/download-revocation")
	public void downloadRevocationData(@RequestParam(value="id") String id, @RequestParam(value="format") String format, HttpSession session, HttpServletResponse response) {
		DiagnosticData diagnosticData = (DiagnosticData) session.getAttribute(DIAGNOSTIC_DATA);
		setRevocationResponse(id, format, diagnosticData, response);
	}
	
	@RequestMapping(value = "/download-timestamp")
	public void downloadTimestamp(@RequestParam(value="id") String id, @RequestParam(value="format") String format, HttpSession session, HttpServletResponse response) {
		DiagnosticData diagnosticData = (DiagnosticData) session.getAttribute(DIAGNOSTIC_DATA);
		setTimestampResponse(id, format, diagnosticData, response);
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