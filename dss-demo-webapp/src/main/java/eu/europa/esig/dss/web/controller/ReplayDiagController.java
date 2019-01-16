package eu.europa.esig.dss.web.controller;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.xml.bind.JAXBException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.xml.sax.SAXException;

import eu.europa.esig.dss.DSSDocument;
import eu.europa.esig.dss.jaxb.diagnostic.DiagnosticData;
import eu.europa.esig.dss.validation.ValidationResourceManager;
import eu.europa.esig.dss.validation.executor.CertificateProcessExecutor;
import eu.europa.esig.dss.validation.executor.CustomProcessExecutor;
import eu.europa.esig.dss.validation.executor.ProcessExecutor;
import eu.europa.esig.dss.validation.policy.EtsiValidationPolicy;
import eu.europa.esig.dss.validation.policy.XmlUtils;
import eu.europa.esig.dss.validation.reports.AbstractReports;
import eu.europa.esig.dss.web.WebAppUtils;
import eu.europa.esig.dss.web.exception.InternalServerException;
import eu.europa.esig.dss.web.model.ReplayDiagForm;
import eu.europa.esig.jaxb.policy.ConstraintsParameters;

@Controller
@SessionAttributes({ "simpleReportXml", "detailedReportXml", "diagnosticDataXml" })
@RequestMapping(value = "/replay-diagnostic-data")
public class ReplayDiagController extends AbstractValidationController {

	private static final Logger logger = LoggerFactory.getLogger(ReplayDiagController.class);
	
	private static final String REPLAY_TILE = "replay-diagnostic-data";
	private static final String VALIDATION_RESULT_TILE = "validation_result";
	
	@Autowired
	private Resource defaultPolicy;

	@RequestMapping(method = RequestMethod.GET)
	public String showReplayDiagForm(Model model, HttpServletRequest request) {
		ReplayDiagForm replayForm = new ReplayDiagForm();
		replayForm.setDefaultPolicy(true);
		model.addAttribute("replayDiagForm", replayForm);
		return REPLAY_TILE;
	}
	
	@RequestMapping(method = RequestMethod.POST)
	public String validate(@ModelAttribute("replayDiagForm") @Valid ReplayDiagForm replayDiagForm, BindingResult result, Model model) {
		if (result.hasErrors()) {
			return REPLAY_TILE;
		}
		
		DiagnosticData dd;
		try {
			InputStream is =  new BufferedInputStream(replayDiagForm.getDiagnosticFile().getInputStream());
			dd = XmlUtils.getJAXBObjectFromString(is, DiagnosticData.class, "/xsd/DiagnosticData.xsd");
		} catch(IOException | JAXBException | SAXException e) {
			throw new InternalServerException("Error while creating diagnostic data from given file");
		}
			
		// Determine if Diagnostic data is a certificate or signature validation
		ProcessExecutor<? extends AbstractReports> executor;
		executor = (dd.getSignatures() == null || dd.getSignatures().isEmpty()) ? new CertificateProcessExecutor() : new CustomProcessExecutor();
		executor.setDiagnosticData(dd);
		
		// Set validation date
		Date validationDate = (replayDiagForm.isResetDate()) ? new Date() : dd.getValidationDate();
		executor.setCurrentTime(validationDate);
		
		// Set policy
		DSSDocument policyFile = WebAppUtils.toDSSDocument(replayDiagForm.getPolicyFile());
		if (!replayDiagForm.isDefaultPolicy() && (policyFile != null)) {
			try (InputStream policyIs = policyFile.openStream()) {
				executor.setValidationPolicy(loadPolicy(policyIs));
			} catch (IOException e) {
				logger.error(e.getMessage(), e);
			}
		} else if (defaultPolicy != null) {
			try (InputStream policyIs = defaultPolicy.getInputStream()) {
				executor.setValidationPolicy(loadPolicy(policyIs));
			} catch (IOException e) {
				logger.error("Unable to parse policy : " + e.getMessage(), e);
			}
		} else {
			logger.error("Not correctly initialized");
		}
		
		// If applicable, set certificate id
		if(executor instanceof CertificateProcessExecutor) {
			((CertificateProcessExecutor) executor).setCertificateId(dd.getUsedCertificates().get(0).getId());
		}
		
		AbstractReports reports = executor.execute();
		setAttributesModels(model, reports);
		
		return VALIDATION_RESULT_TILE;
		
	}
	
	private EtsiValidationPolicy loadPolicy(InputStream is) {
		ConstraintsParameters policyJaxB = ValidationResourceManager.loadPolicyData(is);
		return new EtsiValidationPolicy(policyJaxB);
	}
}