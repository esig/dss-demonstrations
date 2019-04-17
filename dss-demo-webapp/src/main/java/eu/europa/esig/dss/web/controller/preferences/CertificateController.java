package eu.europa.esig.dss.web.controller.preferences;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.SessionAttributes;

import eu.europa.esig.dss.tsl.service.TSLRepository;
import eu.europa.esig.dss.web.model.CertificateForm;
import eu.europa.esig.dss.web.service.KeystoreService;

@Controller
@SessionAttributes(value = "certificateForm")
@RequestMapping(value = "/oj-certificates" )
public class CertificateController {

	private static final String CERTIFICATE_TILE = "oj-certificates";

	@Autowired
	private KeystoreService keystoreService;
	
	@Autowired
	private TSLRepository tslRepository;
	
	@Value("${current.oj.url}")
	private String currentOjUrl;

	@RequestMapping(method = RequestMethod.GET)
	public String showCertificates(Model model, HttpServletRequest request) {
		CertificateForm certificateForm = new CertificateForm();
		model.addAttribute("certificateForm", certificateForm);
		model.addAttribute("keystoreCertificates", keystoreService.getCertificatesDTOFromKeyStore());
		model.addAttribute("actualOjUrl", tslRepository.getActualOjUrl());
		model.addAttribute("currentOjUrl", currentOjUrl);
		return CERTIFICATE_TILE;
	}

}