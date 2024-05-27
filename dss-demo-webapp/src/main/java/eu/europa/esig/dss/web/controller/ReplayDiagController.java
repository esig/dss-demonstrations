package eu.europa.esig.dss.web.controller;

import eu.europa.esig.dss.diagnostic.DiagnosticDataFacade;
import eu.europa.esig.dss.diagnostic.jaxb.XmlCertificate;
import eu.europa.esig.dss.diagnostic.jaxb.XmlDiagnosticData;
import eu.europa.esig.dss.enumerations.ValidationLevel;
import eu.europa.esig.dss.policy.EtsiValidationPolicy;
import eu.europa.esig.dss.policy.ValidationPolicyFacade;
import eu.europa.esig.dss.utils.Utils;
import eu.europa.esig.dss.validation.executor.ProcessExecutor;
import eu.europa.esig.dss.validation.executor.certificate.CertificateProcessExecutor;
import eu.europa.esig.dss.validation.executor.certificate.DefaultCertificateProcessExecutor;
import eu.europa.esig.dss.validation.executor.signature.DefaultSignatureProcessExecutor;
import eu.europa.esig.dss.validation.reports.AbstractReports;
import eu.europa.esig.dss.web.exception.InternalServerException;
import eu.europa.esig.dss.web.model.ReplayDiagForm;
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
import org.springframework.web.bind.annotation.SessionAttributes;

import java.io.InputStream;
import java.util.Date;
import java.util.List;
import java.util.Locale;


@Controller
@SessionAttributes({ "simpleReportXml", "detailedReportXml", "diagnosticDataXml" })
@RequestMapping(value = "/replay-diagnostic-data")
public class ReplayDiagController extends AbstractValidationController {

	private static final Logger LOG = LoggerFactory.getLogger(ReplayDiagController.class);
	
	private static final String REPLAY_TILE = "replay-diagnostic-data";
	private static final String VALIDATION_RESULT_TILE = "validation-result";
	
	private static final String[] ALLOWED_FIELDS = { "diagnosticFile", "resetDate", "validationLevel", "defaultPolicy", "policyFile" };

	@Autowired
	private Resource defaultPolicy;
	
	@InitBinder
	public void setAllowedFields(WebDataBinder webDataBinder) {
		webDataBinder.setAllowedFields(ALLOWED_FIELDS);
	}
	
	@RequestMapping(method = RequestMethod.GET)
	public String showReplayDiagForm(Model model, HttpServletRequest request) {
		ReplayDiagForm replayForm = new ReplayDiagForm();
		replayForm.setValidationLevel(ValidationLevel.ARCHIVAL_DATA);
		replayForm.setDefaultPolicy(true);
		model.addAttribute("replayDiagForm", replayForm);
		return REPLAY_TILE;
	}
	
	@RequestMapping(method = RequestMethod.POST)
	public String validate(@ModelAttribute("replayDiagForm") @Valid ReplayDiagForm replayDiagForm, BindingResult result,
						   Model model, HttpServletRequest request) {
		if (result.hasErrors()) {
			if (LOG.isDebugEnabled()) {
				List<ObjectError> allErrors = result.getAllErrors();
				for (ObjectError error : allErrors) {
					LOG.debug(error.getDefaultMessage());
				}
			}
			return REPLAY_TILE;
		}

		XmlDiagnosticData dd;
		try (InputStream is = replayDiagForm.getDiagnosticFile().getInputStream()) {
			dd = DiagnosticDataFacade.newFacade().unmarshall(is);
		} catch (Exception e) {
			LOG.warn("Unable to parse the diagnostic data", e);
			throw new InternalServerException("Error while creating diagnostic data from given file");
		}
			
		// Determine if Diagnostic data is a certificate or signature validation
		ProcessExecutor<? extends AbstractReports> executor;
		executor = Utils.isCollectionEmpty(dd.getSignatures()) && Utils.isCollectionEmpty(dd.getUsedTimestamps()) ?
				new DefaultCertificateProcessExecutor() : new DefaultSignatureProcessExecutor();
		executor.setDiagnosticData(dd);

		Locale locale = request.getLocale();
		LOG.trace("Requested locale : {}", request.getLocale());
		if (locale == null) {
			locale = Locale.getDefault();
			LOG.warn("The request Locale is null! Use the default one : {}", locale);
		}
		executor.setLocale(locale);
		
		// Set validation date
		Date validationDate = (replayDiagForm.isResetDate()) ? new Date() : dd.getValidationDate();
		dd.setValidationDate(validationDate);
		executor.setCurrentTime(validationDate);
		
		// Set policy
		if (!replayDiagForm.isDefaultPolicy() && ((replayDiagForm.getPolicyFile() != null) && !replayDiagForm.getPolicyFile().isEmpty())) {
			try (InputStream policyIs = replayDiagForm.getPolicyFile().getInputStream()) {
				executor.setValidationPolicy(new EtsiValidationPolicy(ValidationPolicyFacade.newFacade().unmarshall(policyIs)));
			} catch (Exception e) {
				throw new InternalServerException(String.format("Error while loading the provided validation policy: %s", e.getMessage()), e);
			}
		} else if (defaultPolicy != null) {
			try (InputStream is = defaultPolicy.getInputStream()) {
				executor.setValidationPolicy(ValidationPolicyFacade.newFacade().getValidationPolicy(is));
			} catch (Exception e) {
				throw new InternalServerException(String.format("Error while loading the default validation policy: %s", e.getMessage()), e);
			}
		} else {
			throw new IllegalStateException("Validation policy is not correctly initialized!");
		}
		
		// If applicable, set certificate id
		if (executor instanceof CertificateProcessExecutor) {
			((CertificateProcessExecutor) executor).setCertificateId(getCertificateId(dd));
		} else {
			((DefaultSignatureProcessExecutor) executor).setValidationLevel(replayDiagForm.getValidationLevel());
		}
		
		AbstractReports reports = executor.execute();
		setAttributesModels(model, reports);
		
		return VALIDATION_RESULT_TILE;
		
	}

	private String getCertificateId(XmlDiagnosticData dd) {
		String certificateId = null;
		int longestChain= 0;
		List<XmlCertificate> usedCertificates = dd.getUsedCertificates();
		for (XmlCertificate xmlCertificate : usedCertificates) {
			int chainSize = Utils.collectionSize(xmlCertificate.getCertificateChain());
			if (longestChain == 0 || longestChain < chainSize) {
				longestChain = chainSize;
				certificateId = xmlCertificate.getId();
			}
		}
		return certificateId;
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