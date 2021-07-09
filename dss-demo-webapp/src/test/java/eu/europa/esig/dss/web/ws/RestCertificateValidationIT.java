package eu.europa.esig.dss.web.ws;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.ws.rs.InternalServerErrorException;

import org.apache.cxf.ext.logging.LoggingInInterceptor;
import org.apache.cxf.ext.logging.LoggingOutInterceptor;
import org.apache.cxf.jaxrs.client.JAXRSClientFactoryBean;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.europa.esig.dss.diagnostic.CertificateWrapper;
import eu.europa.esig.dss.diagnostic.DiagnosticData;
import eu.europa.esig.dss.diagnostic.jaxb.XmlCertificate;
import eu.europa.esig.dss.diagnostic.jaxb.XmlDiagnosticData;
import eu.europa.esig.dss.simplecertificatereport.jaxb.XmlChainItem;
import eu.europa.esig.dss.spi.DSSUtils;
import eu.europa.esig.dss.web.config.CXFConfig;
import eu.europa.esig.dss.ws.cert.validation.dto.CertificateReportsDTO;
import eu.europa.esig.dss.ws.cert.validation.dto.CertificateToValidateDTO;
import eu.europa.esig.dss.ws.cert.validation.rest.client.RestCertificateValidationService;
import eu.europa.esig.dss.ws.converter.RemoteCertificateConverter;
import eu.europa.esig.dss.ws.dto.RemoteCertificate;

public class RestCertificateValidationIT extends AbstractRestIT {

	private RestCertificateValidationService validationService;

	@BeforeEach
	public void init() {
		JAXRSClientFactoryBean factory = new JAXRSClientFactoryBean();

		factory.setAddress(getBaseCxf() + CXFConfig.REST_CERTIFICATE_VALIDATION);
		factory.setServiceClass(RestCertificateValidationService.class);
		factory.setProviders(Arrays.asList(jacksonJsonProvider()));

		LoggingInInterceptor loggingInInterceptor = new LoggingInInterceptor();
		factory.getInInterceptors().add(loggingInInterceptor);
		factory.getInFaultInterceptors().add(loggingInInterceptor);

		LoggingOutInterceptor loggingOutInterceptor = new LoggingOutInterceptor();
		factory.getOutInterceptors().add(loggingOutInterceptor);
		factory.getOutFaultInterceptors().add(loggingOutInterceptor);

		validationService = factory.create(RestCertificateValidationService.class);
	}
	
	@Test
	public void testWithCertificateChainAndValidationTime() {
		RemoteCertificate remoteCertificate = RemoteCertificateConverter.toRemoteCertificate(
				DSSUtils.loadCertificate(new File("src/test/resources/CZ.cer")));
		RemoteCertificate issuerCertificate = RemoteCertificateConverter
				.toRemoteCertificate(DSSUtils.loadCertificate(new File("src/test/resources/CA_CZ.cer")));
		Calendar calendar = Calendar.getInstance();
		calendar.set(2018, 12, 31);
		Date validationDate = calendar.getTime();
		validationDate.setTime((validationDate.getTime() / 1000) * 1000); // clean millis
		CertificateToValidateDTO certificateToValidateDTO = new CertificateToValidateDTO(remoteCertificate, 
				Arrays.asList(issuerCertificate), validationDate);
		
		CertificateReportsDTO reportsDTO = validationService.validateCertificate(certificateToValidateDTO);

		assertNotNull(reportsDTO.getDiagnosticData());
		assertNotNull(reportsDTO.getSimpleCertificateReport());
		assertNotNull(reportsDTO.getDetailedReport());
		
		XmlDiagnosticData xmlDiagnosticData = reportsDTO.getDiagnosticData();
		List<XmlCertificate> usedCertificates = xmlDiagnosticData.getUsedCertificates();
		assertTrue(usedCertificates.size() > 1);
		List<XmlChainItem> chain = reportsDTO.getSimpleCertificateReport().getChain();
		assertTrue(chain.size() > 1);
		
		DiagnosticData diagnosticData = new DiagnosticData(xmlDiagnosticData);
		assertNotNull(diagnosticData);
		
		for (XmlChainItem chainItem : chain) {
			CertificateWrapper certificate = diagnosticData.getUsedCertificateById(chainItem.getId());
			assertNotNull(certificate);
			CertificateWrapper signingCertificate = certificate.getSigningCertificate();
			assertTrue(signingCertificate != null || certificate.isTrusted() && certificate.isSelfSigned());
		}
		assertEquals(0, validationDate.compareTo(diagnosticData.getValidationDate()));
	}
	
