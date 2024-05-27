package eu.europa.esig.dss.web.controller;

import eu.europa.esig.dss.enumerations.TokenExtractionStrategy;
import eu.europa.esig.dss.model.DSSException;
import eu.europa.esig.dss.model.x509.CertificateToken;
import eu.europa.esig.dss.service.http.commons.SSLCertificateLoader;
import eu.europa.esig.dss.spi.validation.CertificateVerifier;
import eu.europa.esig.dss.spi.validation.CertificateVerifierBuilder;
import eu.europa.esig.dss.spi.x509.CommonCertificateSource;
import eu.europa.esig.dss.utils.Utils;
import eu.europa.esig.dss.validation.CertificateValidator;
import eu.europa.esig.dss.validation.reports.CertificateReports;
import eu.europa.esig.dss.web.exception.InternalServerException;
import eu.europa.esig.dss.web.model.QwacValidationForm;
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
import java.util.List;

@Controller
@RequestMapping(value = "/qwac-validation")
public class QwacValidationController extends AbstractValidationController {

	private static final Logger LOG = LoggerFactory.getLogger(QwacValidationController.class);

	private static final String VALIDATION_TILE = "qwac-validation";
	private static final String VALIDATION_RESULT_TILE = "validation-result";
	
	private static final String[] ALLOWED_FIELDS = { "url", "includeCertificateTokens", "includeRevocationTokens" };
	
	@Autowired
	protected SSLCertificateLoader sslCertificateLoader;

	@Autowired
	private Resource defaultCertificateValidationPolicy;
	
	@InitBinder
	public void setAllowedFields(WebDataBinder webDataBinder) {
		webDataBinder.setAllowedFields(ALLOWED_FIELDS);
	}

	@RequestMapping(method = RequestMethod.GET)
	public String showValidationForm(Model model, HttpServletRequest request) {
		QwacValidationForm qwacValidationForm = new QwacValidationForm();
		model.addAttribute("qwacValidationForm", qwacValidationForm);
		return VALIDATION_TILE;
	}

	@RequestMapping(method = RequestMethod.POST)
	public String validate(@ModelAttribute("qwacValidationForm") @Valid QwacValidationForm qwacValidationForm, BindingResult result, Model model) {
		if (result.hasErrors()) {
			if (LOG.isDebugEnabled()) {
				List<ObjectError> allErrors = result.getAllErrors();
				for (ObjectError error : allErrors) {
					LOG.debug(error.getDefaultMessage());
				}
			}
			return VALIDATION_TILE;
		}

		String url = qwacValidationForm.getUrl();
		
		LOG.info("Start QWAC validation. Requested URL : '{}'", url);

		List<CertificateToken> certificates = sslCertificateLoader.getCertificates(url);
		
		if (Utils.isCollectionNotEmpty(certificates)) {
			CertificateVerifier cv = new CertificateVerifierBuilder(certificateVerifier).buildCompleteCopy();
	        
	        CommonCertificateSource adjunctCertificateSource = new CommonCertificateSource();
	        for (CertificateToken certificateToken : certificates) {
	        	adjunctCertificateSource.addCertificate(certificateToken);
	        }
	        cv.addAdjunctCertSources(adjunctCertificateSource);
			
			// first certificate shall be the peer's own certificate
			CertificateToken qwacCertificate = certificates.iterator().next();
			CertificateValidator certificateValidator = CertificateValidator.fromCertificate(qwacCertificate);
			certificateValidator.setCertificateVerifier(cv);
			certificateValidator.setTokenExtractionStrategy(TokenExtractionStrategy.fromParameters(qwacValidationForm.isIncludeCertificateTokens(), false,
					qwacValidationForm.isIncludeRevocationTokens(), false));

			CertificateReports reports = null;
			if (defaultCertificateValidationPolicy != null) {
				try (InputStream is = defaultCertificateValidationPolicy.getInputStream()) {
					reports = certificateValidator.validate(is);
				} catch (IOException e) {
					throw new InternalServerException(String.format("Unable to parse policy: %s", e.getMessage()), e);
				}
			} else {
				throw new IllegalStateException("Validation policy is not correctly initialized!");
			}

			model.addAttribute("currentCertificate", qwacCertificate.getDSSIdAsString());
			setAttributesModels(model, reports);

			LOG.info("End certificate validation");

			return VALIDATION_RESULT_TILE;
		}

		throw new DSSException(String.format("The requested URL '%s' did not return a list of certificates to validate.", url));
	}

	@ModelAttribute("displayDownloadPdf")
	public boolean isDisplayDownloadPdf() {
		return true;
	}

}
