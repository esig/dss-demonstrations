package eu.europa.esig.dss.web.controller;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;

import eu.europa.esig.dss.DSSASN1Utils;
import eu.europa.esig.dss.DSSUtils;
import eu.europa.esig.dss.MimeType;
import eu.europa.esig.dss.utils.Utils;
import eu.europa.esig.dss.validation.reports.AbstractReports;
import eu.europa.esig.dss.validation.reports.Reports;
import eu.europa.esig.dss.validation.reports.wrapper.CertificateWrapper;
import eu.europa.esig.dss.validation.reports.wrapper.DiagnosticData;
import eu.europa.esig.dss.validation.reports.wrapper.RevocationWrapper;
import eu.europa.esig.dss.validation.reports.wrapper.TimestampWrapper;
import eu.europa.esig.dss.web.exception.BadRequestException;
import eu.europa.esig.dss.web.service.XSLTService;

public abstract class AbstractValidationController {
	
	private static final Logger logger = LoggerFactory.getLogger(AbstractValidationController.class);
	
	protected static final String SIMPLE_REPORT_ATTRIBUTE = "simpleReportXml";
	protected static final String DETAILED_REPORT_ATTRIBUTE = "detailedReportXml";
	protected static final String DIAGNOSTIC_DATA = "diagnosticTreeObject";
	
	@Autowired
	private XSLTService xsltService;
	
	public void setCertificateValidationAttributesModel(Model model, AbstractReports reports) {
		String xmlSimpleReport = reports.getXmlSimpleReport();
		model.addAttribute(SIMPLE_REPORT_ATTRIBUTE, xmlSimpleReport);
		model.addAttribute("simpleReport", xsltService.generateSimpleCertificateReport(xmlSimpleReport));
		
		model.addAttribute("validationTile", "certificate-validation");
		setCommonAttributesModel(model, reports);
	}
	
	public void setSignatureValidationAttributesModel(Model model, Reports reports) {
		String xmlSimpleReport = reports.getXmlSimpleReport();
		model.addAttribute(SIMPLE_REPORT_ATTRIBUTE, xmlSimpleReport);
		model.addAttribute("simpleReport", xsltService.generateSimpleReport(xmlSimpleReport));

		model.addAttribute("validationTile", "validation");
		setCommonAttributesModel(model, reports);
	}
	
	public void setCommonAttributesModel(Model model, AbstractReports reports) {
		String xmlDetailedReport = reports.getXmlDetailedReport();
		model.addAttribute(DETAILED_REPORT_ATTRIBUTE, xmlDetailedReport);
		model.addAttribute("detailedReport", xsltService.generateDetailedReport(xmlDetailedReport));

		DiagnosticData diagnosticData = reports.getDiagnosticData();
		model.addAttribute(DIAGNOSTIC_DATA, diagnosticData);
		model.addAttribute("diagnosticTree", reports.getXmlDiagnosticData());
		
		List<CertificateWrapper> usedCertificates = diagnosticData.getUsedCertificates();
		model.addAttribute("usedCertificates", usedCertificates);
		
		// Are Revocation data binaries available ?
		boolean revocationDataBinariesAvailable = true;
		for(CertificateWrapper cert : usedCertificates) {
			for(RevocationWrapper revocationData : cert.getRevocationData()) {
				if(revocationData.getBinaries() == null) {
					revocationDataBinariesAvailable = false;
				}
			}
		}
		model.addAttribute("showRevocation", revocationDataBinariesAvailable);
		
		// Are Timestamps binaries available ?
		boolean timestampsBinariesAvailable = true;
		Set<TimestampWrapper> allTimestamps = diagnosticData.getAllTimestamps();
		for(TimestampWrapper tst : allTimestamps) {
			if(tst.getBinaries() == null) {
				timestampsBinariesAvailable = false;
			}
		}
		model.addAttribute("allTimestamps", timestampsBinariesAvailable ? allTimestamps : null);
	}
	
