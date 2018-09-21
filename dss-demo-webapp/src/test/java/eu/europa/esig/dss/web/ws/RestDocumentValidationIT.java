package eu.europa.esig.dss.web.ws;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.util.Arrays;

import org.apache.cxf.ext.logging.LoggingInInterceptor;
import org.apache.cxf.ext.logging.LoggingOutInterceptor;
import org.apache.cxf.jaxrs.client.JAXRSClientFactoryBean;
import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;

import eu.europa.esig.dss.DSSUtils;
import eu.europa.esig.dss.DataToValidateDTO;
import eu.europa.esig.dss.DigestAlgorithm;
import eu.europa.esig.dss.FileDocument;
import eu.europa.esig.dss.RemoteDocument;
import eu.europa.esig.dss.utils.Utils;
import eu.europa.esig.dss.validation.RestDocumentValidationService;
import eu.europa.esig.dss.validation.policy.rules.Indication;
import eu.europa.esig.dss.validation.reports.Reports;
import eu.europa.esig.dss.validation.reports.dto.ReportsDTO;
import eu.europa.esig.dss.web.config.CXFConfig;

public class RestDocumentValidationIT extends AbstractIT {

	private RestDocumentValidationService validationService;

	@Before
	public void init() {
		JAXRSClientFactoryBean factory = new JAXRSClientFactoryBean();

		factory.setAddress(getBaseCxf() + CXFConfig.REST_VALIDATION);
		factory.setServiceClass(RestDocumentValidationService.class);
		factory.setProviders(Arrays.asList(new JacksonJsonProvider()));

		LoggingInInterceptor loggingInInterceptor = new LoggingInInterceptor();
		factory.getInInterceptors().add(loggingInInterceptor);
		factory.getInFaultInterceptors().add(loggingInInterceptor);

		LoggingOutInterceptor loggingOutInterceptor = new LoggingOutInterceptor();
		factory.getOutInterceptors().add(loggingOutInterceptor);
		factory.getOutFaultInterceptors().add(loggingOutInterceptor);

		validationService = factory.create(RestDocumentValidationService.class);
	}

	@Test
	public void testWithNoPolicyAndNoOriginalFile() throws Exception {
		RemoteDocument signedFile = toRemoteDocument(new FileDocument("src/test/resources/XAdESLTA.xml"));

		DataToValidateDTO toValidate = new DataToValidateDTO(signedFile, null, null);

		ReportsDTO result = validationService.validateSignature(toValidate);

		assertNotNull(result.getDiagnosticData());
		assertNotNull(result.getDetailedReport());
		assertNotNull(result.getSimpleReport());

		assertEquals(1, result.getSimpleReport().getSignature().size());
		assertEquals(2, result.getDiagnosticData().getSignatures().get(0).getTimestamps().size());
		assertEquals(result.getSimpleReport().getSignature().get(0).getIndication(), Indication.INDETERMINATE);

		Reports reports = new Reports(result.getDiagnosticData(), result.getDetailedReport(), result.getSimpleReport());
		assertNotNull(reports);
	}

	@Test
	public void testWithNoPolicyAndOriginalFile() throws Exception {

		RemoteDocument signedFile = toRemoteDocument(new FileDocument("src/test/resources/xades-detached.xml"));
		RemoteDocument originalFile = toRemoteDocument(new FileDocument("src/test/resources/sample.xml"));

		DataToValidateDTO toValidate = new DataToValidateDTO(signedFile, originalFile, null);

		ReportsDTO result = validationService.validateSignature(toValidate);

		assertNotNull(result.getDiagnosticData());
		assertNotNull(result.getDetailedReport());
		assertNotNull(result.getSimpleReport());

		assertEquals(1, result.getSimpleReport().getSignature().size());
		assertEquals(result.getSimpleReport().getSignature().get(0).getIndication(), Indication.TOTAL_FAILED);

		Reports reports = new Reports(result.getDiagnosticData(), result.getDetailedReport(), result.getSimpleReport());
		assertNotNull(reports);
	}

	@Test
	public void testWithNoPolicyAndDigestOriginalFile() throws Exception {

		RemoteDocument signedFile = toRemoteDocument(new FileDocument("src/test/resources/xades-detached.xml"));

		FileDocument fileDocument = new FileDocument("src/test/resources/sample.xml");
		RemoteDocument originalFile = new RemoteDocument(DSSUtils.digest(DigestAlgorithm.SHA256, fileDocument), fileDocument.getMimeType(),
				fileDocument.getName());

		DataToValidateDTO toValidate = new DataToValidateDTO(signedFile, originalFile, null);

		ReportsDTO result = validationService.validateSignature(toValidate);

		assertNotNull(result.getDiagnosticData());
		assertNotNull(result.getDetailedReport());
		assertNotNull(result.getSimpleReport());

		assertEquals(1, result.getSimpleReport().getSignature().size());
		assertEquals(result.getSimpleReport().getSignature().get(0).getIndication(), Indication.TOTAL_FAILED);

		Reports reports = new Reports(result.getDiagnosticData(), result.getDetailedReport(), result.getSimpleReport());
		assertNotNull(reports);
	}

	@Test
	public void testWithPolicyAndOriginalFile() throws Exception {

		RemoteDocument signedFile = toRemoteDocument(new FileDocument("src/test/resources/xades-detached.xml"));
		RemoteDocument originalFile = toRemoteDocument(new FileDocument("src/test/resources/sample.xml"));
		RemoteDocument policy = toRemoteDocument(new FileDocument("src/test/resources/constraint.xml"));

		DataToValidateDTO toValidate = new DataToValidateDTO(signedFile, originalFile, policy);

		ReportsDTO result = validationService.validateSignature(toValidate);

		assertNotNull(result.getDiagnosticData());
		assertNotNull(result.getDetailedReport());
		assertNotNull(result.getSimpleReport());

		assertEquals(1, result.getSimpleReport().getSignature().size());
		assertEquals(result.getSimpleReport().getSignature().get(0).getIndication(), Indication.TOTAL_FAILED);

		Reports reports = new Reports(result.getDiagnosticData(), result.getDetailedReport(), result.getSimpleReport());
		assertNotNull(reports);
	}

	@Test
	public void testWithPolicyAndNoOriginalFile() throws Exception {

		RemoteDocument signedFile = toRemoteDocument(new FileDocument("src/test/resources/xades-detached.xml"));
		RemoteDocument policy = toRemoteDocument(new FileDocument("src/test/resources/constraint.xml"));

		DataToValidateDTO toValidate = new DataToValidateDTO(signedFile, null, policy);

		ReportsDTO result = validationService.validateSignature(toValidate);

		assertNotNull(result.getDiagnosticData());
		assertNotNull(result.getDetailedReport());
		assertNotNull(result.getSimpleReport());

		assertEquals(1, result.getSimpleReport().getSignature().size());
		assertEquals(result.getSimpleReport().getSignature().get(0).getIndication(), Indication.INDETERMINATE);

		Reports reports = new Reports(result.getDiagnosticData(), result.getDetailedReport(), result.getSimpleReport());
		assertNotNull(reports);
	}

	private RemoteDocument toRemoteDocument(FileDocument fileDoc) throws IOException {
		return new RemoteDocument(Utils.toByteArray(fileDoc.openStream()), fileDoc.getMimeType(), fileDoc.getName());
	}

}
