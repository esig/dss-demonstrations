package eu.europa.esig.dss.web.ws;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;
import org.junit.Before;
import org.junit.Test;

import eu.europa.esig.dss.DataToValidateDTO;
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
		validationService = (SoapDocumentValidationService) factory.create();
	}

	@Test
	public void testWithNoPolicyAndNoOriginalFile() throws Exception {

		RemoteDocument signedFile = toRemoteDocument(new FileDocument("src/test/resources/XAdESLTA.xml"));

		DataToValidateDTO toValidate = new DataToValidateDTO(signedFile, null, null);

		WSReportsDTO result = validationService.validateSignature(toValidate);

		assertNotNull(result.getDiagnosticData());
		assertNotNull(result.getDetailedReport());
		assertNotNull(result.getSimpleReport());

		assertEquals(1, result.getSimpleReport().getSignature().size());
		assertEquals(2, result.getDiagnosticData().getSignatures().get(0).getTimestamps().size());
		assertEquals(result.getSimpleReport().getSignature().get(0).getIndication(), Indication.TOTAL_PASSED);

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

		DataToValidateDTO toValidate = new DataToValidateDTO(signedFile, null, policy);

		WSReportsDTO result = validationService.validateSignature(toValidate);

		assertNotNull(result.getDiagnosticData());
		assertNotNull(result.getDetailedReport());
		assertNotNull(result.getSimpleReport());

		assertEquals(1, result.getSimpleReport().getSignature().size());
		assertEquals(result.getSimpleReport().getSignature().get(0).getIndication(), Indication.INDETERMINATE);

		Reports reports = new Reports(result.getDiagnosticData(), result.getDetailedReport(), result.getSimpleReport());
		assertNotNull(reports);
	}

	private RemoteDocument toRemoteDocument(FileDocument fileDoc) throws IOException {
		return new RemoteDocument(Utils.toByteArray(fileDoc.openStream()), fileDoc.getMimeType(), fileDoc.getName(), fileDoc.getAbsolutePath());
	}

}
