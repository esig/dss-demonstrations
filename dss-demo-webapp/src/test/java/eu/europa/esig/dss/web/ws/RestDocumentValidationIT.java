package eu.europa.esig.dss.web.ws;


import eu.europa.esig.dss.enumerations.DigestAlgorithm;
import eu.europa.esig.dss.enumerations.Indication;
import eu.europa.esig.dss.enumerations.SubIndication;
import eu.europa.esig.dss.model.FileDocument;
import eu.europa.esig.dss.model.InMemoryDocument;
import eu.europa.esig.dss.simplereport.SimpleReport;
import eu.europa.esig.dss.simplereport.jaxb.XmlEvidenceRecord;
import eu.europa.esig.dss.spi.DSSUtils;
import eu.europa.esig.dss.utils.Utils;
import eu.europa.esig.dss.validation.reports.Reports;
import eu.europa.esig.dss.web.config.CXFConfig;
import eu.europa.esig.dss.ws.converter.RemoteDocumentConverter;
import eu.europa.esig.dss.ws.dto.RemoteDocument;
import eu.europa.esig.dss.ws.validation.dto.DataToValidateDTO;
import eu.europa.esig.dss.ws.validation.dto.WSReportsDTO;
import eu.europa.esig.dss.ws.validation.rest.client.RestDocumentValidationService;
import org.apache.cxf.ext.logging.LoggingInInterceptor;
import org.apache.cxf.ext.logging.LoggingOutInterceptor;
import org.apache.cxf.jaxrs.client.JAXRSClientFactoryBean;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class RestDocumentValidationIT extends AbstractRestIT {

	private RestDocumentValidationService validationService;

	@BeforeEach
	public void init() {
		JAXRSClientFactoryBean factory = new JAXRSClientFactoryBean();

		factory.setAddress(getBaseCxf() + CXFConfig.REST_VALIDATION);
		factory.setServiceClass(RestDocumentValidationService.class);
		factory.setProviders(Arrays.asList(jacksonJsonProvider()));

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
		RemoteDocument signedFile = RemoteDocumentConverter.toRemoteDocument(new FileDocument("src/test/resources/XAdESLTA.xml"));

		DataToValidateDTO toValidate = new DataToValidateDTO(signedFile, (RemoteDocument) null, null);

		WSReportsDTO result = validationService.validateSignature(toValidate);

		assertNotNull(result.getDiagnosticData());
		assertNotNull(result.getDetailedReport());
		assertNotNull(result.getSimpleReport());
		assertNotNull(result.getValidationReport());

		assertEquals(1, result.getSimpleReport().getSignatureOrTimestampOrEvidenceRecord().size());
		assertEquals(2, result.getDiagnosticData().getSignatures().get(0).getFoundTimestamps().size());
		assertEquals(result.getSimpleReport().getSignatureOrTimestampOrEvidenceRecord().get(0).getIndication(), Indication.INDETERMINATE);

		Reports reports = new Reports(result.getDiagnosticData(), result.getDetailedReport(), result.getSimpleReport(), 
				result.getValidationReport());
		assertNotNull(reports);
	}

	@Test
	public void testWithNoPolicyAndOriginalFile() throws Exception {

		RemoteDocument signedFile = RemoteDocumentConverter.toRemoteDocument(new FileDocument("src/test/resources/xades-detached.xml"));
		RemoteDocument originalFile = RemoteDocumentConverter.toRemoteDocument(new FileDocument("src/test/resources/sample.xml"));

		DataToValidateDTO toValidate = new DataToValidateDTO(signedFile, originalFile, null);

		WSReportsDTO result = validationService.validateSignature(toValidate);

		assertNotNull(result.getDiagnosticData());
		assertNotNull(result.getDetailedReport());
		assertNotNull(result.getSimpleReport());
		assertNotNull(result.getValidationReport());

		assertEquals(1, result.getSimpleReport().getSignatureOrTimestampOrEvidenceRecord().size());
		assertEquals(Indication.INDETERMINATE, result.getSimpleReport().getSignatureOrTimestampOrEvidenceRecord().get(0).getIndication());
		assertEquals(SubIndication.NO_CERTIFICATE_CHAIN_FOUND, result.getSimpleReport().getSignatureOrTimestampOrEvidenceRecord().get(0).getSubIndication());

		Reports reports = new Reports(result.getDiagnosticData(), result.getDetailedReport(), result.getSimpleReport(), 
				result.getValidationReport());
		assertNotNull(reports);
	}

	@Test
	public void testWithNoPolicyAndDigestOriginalFile() throws Exception {

		RemoteDocument signedFile = RemoteDocumentConverter.toRemoteDocument(new FileDocument("src/test/resources/xades-detached.xml"));

		FileDocument fileDocument = new FileDocument("src/test/resources/sample.xml");
		RemoteDocument originalFile = new RemoteDocument(DSSUtils.digest(DigestAlgorithm.SHA256, fileDocument), DigestAlgorithm.SHA256, fileDocument.getName());

		DataToValidateDTO toValidate = new DataToValidateDTO(signedFile, originalFile, null);

		WSReportsDTO result = validationService.validateSignature(toValidate);

		assertNotNull(result.getDiagnosticData());
		assertNotNull(result.getDetailedReport());
		assertNotNull(result.getSimpleReport());
		assertNotNull(result.getValidationReport());

		assertEquals(1, result.getSimpleReport().getSignatureOrTimestampOrEvidenceRecord().size());
		assertEquals(Indication.INDETERMINATE, result.getSimpleReport().getSignatureOrTimestampOrEvidenceRecord().get(0).getIndication());
		assertEquals(SubIndication.NO_CERTIFICATE_CHAIN_FOUND, result.getSimpleReport().getSignatureOrTimestampOrEvidenceRecord().get(0).getSubIndication());

		Reports reports = new Reports(result.getDiagnosticData(), result.getDetailedReport(), result.getSimpleReport(), 
				result.getValidationReport());
		assertNotNull(reports);
	}

	@Test
	public void testWithPolicyAndOriginalFile() throws Exception {

		RemoteDocument signedFile = RemoteDocumentConverter.toRemoteDocument(new FileDocument("src/test/resources/xades-detached.xml"));
		RemoteDocument originalFile = RemoteDocumentConverter.toRemoteDocument(new FileDocument("src/test/resources/sample.xml"));
		RemoteDocument policy = RemoteDocumentConverter.toRemoteDocument(new FileDocument("src/test/resources/constraint.xml"));

		DataToValidateDTO toValidate = new DataToValidateDTO(signedFile, originalFile, policy);

		WSReportsDTO result = validationService.validateSignature(toValidate);

		assertNotNull(result.getDiagnosticData());
		assertNotNull(result.getDetailedReport());
		assertNotNull(result.getSimpleReport());
		assertNotNull(result.getValidationReport());

		assertEquals(1, result.getSimpleReport().getSignatureOrTimestampOrEvidenceRecord().size());
		assertEquals(Indication.INDETERMINATE, result.getSimpleReport().getSignatureOrTimestampOrEvidenceRecord().get(0).getIndication());
		assertEquals(SubIndication.NO_CERTIFICATE_CHAIN_FOUND, result.getSimpleReport().getSignatureOrTimestampOrEvidenceRecord().get(0).getSubIndication());

		Reports reports = new Reports(result.getDiagnosticData(), result.getDetailedReport(), result.getSimpleReport(), 
				result.getValidationReport());
		assertNotNull(reports);
	}

	@Test
	public void testWithPolicyAndNoOriginalFile() throws Exception {

		RemoteDocument signedFile = RemoteDocumentConverter.toRemoteDocument(new FileDocument("src/test/resources/xades-detached.xml"));
		RemoteDocument policy = RemoteDocumentConverter.toRemoteDocument(new FileDocument("src/test/resources/constraint.xml"));

		DataToValidateDTO toValidate = new DataToValidateDTO(signedFile, (RemoteDocument) null, policy);

		WSReportsDTO result = validationService.validateSignature(toValidate);

		assertNotNull(result.getDiagnosticData());
		assertNotNull(result.getDetailedReport());
		assertNotNull(result.getSimpleReport());
		assertNotNull(result.getValidationReport());

		assertEquals(1, result.getSimpleReport().getSignatureOrTimestampOrEvidenceRecord().size());
		assertEquals(Indication.INDETERMINATE, result.getSimpleReport().getSignatureOrTimestampOrEvidenceRecord().get(0).getIndication());
		assertEquals(SubIndication.SIGNED_DATA_NOT_FOUND, result.getSimpleReport().getSignatureOrTimestampOrEvidenceRecord().get(0).getSubIndication());

		Reports reports = new Reports(result.getDiagnosticData(), result.getDetailedReport(), result.getSimpleReport(), 
				result.getValidationReport());
		assertNotNull(reports);
	}

	@Test
	public void testGetOriginals() throws Exception {
		RemoteDocument signedFile = RemoteDocumentConverter.toRemoteDocument(new FileDocument("src/test/resources/XAdESLTA.xml"));

		DataToValidateDTO toValidate = new DataToValidateDTO();
		toValidate.setSignedDocument(signedFile);

		WSReportsDTO reports = validationService.validateSignature(toValidate);
		toValidate.setSignatureId(reports.getDiagnosticData().getSignatures().get(0).getId());
		
		List<RemoteDocument> result = validationService.getOriginalDocuments(toValidate);
		assertNotNull(result);
		assertEquals(1, result.size());
		RemoteDocument document = result.get(0);
		assertNotNull(document);
		assertNotNull(document.getBytes());
	}

	@Test
	public void testGetOriginalsWithoutId() throws Exception {
		RemoteDocument signedFile = RemoteDocumentConverter.toRemoteDocument(new FileDocument("src/test/resources/XAdESLTA.xml"));

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
		RemoteDocument signedFile = RemoteDocumentConverter.toRemoteDocument(new FileDocument("src/test/resources/XAdESLTA.xml"));

		DataToValidateDTO toValidate = new DataToValidateDTO();
		toValidate.setSignatureId("id-wrong");
		toValidate.setSignedDocument(signedFile);
		List<RemoteDocument> result = validationService.getOriginalDocuments(toValidate);
		// Difference with SOAP
		assertNotNull(result);
		assertEquals(0, result.size());
	}

	@Test
	public void testValidateTimestamp() {
		RemoteDocument timestampDocument = RemoteDocumentConverter.toRemoteDocument(new FileDocument("src/test/resources/d-trust.tsr"));
		RemoteDocument timestampedContent = RemoteDocumentConverter.toRemoteDocument(new InMemoryDocument("Test123".getBytes()));
		DataToValidateDTO dto = new DataToValidateDTO(timestampDocument, timestampedContent, null);

		WSReportsDTO result = validationService.validateSignature(dto);
		assertNotNull(result.getDiagnosticData());
		assertNotNull(result.getDetailedReport());
		assertNotNull(result.getSimpleReport());
		assertNotNull(result.getValidationReport());

		Reports reports = new Reports(result.getDiagnosticData(), result.getDetailedReport(), result.getSimpleReport(), result.getValidationReport());

		SimpleReport simpleReport = reports.getSimpleReport();
		assertEquals(0, simpleReport.getSignaturesCount());
		assertEquals(1, simpleReport.getTimestampIdList().size());
	}

	@Test
	public void testValidateEvidenceRecord() {
		RemoteDocument erDocument = RemoteDocumentConverter.toRemoteDocument(new FileDocument("src/test/resources/evidence-record.xml"));
		RemoteDocument originalFile = new RemoteDocument(Utils.fromBase64("dCeyHarzzN3cWzVNTMKZyY00rW4gNGGto/2ZLfzpsXM="), DigestAlgorithm.SHA256, "signed-file");
		DataToValidateDTO dto = new DataToValidateDTO(erDocument, originalFile, null);

		WSReportsDTO result = validationService.validateSignature(dto);
		assertNotNull(result.getDiagnosticData());
		assertNotNull(result.getDetailedReport());
		assertNotNull(result.getSimpleReport());
		assertNotNull(result.getValidationReport());

		Reports reports = new Reports(result.getDiagnosticData(), result.getDetailedReport(), result.getSimpleReport(), result.getValidationReport());

		SimpleReport simpleReport = reports.getSimpleReport();
		assertEquals(0, simpleReport.getSignaturesCount());
		assertEquals(0, simpleReport.getTimestampIdList().size());
		assertEquals(1, simpleReport.getEvidenceRecordIdList().size());
	}

	@Test
	public void testValidateSignatureWithDetachedEvidenceRecord() {
		RemoteDocument signatureDocument = RemoteDocumentConverter.toRemoteDocument(new FileDocument("src/test/resources/Signature-X-LT.xml"));
		RemoteDocument erDocument = RemoteDocumentConverter.toRemoteDocument(new FileDocument("src/test/resources/evidence-record.xml"));

		DataToValidateDTO dto = new DataToValidateDTO();
		dto.setSignedDocument(signatureDocument);
		dto.setEvidenceRecords(Collections.singletonList(erDocument));

		WSReportsDTO result = validationService.validateSignature(dto);
		assertNotNull(result.getDiagnosticData());
		assertNotNull(result.getDetailedReport());
		assertNotNull(result.getSimpleReport());
		assertNotNull(result.getValidationReport());

		Reports reports = new Reports(result.getDiagnosticData(), result.getDetailedReport(), result.getSimpleReport(), result.getValidationReport());

		SimpleReport simpleReport = reports.getSimpleReport();
		assertEquals(2, simpleReport.getSignaturesCount());
		assertEquals(0, simpleReport.getTimestampIdList().size());

		List<XmlEvidenceRecord> signatureEvidenceRecords = simpleReport.getSignatureEvidenceRecords(simpleReport.getFirstSignatureId());
		assertEquals(1, signatureEvidenceRecords.size());
	}

}
