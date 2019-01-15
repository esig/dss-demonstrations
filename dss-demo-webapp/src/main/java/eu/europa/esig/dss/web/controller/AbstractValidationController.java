package eu.europa.esig.dss.web.controller;

import java.io.StringReader;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpSession;
import javax.xml.bind.JAXB;
import javax.xml.bind.JAXBException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;

import eu.europa.esig.dss.validation.reports.AbstractReports;
import eu.europa.esig.dss.validation.reports.CertificateReports;
import eu.europa.esig.dss.validation.reports.Reports;
import eu.europa.esig.dss.validation.reports.wrapper.CertificateWrapper;
import eu.europa.esig.dss.validation.reports.wrapper.DiagnosticData;
import eu.europa.esig.dss.validation.reports.wrapper.RevocationWrapper;
import eu.europa.esig.dss.validation.reports.wrapper.TimestampWrapper;
import eu.europa.esig.dss.web.model.TokenDTO;
import eu.europa.esig.dss.web.service.XSLTService;

public abstract class AbstractValidationController {
	
	protected static final String SIMPLE_REPORT_ATTRIBUTE = "simpleReportXml";
	protected static final String DETAILED_REPORT_ATTRIBUTE = "detailedReportXml";
	protected static final String DIAGNOSTIC_DATA_ATTRIBUTE = "diagnosticDataXml";
	
	@Autowired
	private XSLTService xsltService;
	
	public void setCertificateValidationAttributesModels(Model model, CertificateReports reports) {
		String xmlSimpleReport = reports.getXmlSimpleReport();
		model.addAttribute(SIMPLE_REPORT_ATTRIBUTE, xmlSimpleReport);
		model.addAttribute("simpleReport", xsltService.generateSimpleCertificateReport(xmlSimpleReport));
		
		setCommonAttributesModel(model, reports);
	}
	
	public void setSignatureValidationAttributesModels(Model model, Reports reports) {
		String xmlSimpleReport = reports.getXmlSimpleReport();
		model.addAttribute(SIMPLE_REPORT_ATTRIBUTE, xmlSimpleReport);
		model.addAttribute("simpleReport", xsltService.generateSimpleReport(xmlSimpleReport));

		setCommonAttributesModel(model, reports);
	}
	
	public void setCommonAttributesModel(Model model, AbstractReports reports) {
		String xmlDetailedReport = reports.getXmlDetailedReport();
		model.addAttribute(DETAILED_REPORT_ATTRIBUTE, xmlDetailedReport);
		model.addAttribute("detailedReport", xsltService.generateDetailedReport(xmlDetailedReport));

		DiagnosticData diagnosticData = reports.getDiagnosticData();
		model.addAttribute(DIAGNOSTIC_DATA_ATTRIBUTE, reports.getXmlDiagnosticData());
		
		// Get Certificates for which binaries are available
		List<CertificateWrapper> usedCertificates = diagnosticData.getUsedCertificates();
		Set<TokenDTO> certificatesTokens = new HashSet<TokenDTO>();
		for(CertificateWrapper cert : usedCertificates) {
			if(cert.getBinaries() != null) {
				certificatesTokens.add(new TokenDTO(cert));
			}
		}
		model.addAttribute("allCertificates", certificatesTokens);
		
		// Get Revocation data for which binaries are available
		Set<RevocationWrapper> allRevocationData = diagnosticData.getAllRevocationData();
		Set<TokenDTO> revocationTokens = new HashSet<TokenDTO>();
		for(RevocationWrapper rd : allRevocationData) {
			if(rd.getBinaries() != null) {
				revocationTokens.add(new TokenDTO(rd));
			}
		}
		model.addAttribute("allRevocationData", revocationTokens);
		
		// Get Timestamps for which binaries are available
		Set<TimestampWrapper> allTimestamps = diagnosticData.getAllTimestamps();
		Set<TokenDTO> timestampsTokens = new HashSet<TokenDTO>();
		for(TimestampWrapper tst : allTimestamps) {
			if(tst.getBinaries() != null) {
				timestampsTokens.add(new TokenDTO(tst));
			}
		}
		model.addAttribute("allTimestamps", timestampsTokens);
	}
	
	public DiagnosticData getDiagnosticData(HttpSession session) throws JAXBException {
		String diagnosticDataXml = (String) session.getAttribute(DIAGNOSTIC_DATA_ATTRIBUTE);
		return new DiagnosticData(JAXB.unmarshal(new StringReader(diagnosticDataXml), eu.europa.esig.dss.jaxb.diagnostic.DiagnosticData.class));
	}
}