	@Test
	public void testWithNoValidationTime() {
		RemoteCertificate remoteCertificate = RemoteCertificateConverter.toRemoteCertificate(
				DSSUtils.loadCertificate(new File("src/test/resources/CZ.cer")));
		RemoteCertificate issuerCertificate = RemoteCertificateConverter
				.toRemoteCertificate(DSSUtils.loadCertificate(new File("src/test/resources/CA_CZ.cer")));
		
		CertificateToValidateDTO certificateToValidateDTO = new CertificateToValidateDTO(remoteCertificate, 
				Arrays.asList(issuerCertificate), null);
		
		CertificateReportsDTO reportsDTO = validationService.validateCertificate(certificateToValidateDTO);

		assertNotNull(reportsDTO.getDiagnosticData());
		assertNotNull(reportsDTO.getSimpleCertificateReport());
		assertNotNull(reportsDTO.getDetailedReport());
		
		XmlDiagnosticData xmlDiagnosticData = reportsDTO.getDiagnosticData();
		List<XmlCertificate> usedCertificates = xmlDiagnosticData.getUsedCertificates();
		assertTrue(usedCertificates.size() > 1);
		List<XmlChainItem> chain = reportsDTO.getSimpleCertificateReport().getChain();
		assertTrue(chain.size() > 1);
		
		DiagnosticData diagnosticData = new DiagnosticData(xmlDiagnosticData);
		assertNotNull(diagnosticData);
		
		for (XmlChainItem chainItem : chain) {
			CertificateWrapper certificate = diagnosticData.getUsedCertificateById(chainItem.getId());
			assertNotNull(certificate);
			CertificateWrapper signingCertificate = certificate.getSigningCertificate();
			assertTrue(signingCertificate != null || certificate.isTrusted() && certificate.isSelfSigned());
		}
		assertNotNull(diagnosticData.getValidationDate());
	}
	
	@Test
	public void testWithNoCertificateChain() {
		RemoteCertificate remoteCertificate = RemoteCertificateConverter.toRemoteCertificate(
				DSSUtils.loadCertificate(new File("src/test/resources/CZ.cer")));
		CertificateToValidateDTO certificateToValidateDTO = new CertificateToValidateDTO(remoteCertificate);
		
		CertificateReportsDTO reportsDTO = validationService.validateCertificate(certificateToValidateDTO);

		assertNotNull(reportsDTO.getDiagnosticData());
		assertNotNull(reportsDTO.getSimpleCertificateReport());
		assertNotNull(reportsDTO.getDetailedReport());
		
		XmlDiagnosticData xmlDiagnosticData = reportsDTO.getDiagnosticData();
		List<XmlCertificate> usedCertificates = xmlDiagnosticData.getUsedCertificates();
		assertTrue(usedCertificates.size() > 1);
		List<XmlChainItem> chain = reportsDTO.getSimpleCertificateReport().getChain();
		assertTrue(chain.size() > 1);
		
		DiagnosticData diagnosticData = new DiagnosticData(xmlDiagnosticData);
		assertNotNull(diagnosticData);
		
		for (XmlChainItem chainItem : chain) {
			CertificateWrapper certificate = diagnosticData.getUsedCertificateById(chainItem.getId());
			assertNotNull(certificate);
			CertificateWrapper signingCertificate = certificate.getSigningCertificate();
			assertTrue(signingCertificate != null || certificate.isTrusted() && certificate.isSelfSigned());
		}
		assertNotNull(diagnosticData.getValidationDate());
	}
	
	@Test
	public void testWithNoCertificateProvided() {
		CertificateToValidateDTO certificateToValidateDTO = new CertificateToValidateDTO(null);
		
		assertThrows(InternalServerErrorException.class, () -> validationService.validateCertificate(certificateToValidateDTO));
	}

}
