package eu.europa.esig.dss.web.ws;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.cxf.ext.logging.LoggingInInterceptor;
import org.apache.cxf.ext.logging.LoggingOutInterceptor;
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;
import org.junit.Before;
import org.junit.Test;

import eu.europa.esig.dss.DSSUtils;
import eu.europa.esig.dss.DataToValidateDTO;
import eu.europa.esig.dss.DigestAlgorithm;
import eu.europa.esig.dss.FileDocument;
import eu.europa.esig.dss.RemoteDocument;
import eu.europa.esig.dss.utils.Utils;
import eu.europa.esig.dss.validation.SoapDocumentValidationService;
import eu.europa.esig.dss.validation.WSReportsDTO;
import eu.europa.esig.dss.validation.policy.rules.Indication;
import eu.europa.esig.dss.validation.reports.Reports;
import eu.europa.esig.dss.web.config.CXFConfig;

public class SoapDocumentValidationIT extends AbstractIT {

	private SoapDocumentValidationService validationService;

	@Before
	public void init() {
		JaxWsProxyFactoryBean factory = new JaxWsProxyFactoryBean();
		factory.setServiceClass(SoapDocumentValidationService.class);

		Map<String, Object> props = new HashMap<String, Object>();
		props.put("mtom-enabled", Boolean.TRUE);
		factory.setProperties(props);

		factory.setAddress(getBaseCxf() + CXFConfig.SOAP_VALIDATION);

		LoggingInInterceptor loggingInInterceptor = new LoggingInInterceptor();
		factory.getInInterceptors().add(loggingInInterceptor);
		factory.getInFaultInterceptors().add(loggingInInterceptor);

		LoggingOutInterceptor loggingOutInterceptor = new LoggingOutInterceptor();
		factory.getOutInterceptors().add(loggingOutInterceptor);
		factory.getOutFaultInterceptors().add(loggingOutInterceptor);

		validationService = factory.create(SoapDocumentValidationService.class);
	}

	@Test
	public void testWithNoPolicyAndNoOriginalFile() throws Exception {

		RemoteDocument signedFile = toRemoteDocument(new FileDocument("src/test/resources/XAdESLTA.xml"));

		DataToValidateDTO toValidate = new DataToValidateDTO(signedFile, (RemoteDocument) null, null);

		WSReportsDTO result = validationService.validateSignature(toValidate);

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

		WSReportsDTO result = validationService.validateSignature(toValidate);

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

		WSReportsDTO result = validationService.validateSignature(toValidate);

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

		WSReportsDTO result = validationService.validateSignature(toValidate);

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

		DataToValidateDTO toValidate = new DataToValidateDTO(signedFile, (RemoteDocument) null, policy);

		WSReportsDTO result = validationService.validateSignature(toValidate);

		assertNotNull(result.getDiagnosticData());
		assertNotNull(result.getDetailedReport());
		assertNotNull(result.getSimpleReport());

		assertEquals(1, result.getSimpleReport().getSignature().size());
		assertEquals(result.getSimpleReport().getSignature().get(0).getIndication(), Indication.INDETERMINATE);

		Reports reports = new Reports(result.getDiagnosticData(), result.getDetailedReport(), result.getSimpleReport());
		assertNotNull(reports);
	}

	@Test
	public void testGetOriginals() throws Exception {
		RemoteDocument signedFile = toRemoteDocument(new FileDocument("src/test/resources/XAdESLTA.xml"));

		DataToValidateDTO toValidate = new DataToValidateDTO();
		toValidate.setSignatureId("id-0d11cae494b17e19234c619a9db45a97");
		toValidate.setSignedDocument(signedFile);
		List<RemoteDocument> result = validationService.getOriginalDocuments(toValidate);
		assertNotNull(result);
		assertEquals(1, result.size());
		RemoteDocument document = result.get(0);
		assertNotNull(document);
		assertNotNull(document.getBytes());
	}

	@Test
	public void testGetOriginalsWithoutId() throws Exception {
		RemoteDocument signedFile = toRemoteDocument(new FileDocument("src/test/resources/XAdESLTA.xml"));

		DataToValidateDTO toValidate = new DataToValidateDTO();
		toValidate.setSignedDocument(signedFile);
		List<RemoteDocument> result = validationService.getOriginalDocuments(toValidate);
		assertNotNull(result);
		assertEquals(1, result.size());
		RemoteDocument document = result.get(0);
		assertNotNull(document);
		assertNotNull(document.getBytes());
	}

	@Test
	public void testGetOriginalsWithWrongId() throws Exception {
		RemoteDocument signedFile = toRemoteDocument(new FileDocument("src/test/resources/XAdESLTA.xml"));

		DataToValidateDTO toValidate = new DataToValidateDTO();
		toValidate.setSignatureId("id-wrong");
		toValidate.setSignedDocument(signedFile);
		List<RemoteDocument> result = validationService.getOriginalDocuments(toValidate);
		// Difference with REST
		assertNull(result);
	}

	private RemoteDocument toRemoteDocument(FileDocument fileDoc) throws IOException {
		return new RemoteDocument(Utils.toByteArray(fileDoc.openStream()), fileDoc.getMimeType(), fileDoc.getName());
	}

}
