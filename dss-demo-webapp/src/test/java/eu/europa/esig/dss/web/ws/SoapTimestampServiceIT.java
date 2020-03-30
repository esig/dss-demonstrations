package eu.europa.esig.dss.web.ws;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.security.KeyStore.PasswordProtection;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.crypto.dsig.CanonicalizationMethod;

import org.apache.cxf.ext.logging.LoggingInInterceptor;
import org.apache.cxf.ext.logging.LoggingOutInterceptor;
import org.apache.cxf.jaxb.JAXBDataBinding;
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.europa.esig.dss.diagnostic.DiagnosticData;
import eu.europa.esig.dss.diagnostic.TimestampWrapper;
import eu.europa.esig.dss.diagnostic.jaxb.XmlDigestAlgoAndValue;
import eu.europa.esig.dss.diagnostic.jaxb.XmlDigestMatcher;
import eu.europa.esig.dss.enumerations.DigestAlgorithm;
import eu.europa.esig.dss.enumerations.SignatureLevel;
import eu.europa.esig.dss.enumerations.SignaturePackaging;
import eu.europa.esig.dss.enumerations.TimestampType;
import eu.europa.esig.dss.model.FileDocument;
import eu.europa.esig.dss.model.SignatureValue;
import eu.europa.esig.dss.spi.DSSUtils;
import eu.europa.esig.dss.token.DSSPrivateKeyEntry;
import eu.europa.esig.dss.token.Pkcs12SignatureToken;
import eu.europa.esig.dss.utils.Utils;
import eu.europa.esig.dss.validation.reports.Reports;
import eu.europa.esig.dss.web.config.CXFConfig;
import eu.europa.esig.dss.ws.converter.DTOConverter;
import eu.europa.esig.dss.ws.dto.DigestDTO;
import eu.europa.esig.dss.ws.dto.RemoteCertificate;
import eu.europa.esig.dss.ws.dto.RemoteDocument;
import eu.europa.esig.dss.ws.dto.SignatureValueDTO;
import eu.europa.esig.dss.ws.dto.TimestampDTO;
import eu.europa.esig.dss.ws.dto.ToBeSignedDTO;
import eu.europa.esig.dss.ws.signature.dto.DataToSignOneDocumentDTO;
import eu.europa.esig.dss.ws.signature.dto.SignOneDocumentDTO;
import eu.europa.esig.dss.ws.signature.dto.parameters.RemoteSignatureParameters;
import eu.europa.esig.dss.ws.signature.soap.client.DateAdapter;
import eu.europa.esig.dss.ws.signature.soap.client.SoapDocumentSignatureService;
import eu.europa.esig.dss.ws.timestamp.dto.TimestampResponseDTO;
import eu.europa.esig.dss.ws.timestamp.remote.soap.client.SoapTimestampService;
import eu.europa.esig.dss.ws.validation.dto.DataToValidateDTO;
import eu.europa.esig.dss.ws.validation.dto.WSReportsDTO;
import eu.europa.esig.dss.ws.validation.soap.client.SoapDocumentValidationService;
import eu.europa.esig.dss.xades.DSSXMLUtils;

public class SoapTimestampServiceIT extends AbstractIT {

	private SoapTimestampService timestampService;

	@BeforeEach
	public void init() {
		JaxWsProxyFactoryBean factory = new JaxWsProxyFactoryBean();
		factory.setServiceClass(SoapTimestampService.class);

		Map<String, Object> props = new HashMap<String, Object>();
		props.put("mtom-enabled", Boolean.TRUE);
//		props.put("jaxb.additionalContextClasses", getExtraClasses());
		factory.setProperties(props);

		factory.setAddress(getBaseCxf() + CXFConfig.SOAP_TIMESTAMP_SERVICE);

		LoggingInInterceptor loggingInInterceptor = new LoggingInInterceptor();
		factory.getInInterceptors().add(loggingInInterceptor);
		factory.getInFaultInterceptors().add(loggingInInterceptor);

		LoggingOutInterceptor loggingOutInterceptor = new LoggingOutInterceptor();
		factory.getOutInterceptors().add(loggingOutInterceptor);
		factory.getOutFaultInterceptors().add(loggingOutInterceptor);

		timestampService = factory.create(SoapTimestampService.class);
	}
	
	@Test
	public void simpleTest() {
		byte[] contentToBeTimestamped = "Hello World!".getBytes();
		byte[] digestValue = DSSUtils.digest(DigestAlgorithm.SHA1, contentToBeTimestamped);
		DigestDTO digest = new DigestDTO(DigestAlgorithm.SHA1, digestValue);
		TimestampResponseDTO timestampResponse = timestampService.getTimestampResponse(digest);
		assertNotNull(timestampResponse);
		assertTrue(Utils.isArrayNotEmpty(timestampResponse.getBinaries()));
	}
	
