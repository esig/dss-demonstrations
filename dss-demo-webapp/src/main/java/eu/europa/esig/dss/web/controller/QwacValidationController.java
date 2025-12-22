package eu.europa.esig.dss.web.controller;

import eu.europa.esig.dss.enumerations.TokenExtractionStrategy;
import eu.europa.esig.dss.model.DSSException;
import eu.europa.esig.dss.model.http.ResponseEnvelope;
import eu.europa.esig.dss.model.identifier.OriginalIdentifierProvider;
import eu.europa.esig.dss.model.identifier.TokenIdentifierProvider;
import eu.europa.esig.dss.model.policy.ValidationPolicy;
import eu.europa.esig.dss.model.x509.CertificateToken;
import eu.europa.esig.dss.service.http.commons.CommonsDataLoader;
import eu.europa.esig.dss.spi.client.http.AdvancedDataLoader;
import eu.europa.esig.dss.spi.client.http.AdvancedMemoryDataLoader;
import eu.europa.esig.dss.validation.identifier.UserFriendlyIdentifierProvider;
import eu.europa.esig.dss.validation.policy.ValidationPolicyLoader;
import eu.europa.esig.dss.validation.qwac.QWACValidator;
import eu.europa.esig.dss.validation.reports.CertificateReports;
import eu.europa.esig.dss.web.WebAppUtils;
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
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serial;
import java.security.cert.Certificate;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping(value = "/qwac-validation")
public class QwacValidationController extends AbstractValidationController {

	private static final Logger LOG = LoggerFactory.getLogger(QwacValidationController.class);

	private static final String VALIDATION_TILE = "qwac-validation";
	private static final String VALIDATION_RESULT_TILE = "validation-result";

	private static final String[] ALLOWED_FIELDS = { "url", "tlsCertificate", "tlsBindingSignature", "validationTime",
			"timezoneDifference", "defaultPolicy", "policyFile", "includeCertificateTokens", "includeRevocationTokens",
			"includeUserFriendlyIdentifiers" };

	@Autowired
	protected CommonsDataLoader trustAllDataLoader;

	@Autowired
	protected Resource defaultQwacPolicy;

	@InitBinder
	public void setAllowedFields(WebDataBinder webDataBinder) {
		webDataBinder.setAllowedFields(ALLOWED_FIELDS);
	}

	@RequestMapping(method = RequestMethod.GET)
	public String showValidationForm(Model model, HttpServletRequest request) {
		QwacValidationForm qwacValidationForm = new QwacValidationForm();
		qwacValidationForm.setDefaultPolicy(true);
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
		CertificateToken tlsCertificate = WebAppUtils.toCertificateToken(qwacValidationForm.getTlsCertificate());

		LOG.info("Start QWAC validation. Requested URL : '{}'", url);

		QWACValidator qwacValidator;
		if (tlsCertificate != null) {
			qwacValidator = QWACValidator.fromUrlAndCertificate(url, tlsCertificate);
		} else {
			qwacValidator = QWACValidator.fromUrl(url);
		}

		qwacValidator.setCertificateVerifier(certificateVerifier);
		qwacValidator.setDataLoader(trustAllDataLoader);
		qwacValidator.setValidationTime(getValidationTime(qwacValidationForm.getValidationTime(), qwacValidationForm.getTimezoneDifference()));
		qwacValidator.setTokenExtractionStrategy(TokenExtractionStrategy.fromParameters(qwacValidationForm.isIncludeCertificateTokens(), false,
				qwacValidationForm.isIncludeRevocationTokens(), false));

		TokenIdentifierProvider identifierProvider = qwacValidationForm.isIncludeUserFriendlyIdentifiers() ?
				new UserFriendlyIdentifierProvider() : new OriginalIdentifierProvider();
		qwacValidator.setTokenIdentifierProvider(identifierProvider);

		byte[] tlsBindingSignature = getTlsBindingSignatureBytes(qwacValidationForm);
		if (tlsBindingSignature != null) {
			qwacValidator.setDataLoader(new ProxiedMemoryDataLoader(trustAllDataLoader, url, tlsCertificate, tlsBindingSignature));
		}

		CertificateReports reports = validate(qwacValidator, qwacValidationForm);

		model.addAttribute("currentCertificate", reports.getSimpleReport().getCertificateIds().get(0));
		setAttributesModels(model, reports);

		LOG.info("End certificate validation");

		return VALIDATION_RESULT_TILE;
	}

