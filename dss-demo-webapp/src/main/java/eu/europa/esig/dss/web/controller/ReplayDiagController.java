package eu.europa.esig.dss.web.controller;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
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
import org.xml.sax.SAXException;

import eu.europa.esig.dss.DSSDocument;
import eu.europa.esig.dss.jaxb.diagnostic.DiagnosticData;
import eu.europa.esig.dss.utils.Utils;
import eu.europa.esig.dss.validation.executor.CertificateProcessExecutor;
import eu.europa.esig.dss.validation.executor.CustomProcessExecutor;
import eu.europa.esig.dss.validation.policy.EtsiValidationPolicy;
import eu.europa.esig.dss.validation.reports.CertificateReports;
import eu.europa.esig.dss.validation.reports.Reports;
import eu.europa.esig.dss.web.WebAppUtils;
import eu.europa.esig.dss.web.model.ReplayDiagForm;
import eu.europa.esig.jaxb.policy.ConstraintsParameters;

@Controller
@SessionAttributes({ "simpleReportXml", "detailedReportXml", "diagnosticDataXml" })
@RequestMapping(value = "/replay-diagnostic-data")
public class ReplayDiagController extends AbstractValidationController {

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
	public String validate(@ModelAttribute("replayDiagForm") @Valid ReplayDiagForm replayDiagForm, BindingResult result, Model model) throws Exception {
		if (result.hasErrors()) {
			return REPLAY_TILE;
		}
			
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
		
		// Determine if Diagnostic data is a signature or certificate validation
		if(dd.getSignatures() == null || dd.getSignatures().isEmpty()) {
			// No signature -> Certificate validation
			
			CertificateProcessExecutor executor = new CertificateProcessExecutor();
			executor.setDiagnosticData(dd);
			executor.setValidationPolicy(loadPolicy(policyIs));
			executor.setCurrentTime(validationDate);
			executor.setCertificateId(dd.getUsedCertificates().get(0).getId());
			
			CertificateReports reports = executor.execute();
			
			setCertificateValidationAttributesModels(model, reports);
		} else {
			// Signatures -> Signature validation
			
			CustomProcessExecutor executor = new CustomProcessExecutor();
			executor.setDiagnosticData(dd);
			executor.setValidationPolicy(loadPolicy(policyIs));
			executor.setCurrentTime(validationDate);
			
			Reports reports = executor.execute();
			
			setSignatureValidationAttributesModels(model, reports);
		}
		
		policyIs.close();
		
		return VALIDATION_RESULT_TILE;
	}
	
	private EtsiValidationPolicy loadPolicy(InputStream is) throws Exception {
		ConstraintsParameters policyJaxB = getJAXBObjectFromString(is, ConstraintsParameters.class, "/xsd/policy.xsd");
		return new EtsiValidationPolicy(policyJaxB);
	}
	
	@SuppressWarnings("unchecked")
	private <T extends Object> T getJAXBObjectFromString(InputStream is, Class<T> clazz, String xsd) throws JAXBException, SAXException  {
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