	@Test
	public void signWithContentTimestampAndValidateTest() throws Exception {
		
		/* Init SoapDocumentSignatureService */
		JAXBDataBinding dataBinding = new JAXBDataBinding();
		dataBinding.getConfiguredXmlAdapters().add(new DateAdapter());

		Map<String, Object> props = new HashMap<String, Object>();
		props.put("mtom-enabled", Boolean.TRUE);

		JaxWsProxyFactoryBean factory = new JaxWsProxyFactoryBean();
		factory.setServiceClass(SoapDocumentSignatureService.class);
		factory.setProperties(props);
		factory.setDataBinding(dataBinding);
		factory.setAddress(getBaseCxf() + CXFConfig.SOAP_SIGNATURE_ONE_DOCUMENT);

		LoggingInInterceptor loggingInInterceptor = new LoggingInInterceptor();
		factory.getInInterceptors().add(loggingInInterceptor);
		factory.getInFaultInterceptors().add(loggingInInterceptor);

		LoggingOutInterceptor loggingOutInterceptor = new LoggingOutInterceptor();
		factory.getOutInterceptors().add(loggingOutInterceptor);
		factory.getOutFaultInterceptors().add(loggingOutInterceptor);

		SoapDocumentSignatureService soapClient = factory.create(SoapDocumentSignatureService.class);
		
		/* Create a content timestamp */
		FileDocument fileToSign = new FileDocument(new File("src/test/resources/sample.xml"));
		
		byte[] digestValue = DSSUtils.digest(DigestAlgorithm.SHA1, DSSXMLUtils.canonicalize(
				CanonicalizationMethod.EXCLUSIVE, DSSUtils.toByteArray(fileToSign)));
		DigestDTO digest = new DigestDTO(DigestAlgorithm.SHA1, digestValue);
		TimestampResponseDTO timeStampResponse = timestampService.getTimestampResponse(digest);
		
		TimestampDTO contentTimestamp = new TimestampDTO(timeStampResponse.getBinaries(), TimestampType.ALL_DATA_OBJECTS_TIMESTAMP);
		contentTimestamp.setCanonicalizationMethod(CanonicalizationMethod.EXCLUSIVE);
		
		/* Sign the document */
		RemoteDocument signedDocument;
		try (Pkcs12SignatureToken token = new Pkcs12SignatureToken(new FileInputStream("src/test/resources/user_a_rsa.p12"),
				new PasswordProtection("password".toCharArray()))) {

			List<DSSPrivateKeyEntry> keys = token.getKeys();
			DSSPrivateKeyEntry dssPrivateKeyEntry = keys.get(0);

			RemoteSignatureParameters parameters = new RemoteSignatureParameters();
			parameters.setSignatureLevel(SignatureLevel.XAdES_BASELINE_B);
			parameters.setSigningCertificate(new RemoteCertificate(dssPrivateKeyEntry.getCertificate().getCertificate().getEncoded()));
			parameters.setSignaturePackaging(SignaturePackaging.ENVELOPING);
			parameters.setDigestAlgorithm(DigestAlgorithm.SHA256);
			/* set the content timestamp */
			parameters.setContentTimestamps(Arrays.asList(contentTimestamp));

			RemoteDocument toSignDocument = new RemoteDocument(Utils.toByteArray(fileToSign.openStream()), fileToSign.getName());
			ToBeSignedDTO dataToSign = soapClient.getDataToSign(new DataToSignOneDocumentDTO(toSignDocument, parameters));
			assertNotNull(dataToSign);

			SignatureValue signatureValue = token.sign(DTOConverter.toToBeSigned(dataToSign), DigestAlgorithm.SHA256, dssPrivateKeyEntry);
			SignOneDocumentDTO signDocument = new SignOneDocumentDTO(toSignDocument, parameters,
					new SignatureValueDTO(signatureValue.getAlgorithm(), signatureValue.getValue()));
			signedDocument = soapClient.signDocument(signDocument);
		}
		assertNotNull(signedDocument);
		
		/* Init SoapDocumentValidationService */
		factory = new JaxWsProxyFactoryBean();
		factory.setServiceClass(SoapDocumentValidationService.class);

		props = new HashMap<String, Object>();
		props.put("mtom-enabled", Boolean.TRUE);
//		props.put("jaxb.additionalContextClasses", getExtraClasses());
		factory.setProperties(props);

		factory.setAddress(getBaseCxf() + CXFConfig.SOAP_VALIDATION);

		loggingInInterceptor = new LoggingInInterceptor();
		factory.getInInterceptors().add(loggingInInterceptor);
		factory.getInFaultInterceptors().add(loggingInInterceptor);

		loggingOutInterceptor = new LoggingOutInterceptor();
		factory.getOutInterceptors().add(loggingOutInterceptor);
		factory.getOutFaultInterceptors().add(loggingOutInterceptor);

		SoapDocumentValidationService validationService = factory.create(SoapDocumentValidationService.class);
		
		/* Validate */
		DataToValidateDTO toValidate = new DataToValidateDTO(signedDocument, (RemoteDocument) null, null);

		WSReportsDTO result = validationService.validateSignature(toValidate);
		Reports reports = new Reports(result.getDiagnosticData(), result.getDetailedReport(), result.getSimpleReport(), 
				result.getValidationReport());
		
		/* Check the timestamp*/
		DiagnosticData diagnosticData = reports.getDiagnosticData();
		List<TimestampWrapper> timestampList = diagnosticData.getTimestampList();
		assertNotNull(timestampList);
		assertEquals(1, timestampList.size());
		
		TimestampWrapper timestamp = timestampList.get(0);
		assertTrue(timestamp.getType().isContentTimestamp());
		assertEquals(TimestampType.ALL_DATA_OBJECTS_TIMESTAMP, timestamp.getType());
		
		XmlDigestAlgoAndValue digestAlgoAndValue = timestamp.getDigestAlgoAndValue();
		assertNotNull(digestAlgoAndValue);
		assertTrue(Arrays.equals(digestAlgoAndValue.getDigestValue(), DSSUtils.digest(digestAlgoAndValue.getDigestMethod(), timeStampResponse.getBinaries())));
		
		List<XmlDigestMatcher> digestMatchers = timestamp.getDigestMatchers();
		assertEquals(1, digestMatchers.size());
		XmlDigestMatcher timestampDigestMatcher = digestMatchers.get(0);
		assertTrue(timestampDigestMatcher.isDataFound());
		assertTrue(timestampDigestMatcher.isDataIntact());
		
	}

}