	public void setCertificateResponse(String id, DiagnosticData diagnosticData, HttpServletResponse response) {
		CertificateWrapper certificate = diagnosticData.getUsedCertificateById(id);
		if(certificate == null) {
			String message = "Certificate " + id + " not found";
			logger.warn(message);
			throw new BadRequestException(message);
		}
		String pemCert = DSSUtils.convertToPEM(DSSUtils.loadCertificate(certificate.getBinaries()));
		String filename = DSSASN1Utils.getHumanReadableName(DSSUtils.loadCertificate(certificate.getBinaries())).replace(" ", "_")+".cer";
		
		response.setContentType(MimeType.CER.getMimeTypeString());
		response.setHeader("Content-Disposition", "attachment; filename="+filename);
		try {
			Utils.copy(new ByteArrayInputStream(pemCert.getBytes()), response.getOutputStream());
		} catch (IOException e) {
			logger.error("An error occured while downloading certificate : " + e.getMessage(), e);
		}
	}
	
	public void setRevocationResponse(String id, String format, DiagnosticData diagnosticData, HttpServletResponse response) {
		RevocationWrapper revocationData = diagnosticData.getRevocationDataById(id);
		if(revocationData == null) {
			String message = "Revocation data " + id + " not found";
			logger.warn(message);
			throw new BadRequestException(message);
		}
		String certId = revocationData.getSigningCertificateId();
		String filename = "";
				
		CertificateWrapper cert = diagnosticData.getUsedCertificateById(certId);
		if(cert != null) {
			filename+=DSSASN1Utils.getHumanReadableName(DSSUtils.loadCertificate(cert.getBinaries()))+"_";
		}
		filename += revocationData.getSource().replace("Token", "");
		String mimeType;
		byte[] is;
		
		if(revocationData.getSource().contains("CRL")) {
			mimeType = MimeType.CRL.getMimeTypeString();
			filename += ".crl";
			
			if(Utils.areStringsEqualIgnoreCase(format ,"pem")) {
				String pem = "-----BEGIN CRL-----\n";
				pem += Utils.toBase64(revocationData.getBinaries());
				pem += "\n-----END CRL-----";
				is = pem.getBytes();
			} else {
				is = revocationData.getBinaries();
			}
		} else {
			mimeType = MimeType.BINARY.getMimeTypeString();
			filename += ".ocsp";
			is = revocationData.getBinaries();
		}
		response.setContentType(mimeType);
		response.setHeader("Content-Disposition", "attachment; filename="+filename.replace(" ", "_"));
		try {
			Utils.copy(new ByteArrayInputStream(is), response.getOutputStream());
		} catch (IOException e) {
			logger.error("An error occured while downloading revocation data : " + e.getMessage(), e);
		}
	}
	
	public void setTimestampResponse(String id, String format, DiagnosticData diagnosticData, HttpServletResponse response) {
		TimestampWrapper timestamp = diagnosticData.getTimestampById(id);
		if(timestamp == null) {
			String message = "Timestamp " + id + " not found";
			logger.warn(message);
			throw new BadRequestException(message);
		}
		String certId = timestamp.getSigningCertificateId();
		CertificateWrapper cert = diagnosticData.getUsedCertificateById(certId);
		String filename = "";
		
		if(cert != null) {
			filename+=DSSASN1Utils.getHumanReadableName(DSSUtils.loadCertificate(cert.getBinaries()))+"_";
		}
		filename += timestamp.getType();
		
		response.setContentType(MimeType.TST.getMimeTypeString());
		response.setHeader("Content-Disposition", "attachment; filename="+filename.replace(" ", "_")+".tst");
		byte[] is;
		
		if(Utils.areStringsEqualIgnoreCase(format, "pem")) {
			String pem = "-----BEGIN TIMESTAMP-----\n";
			pem += Utils.toBase64(timestamp.getBinaries());
			pem += "\n-----END TIMESTAMP-----";
			is = pem.getBytes();
		} else {
			is = timestamp.getBinaries();
		}
		
		try {
			Utils.copy(new ByteArrayInputStream(is), response.getOutputStream());
		} catch (IOException e) {
			logger.error("An error occured while downloading timestamp : " + e.getMessage(), e);
		}
	}
}
