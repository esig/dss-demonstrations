package eu.europa.esig.dss.web.controller;

import eu.europa.esig.dss.enumerations.TokenExtractionStrategy;
import eu.europa.esig.dss.model.DSSException;
import eu.europa.esig.dss.model.x509.CertificateToken;
import eu.europa.esig.dss.spi.DSSUtils;
import eu.europa.esig.dss.spi.x509.CertificateSource;
import eu.europa.esig.dss.validation.CertificateValidator;
import eu.europa.esig.dss.validation.CertificateVerifier;
import eu.europa.esig.dss.validation.CertificateVerifierBuilder;
import eu.europa.esig.dss.validation.OriginalIdentifierProvider;
import eu.europa.esig.dss.validation.TokenIdentifierProvider;
import eu.europa.esig.dss.validation.UserFriendlyIdentifierProvider;
import eu.europa.esig.dss.validation.reports.CertificateReports;
import eu.europa.esig.dss.web.WebAppUtils;
import eu.europa.esig.dss.web.model.CertificateForm;
import eu.europa.esig.dss.web.model.CertificateValidationForm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.SessionAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

@Controller
@SessionAttributes({ "simpleReportXml", "detailedReportXml", "diagnosticDataXml" })
@RequestMapping(value = "/certificate-validation")
public class CertificateValidationController extends AbstractValidationController {

	private static final Logger LOG = LoggerFactory.getLogger(CertificateValidationController.class);

	private static final String VALIDATION_TILE = "certificate-validation";
	private static final String VALIDATION_RESULT_TILE = "validation-result";
	
	private static final String[] ALLOWED_FIELDS = { "certificateForm.certificateFile", "certificateForm.certificateBase64", "certificateChainFiles",
			"validationTime", "includeCertificateTokens", "includeRevocationTokens", "includeUserFriendlyIdentifiers" };

	@InitBinder
	public void initBinder(WebDataBinder webDataBinder) {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
		dateFormat.setLenient(false);
		webDataBinder.registerCustomEditor(Date.class, new CustomDateEditor(dateFormat, true));
	}
	
	@InitBinder
	public void setAllowedFields(WebDataBinder webDataBinder) {
		webDataBinder.setAllowedFields(ALLOWED_FIELDS);
	}

	@RequestMapping(method = RequestMethod.GET)
	public String showValidationForm(Model model, HttpServletRequest request) {
		CertificateValidationForm certificateValidationForm = new CertificateValidationForm();
		certificateValidationForm.setValidationTime(new Date());
		model.addAttribute("certValidationForm", certificateValidationForm);
		return VALIDATION_TILE;
	}

	@RequestMapping(method = RequestMethod.POST)
	public String validate(@ModelAttribute("certValidationForm") @Valid CertificateValidationForm certValidationForm, 
			BindingResult result, Model model, HttpServletRequest request) {
		if (result.hasErrors()) {
			if (LOG.isDebugEnabled()) {
				List<ObjectError> allErrors = result.getAllErrors();
				for (ObjectError error : allErrors) {
					LOG.debug(error.getDefaultMessage());
				}
			}
			return VALIDATION_TILE;
		}

		CertificateToken certificate = getCertificate(certValidationForm.getCertificateForm());

		LOG.trace("Start certificate validation");

		CertificateValidator certificateValidator = CertificateValidator.fromCertificate(certificate);
		certificateValidator.setCertificateVerifier(getCertificateVerifier(certValidationForm));
		certificateValidator.setTokenExtractionStrategy(
				TokenExtractionStrategy.fromParameters(certValidationForm.isIncludeCertificateTokens(), false, certValidationForm.isIncludeRevocationTokens()));
		certificateValidator.setValidationTime(certValidationForm.getValidationTime());

		TokenIdentifierProvider identifierProvider = certValidationForm.isIncludeUserFriendlyIdentifiers() ?
				new UserFriendlyIdentifierProvider() : new OriginalIdentifierProvider();
		certificateValidator.setTokenIdentifierProvider(identifierProvider);

		Locale locale = request.getLocale();
		LOG.trace("Requested locale : {}", request.getLocale());
		if (locale == null) {
			locale = Locale.getDefault();
			LOG.warn("The request locale is null! Use the default one : {}", locale);
		}
		certificateValidator.setLocale(locale);

		CertificateReports reports = certificateValidator.validate();

		// reports.print();

		model.addAttribute("currentCertificate", identifierProvider.getIdAsString(certificate));
		setAttributesModels(model, reports);

		return VALIDATION_RESULT_TILE;
	}
	
	private CertificateToken getCertificate(CertificateForm certificateForm) {
		CertificateToken certificateToken = WebAppUtils.toCertificateToken(certificateForm.getCertificateFile());
		if (certificateToken == null) {
			certificateToken = DSSUtils.loadCertificateFromBase64EncodedString(certificateForm.getCertificateBase64());
			if (certificateToken == null) {
				throw new DSSException("Cannot convert base64 to a CertificateToken!");
			}
		}
		return certificateToken;
	}
	
	private CertificateVerifier getCertificateVerifier(CertificateValidationForm certValidationForm) {
		CertificateSource adjunctCertSource = WebAppUtils.toCertificateSource(certValidationForm.getCertificateChainFiles());
	
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
	
	@ModelAttribute("displayDownloadPdf")
	public boolean isDisplayDownloadPdf() {
		return false;
	}

}