package eu.europa.esig.dss.web.controller;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.SessionAttributes;

import eu.europa.esig.dss.DSSDocument;
import eu.europa.esig.dss.jaxb.diagnostic.DiagnosticData;
import eu.europa.esig.dss.utils.Utils;
import eu.europa.esig.dss.validation.executor.CustomProcessExecutor;
import eu.europa.esig.dss.validation.policy.EtsiValidationPolicy;
import eu.europa.esig.dss.validation.reports.Reports;
import eu.europa.esig.dss.web.WebAppUtils;
import eu.europa.esig.dss.web.model.ReplayDiagForm;
import eu.europa.esig.jaxb.policy.ConstraintsParameters;

@Controller
@SessionAttributes({ "simpleReportXml", "detailedReportXml", "diagnosticTreeObject" })
@RequestMapping(value = "/replay-diag")
public class ReplayDiagController extends AbstractValidationController {

	private static final String REPLAY_TILE = "replay-diag";
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
		
		try {
			
			InputStream is =  new BufferedInputStream(replayDiagForm.getDiagnosticFile().getInputStream());
			DiagnosticData dd = getJAXBObjectFromString(is, DiagnosticData.class, "/xsd/DiagnosticData.xsd");
			
			// Get policy
			DSSDocument policyFile = WebAppUtils.toDSSDocument(replayDiagForm.getPolicyFile());
			InputStream policyIs;
			if (!replayDiagForm.isDefaultPolicy() && (policyFile != null)) {
				policyIs = policyFile.openStream();
			} else {
				policyIs = defaultPolicy.getInputStream();
			}
			
			// Get validation date
			Date validationDate;
			if(replayDiagForm.isResetDate()) {
				validationDate = new Date();
			} else {
				validationDate = dd.getValidationDate();
			}
			
			CustomProcessExecutor executor = new CustomProcessExecutor();
			executor.setDiagnosticData(dd);
			executor.setValidationPolicy(loadPolicy(policyIs));
			executor.setCurrentTime(validationDate);
			
			Reports reports = executor.execute();
			
			eu.europa.esig.dss.validation.reports.wrapper.DiagnosticData diagnosticData = reports.getDiagnosticData();
			
			if(diagnosticData.getAllSignatures() == null) {
				// No signature -> Certificate validation
				setCertificateValidationAttributesModel(model, reports);
			} else {
				// Signatures -> Signature validation
				setSignatureValidationAttributesModel(model, reports);
			}
			
			return VALIDATION_RESULT_TILE;
		} catch(Exception e) {
			return REPLAY_TILE;
		}
	}
	
	private EtsiValidationPolicy loadPolicy(InputStream is) throws Exception {
		ConstraintsParameters policyJaxB = getJAXBObjectFromString(is, ConstraintsParameters.class, "/xsd/policy.xsd");
		return new EtsiValidationPolicy(policyJaxB);
	}
	
	@SuppressWarnings("unchecked") // TODO: move to Utils / DSSUtils
	private <T extends Object> T getJAXBObjectFromString(InputStream is, Class<T> clazz, String xsd) throws Exception {
		JAXBContext context = JAXBContext.newInstance(clazz.getPackage().getName());
		Unmarshaller unmarshaller = context.createUnmarshaller();
		if (Utils.isStringNotEmpty(xsd)) {
			SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
			InputStream inputStream = this.getClass().getResourceAsStream(xsd);
			Source source = new StreamSource(inputStream);
			Schema schema = sf.newSchema(source);
			unmarshaller.setSchema(schema);
		}
		return (T) unmarshaller.unmarshal(is);
	}
}