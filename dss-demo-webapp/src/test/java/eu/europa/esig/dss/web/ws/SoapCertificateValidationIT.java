package eu.europa.esig.dss.web.ws;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.ws.soap.SOAPFaultException;

import org.apache.cxf.ext.logging.LoggingInInterceptor;
import org.apache.cxf.ext.logging.LoggingOutInterceptor;
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.europa.esig.dss.diagnostic.CertificateWrapper;
import eu.europa.esig.dss.diagnostic.DiagnosticData;
import eu.europa.esig.dss.diagnostic.jaxb.XmlCertificate;
import eu.europa.esig.dss.diagnostic.jaxb.XmlDiagnosticData;
import eu.europa.esig.dss.simplecertificatereport.jaxb.XmlChainItem;
import eu.europa.esig.dss.spi.DSSUtils;
import eu.europa.esig.dss.web.config.CXFConfig;
import eu.europa.esig.dss.ws.cert.validation.dto.CertificateToValidateDTO;
import eu.europa.esig.dss.ws.cert.validation.soap.client.SoapCertificateValidationService;
import eu.europa.esig.dss.ws.cert.validation.soap.client.WSCertificateReportsDTO;
import eu.europa.esig.dss.ws.converter.RemoteCertificateConverter;
import eu.europa.esig.dss.ws.dto.RemoteCertificate;

public class SoapCertificateValidationIT extends AbstractIT {

	private SoapCertificateValidationService validationService;

	@BeforeEach
	public void init() {
		JaxWsProxyFactoryBean factory = new JaxWsProxyFactoryBean();
		factory.setServiceClass(SoapCertificateValidationService.class);

		Map<String, Object> props = new HashMap<String, Object>();
		props.put("mtom-enabled", Boolean.TRUE);
//		props.put("jaxb.additionalContextClasses", getExtraClasses());
		factory.setProperties(props);

		factory.setAddress(getBaseCxf() + CXFConfig.SOAP_CERTIFICATE_VALIDATION);

		LoggingInInterceptor loggingInInterceptor = new LoggingInInterceptor();
		factory.getInInterceptors().add(loggingInInterceptor);
		factory.getInFaultInterceptors().add(loggingInInterceptor);

		LoggingOutInterceptor loggingOutInterceptor = new LoggingOutInterceptor();
		factory.getOutInterceptors().add(loggingOutInterceptor);
		factory.getOutFaultInterceptors().add(loggingOutInterceptor);

		validationService = factory.create(SoapCertificateValidationService.class);
	}

	@Test
	public void testWithCertificateChainAndValidationTime() {
		RemoteCertificate remoteCertificate = RemoteCertificateConverter.toRemoteCertificate(DSSUtils.loadCertificate(new File("src/test/resources/CZ.cer")));
		RemoteCertificate issuerCertificate = RemoteCertificateConverter
				.toRemoteCertificate(DSSUtils.loadCertificate(new File("src/test/resources/CA_CZ.cer")));
		Calendar calendar = Calendar.getInstance();
		calendar.set(2018, 12, 31);
		Date validationDate = calendar.getTime();
		validationDate.setTime((validationDate.getTime() / 1000) * 1000); // clean millis
		CertificateToValidateDTO certificateToValidateDTO = new CertificateToValidateDTO(remoteCertificate, Arrays.asList(issuerCertificate), validationDate);

		WSCertificateReportsDTO reportsDTO = validationService.validateCertificate(certificateToValidateDTO);

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
		assertTrue(validationDate.compareTo(diagnosticData.getValidationDate()) == 0);
	}

	@Test
	public void testWithNoValidationTime() {
		RemoteCertificate remoteCertificate = RemoteCertificateConverter.toRemoteCertificate(DSSUtils.loadCertificate(new File("src/test/resources/CZ.cer")));
		RemoteCertificate issuerCertificate = RemoteCertificateConverter
				.toRemoteCertificate(DSSUtils.loadCertificate(new File("src/test/resources/CA_CZ.cer")));

		CertificateToValidateDTO certificateToValidateDTO = new CertificateToValidateDTO(remoteCertificate, Arrays.asList(issuerCertificate), null);

		WSCertificateReportsDTO reportsDTO = validationService.validateCertificate(certificateToValidateDTO);

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
		RemoteCertificate remoteCertificate = RemoteCertificateConverter.toRemoteCertificate(DSSUtils.loadCertificate(new File("src/test/resources/CZ.cer")));
		CertificateToValidateDTO certificateToValidateDTO = new CertificateToValidateDTO(remoteCertificate);

		WSCertificateReportsDTO reportsDTO = validationService.validateCertificate(certificateToValidateDTO);

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

		assertThrows(SOAPFaultException.class, () -> validationService.validateCertificate(certificateToValidateDTO));
	}

}
