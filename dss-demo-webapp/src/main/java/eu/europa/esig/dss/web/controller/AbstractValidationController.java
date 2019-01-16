package eu.europa.esig.dss.web.controller;

import java.util.HashSet;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;

import eu.europa.esig.dss.validation.reports.AbstractReports;
import eu.europa.esig.dss.validation.reports.CertificateReports;
import eu.europa.esig.dss.validation.reports.wrapper.AbstractTokenProxy;
import eu.europa.esig.dss.validation.reports.wrapper.CertificateWrapper;
import eu.europa.esig.dss.validation.reports.wrapper.DiagnosticData;
import eu.europa.esig.dss.web.model.TokenDTO;
import eu.europa.esig.dss.web.service.XSLTService;

public abstract class AbstractValidationController {
	
	protected static final String SIMPLE_REPORT_ATTRIBUTE = "simpleReportXml";
	protected static final String DETAILED_REPORT_ATTRIBUTE = "detailedReportXml";
	protected static final String DIAGNOSTIC_DATA_ATTRIBUTE = "diagnosticDataXml";
	
	@Autowired
	private XSLTService xsltService;
	
	public void setAttributesModels(Model model, AbstractReports reports) {
		String xmlSimpleReport = reports.getXmlSimpleReport();
		model.addAttribute(SIMPLE_REPORT_ATTRIBUTE, xmlSimpleReport);
		if(reports instanceof CertificateReports) {
			model.addAttribute("simpleReport", xsltService.generateSimpleCertificateReport(xmlSimpleReport));
		} else {
			model.addAttribute("simpleReport", xsltService.generateSimpleReport(xmlSimpleReport));
		}
		
		String xmlDetailedReport = reports.getXmlDetailedReport();
		model.addAttribute(DETAILED_REPORT_ATTRIBUTE, xmlDetailedReport);
		model.addAttribute("detailedReport", xsltService.generateDetailedReport(xmlDetailedReport));

		DiagnosticData diagnosticData = reports.getDiagnosticData();
		model.addAttribute(DIAGNOSTIC_DATA_ATTRIBUTE, reports.getXmlDiagnosticData());
		
		// Get Certificates for which binaries are available
		Set<CertificateWrapper> usedCertificates = new HashSet<CertificateWrapper>(diagnosticData.getUsedCertificates());
		model.addAttribute("allCertificates", buildTokenDtos(usedCertificates));
		
		// Get Revocation data for which binaries are available
		model.addAttribute("allRevocationData", buildTokenDtos(diagnosticData.getAllRevocationData()));
		
		// Get Timestamps for which binaries are available
		model.addAttribute("allTimestamps", buildTokenDtos(diagnosticData.getAllTimestamps()));
	}

	private Set<TokenDTO> buildTokenDtos(Set<? extends AbstractTokenProxy> abstractTokens) {
		Set<TokenDTO> tokenDtos = new HashSet<TokenDTO>();
		for(AbstractTokenProxy token : abstractTokens) {
			if(token.getBinaries() != null) {
				tokenDtos.add(new TokenDTO(token));
			}
		}
		return tokenDtos;
	}
	
}
