package eu.europa.esig.dss.web.controller;

import eu.europa.esig.dss.enumerations.TokenExtractionStrategy;
import eu.europa.esig.dss.model.DSSException;
import eu.europa.esig.dss.model.identifier.TokenIdentifierProvider;
import eu.europa.esig.dss.model.x509.CertificateToken;
import eu.europa.esig.dss.spi.DSSUtils;
import eu.europa.esig.dss.spi.x509.CertificateSource;
import eu.europa.esig.dss.validation.CertificateValidator;
import eu.europa.esig.dss.validation.CertificateVerifier;
import eu.europa.esig.dss.validation.CertificateVerifierBuilder;
import eu.europa.esig.dss.validation.OriginalIdentifierProvider;
import eu.europa.esig.dss.validation.UserFriendlyIdentifierProvider;
import eu.europa.esig.dss.validation.reports.CertificateReports;
import eu.europa.esig.dss.web.WebAppUtils;
import eu.europa.esig.dss.web.exception.InternalServerException;
import eu.europa.esig.dss.web.model.CertificateForm;
import eu.europa.esig.dss.web.model.CertificateValidationForm;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

@Controller
@RequestMapping(value = "/certificate-validation")
public class CertificateValidationController extends AbstractValidationController {

	private static final Logger LOG = LoggerFactory.getLogger(CertificateValidationController.class);

	private static final String VALIDATION_TILE = "certificate-validation";
	private static final String VALIDATION_RESULT_TILE = "validation-result";
	
	private static final String[] ALLOWED_FIELDS = { "certificateForm.certificateFile", "certificateForm.certificateBase64", "certificateChainFiles",
			"validationTime", "timezoneDifference", "includeCertificateTokens", "includeRevocationTokens", "includeUserFriendlyIdentifiers" };

	@Autowired
	private Resource defaultCertificateValidationPolicy;
	
	@InitBinder
	public void setAllowedFields(WebDataBinder webDataBinder) {
		webDataBinder.setAllowedFields(ALLOWED_FIELDS);
	}

	@RequestMapping(method = RequestMethod.GET)
	public String showValidationForm(Model model, HttpServletRequest request) {
		model.addAttribute("certValidationForm", new CertificateValidationForm());
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
		certificateValidator.setTokenExtractionStrategy(TokenExtractionStrategy.fromParameters(
				certValidationForm.isIncludeCertificateTokens(), false, certValidationForm.isIncludeRevocationTokens(), false));
		certificateValidator.setValidationTime(getValidationTime(certValidationForm));

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

		CertificateReports reports;
		if (defaultCertificateValidationPolicy != null) {
			try (InputStream is = defaultCertificateValidationPolicy.getInputStream()) {
				reports = certificateValidator.validate(is);
			} catch (IOException e) {
				throw new InternalServerException(String.format("Unable to parse policy: %s", e.getMessage()), e);
			}
		} else {
			throw new IllegalStateException("Validation policy is not correctly initialized!");
		}

		// reports.print();

		model.addAttribute("currentCertificate", identifierProvider.getIdAsString(certificate));
		setAttributesModels(model, reports);

		return VALIDATION_RESULT_TILE;
	}

	private Date getValidationTime(CertificateValidationForm validationForm) {
		if (validationForm.getValidationTime() != null) {
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(validationForm.getValidationTime());
			calendar.add(Calendar.MINUTE, validationForm.getTimezoneDifference());
			return calendar.getTime();
		}
		return null;
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
		return true;
	}

}