	private byte[] getTlsBindingSignatureBytes(QwacValidationForm qwacValidationForm) {
		MultipartFile tlsBindingSignature = qwacValidationForm.getTlsBindingSignature();
		if (tlsBindingSignature == null || tlsBindingSignature.isEmpty()) {
			return null;
		}
		try {
			return tlsBindingSignature.getBytes();
		} catch (IOException e) {
			throw new DSSException("Unable to read TLS Binding Signature!", e);
		}
	}

	private CertificateReports validate(QWACValidator qwacValidator, QwacValidationForm validationForm) {
		ValidationPolicy validationPolicy;
		MultipartFile policyFile = validationForm.getPolicyFile();
		if (!validationForm.isDefaultPolicy() && policyFile != null && !policyFile.isEmpty()) {
			try (InputStream is = policyFile.getInputStream()) {
				validationPolicy = ValidationPolicyLoader.fromValidationPolicy(is).create();
			} catch (IOException e) {
				throw new DSSException("Unable to load validation policy!", e);
			}
		} else if (defaultQwacPolicy != null) {
			try (InputStream is = defaultQwacPolicy.getInputStream()) {
				validationPolicy = ValidationPolicyLoader.fromValidationPolicy(is).create();
			} catch (IOException e) {
				throw new InternalServerException(String.format("Unable to parse policy: %s", e.getMessage()), e);
			}
		} else {
			throw new IllegalStateException("Validation policy is not correctly initialized!");
		}
		return qwacValidator.validate(validationPolicy);
	}

	@ModelAttribute("displayDownloadPdf")
	public boolean isDisplayDownloadPdf() {
		return true;
	}

	private static class ProxiedMemoryDataLoader extends AdvancedMemoryDataLoader {

		@Serial
		private static final long serialVersionUID = 260181250279558214L;

		private final AdvancedDataLoader proxiedDataLoader;

		public ProxiedMemoryDataLoader(AdvancedDataLoader proxiedDataLoader, String url,
									   CertificateToken tlsCertificate, byte[] tlsBindingSignature) {
			super(toMap(proxiedDataLoader, url, tlsCertificate, tlsBindingSignature));
			this.proxiedDataLoader = proxiedDataLoader;
		}

		private static Map<String, ResponseEnvelope> toMap(AdvancedDataLoader proxiedDataLoader, String url,
														   CertificateToken tlsCertificate, byte[] tlsBindingSignature) {
			final Map<String, ResponseEnvelope> map = new HashMap<>();

			ResponseEnvelope responseEnvelope = new ResponseEnvelope();
			if (tlsCertificate != null) {
				responseEnvelope.setTLSCertificates(new Certificate[] { tlsCertificate.getCertificate() });
			} else {
				responseEnvelope.setTLSCertificates(proxiedDataLoader.requestGet(url, true, false).getTLSCertificates());
			}

			if (tlsBindingSignature != null) {
				Map<String, List<String>> headersMap = new HashMap<>();
				headersMap.put("Link", Collections.singletonList("<provided>; rel=\"tls-certificate-binding\";"));
				responseEnvelope.setHeaders(headersMap);

				ResponseEnvelope tlsBindingSignatureEnvelope = new ResponseEnvelope();
				tlsBindingSignatureEnvelope.setResponseBody(tlsBindingSignature);
				map.put("provided", tlsBindingSignatureEnvelope);
			}

			map.put(url, responseEnvelope);

			return map;

		}

		@Override
		public ResponseEnvelope requestGet(String url, boolean includeResponseDetails, boolean includeResponseBody) {
			ResponseEnvelope responseEnvelope = super.requestGet(url, includeResponseDetails, includeResponseBody);
			if (responseEnvelope != null) {
				return responseEnvelope;
			}
			return proxiedDataLoader.requestGet(url, includeResponseDetails, includeResponseBody);
		}
	}

}
