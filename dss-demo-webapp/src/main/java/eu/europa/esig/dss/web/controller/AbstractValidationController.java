package eu.europa.esig.dss.web.controller;

import java.util.HashSet;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;

import eu.europa.esig.dss.diagnostic.AbstractTokenProxy;
import eu.europa.esig.dss.diagnostic.CertificateWrapper;
import eu.europa.esig.dss.diagnostic.DiagnosticData;
import eu.europa.esig.dss.validation.CertificateVerifier;
import eu.europa.esig.dss.validation.reports.AbstractReports;
import eu.europa.esig.dss.validation.reports.CertificateReports;
import eu.europa.esig.dss.validation.reports.Reports;
import eu.europa.esig.dss.web.model.TokenDTO;
import eu.europa.esig.dss.web.service.XSLTService;
import eu.europa.esig.validationreport.jaxb.ValidationReportType;

public abstract class AbstractValidationController {

	protected static final String SIMPLE_REPORT_ATTRIBUTE = "simpleReport";
	protected static final String DETAILED_REPORT_ATTRIBUTE = "detailedReport";
	
	protected static final String XML_SIMPLE_REPORT_ATTRIBUTE = "simpleReportXml";
	protected static final String XML_DETAILED_REPORT_ATTRIBUTE = "detailedReportXml";
	protected static final String XML_DIAGNOSTIC_DATA_ATTRIBUTE = "diagnosticDataXml";
	protected static final String ETSI_VALIDATION_REPORT_ATTRIBUTE = "etsiValidationReport";
	
	protected static final String ALL_CERTIFICATES_ATTRIBUTE = "allCertificates";
	protected static final String ALL_REVOCATION_DATA_ATTRIBUTE = "allRevocationData";
	protected static final String ALL_TIMESTAMPS_ATTRIBUTE = "allTimestamps";

	@Autowired
	protected CertificateVerifier certificateVerifier;

	@Autowired
	protected XSLTService xsltService;

	public void setAttributesModels(Model model, AbstractReports reports) {
		String xmlSimpleReport = reports.getXmlSimpleReport();
		model.addAttribute(XML_SIMPLE_REPORT_ATTRIBUTE, xmlSimpleReport);
		if (reports instanceof CertificateReports) {
			model.addAttribute(SIMPLE_REPORT_ATTRIBUTE, xsltService.generateSimpleCertificateReport(xmlSimpleReport));
		} else {
			model.addAttribute(SIMPLE_REPORT_ATTRIBUTE, xsltService.generateSimpleReport(xmlSimpleReport));
		}

		String xmlDetailedReport = reports.getXmlDetailedReport();
		model.addAttribute(XML_DETAILED_REPORT_ATTRIBUTE, xmlDetailedReport);
		model.addAttribute(DETAILED_REPORT_ATTRIBUTE, xsltService.generateDetailedReport(xmlDetailedReport));

		DiagnosticData diagnosticData = reports.getDiagnosticData();
		model.addAttribute(XML_DIAGNOSTIC_DATA_ATTRIBUTE, reports.getXmlDiagnosticData());

		if (reports instanceof Reports) {
			Reports sigReports = (Reports) reports;
			ValidationReportType etsiValidationReportJaxb = sigReports.getEtsiValidationReportJaxb();
			if (etsiValidationReportJaxb != null) {
				model.addAttribute(ETSI_VALIDATION_REPORT_ATTRIBUTE, sigReports.getXmlValidationReport());
			}
		}

		// Get Certificates for which binaries are available
		Set<CertificateWrapper> usedCertificates = new HashSet<CertificateWrapper>(diagnosticData.getUsedCertificates());
		model.addAttribute(ALL_CERTIFICATES_ATTRIBUTE, buildTokenDtos(usedCertificates));

		// Get Revocation data for which binaries are available
		model.addAttribute(ALL_REVOCATION_DATA_ATTRIBUTE, buildTokenDtos(diagnosticData.getAllRevocationData()));

		// Get Timestamps for which binaries are available
		model.addAttribute(ALL_TIMESTAMPS_ATTRIBUTE, buildTokenDtos(diagnosticData.getTimestampSet()));
	}

	private Set<TokenDTO> buildTokenDtos(Set<? extends AbstractTokenProxy> abstractTokens) {
		Set<TokenDTO> tokenDtos = new HashSet<TokenDTO>();
		for (AbstractTokenProxy token : abstractTokens) {
			if (token.getBinaries() != null) {
				tokenDtos.add(new TokenDTO(token));
			}
		}
		return tokenDtos;
	}

}
