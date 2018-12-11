package eu.europa.esig.dss.web.controller;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import eu.europa.esig.dss.DSSASN1Utils;
import eu.europa.esig.dss.DSSUtils;
import eu.europa.esig.dss.MimeType;
import eu.europa.esig.dss.utils.Utils;
import eu.europa.esig.dss.validation.reports.wrapper.CertificateWrapper;
import eu.europa.esig.dss.validation.reports.wrapper.DiagnosticData;
import eu.europa.esig.dss.validation.reports.wrapper.RevocationWrapper;
import eu.europa.esig.dss.web.exception.BadRequestException;

public abstract class AbstractValidationController {
	
	private static final Logger logger = LoggerFactory.getLogger(AbstractValidationController.class);
	
	protected static final String DIAGNOSTIC_DATA = "diagnosticTreeObject";
	
	@RequestMapping(value = "/download-certificate")
	public void downloadCertificate(@RequestParam(value="id") String id, HttpSession session, HttpServletResponse response) {
		DiagnosticData diagnosticData = (DiagnosticData) session.getAttribute(DIAGNOSTIC_DATA);
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
	
	@RequestMapping(value = "/download-revocation")
	public void downloadRevocationData(@RequestParam(value="id") String id, @RequestParam(value="format", required=false) String format, HttpSession session, HttpServletResponse response) {
		DiagnosticData diagnosticData = (DiagnosticData) session.getAttribute(DIAGNOSTIC_DATA);
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
}
