package eu.europa.esig.dss.web.ws;

import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.cxf.ext.logging.LoggingInInterceptor;
import org.apache.cxf.ext.logging.LoggingOutInterceptor;
import org.apache.cxf.jaxb.JAXBDataBinding;
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;
import org.junit.Before;
import org.junit.Test;

import eu.europa.esig.dss.ASiCContainerType;
import eu.europa.esig.dss.DSSUtils;
import eu.europa.esig.dss.DigestAlgorithm;
import eu.europa.esig.dss.FileDocument;
import eu.europa.esig.dss.InMemoryDocument;
import eu.europa.esig.dss.MimeType;
import eu.europa.esig.dss.RemoteCertificate;
import eu.europa.esig.dss.RemoteDocument;
import eu.europa.esig.dss.RemoteSignatureParameters;
import eu.europa.esig.dss.SignatureAlgorithm;
import eu.europa.esig.dss.SignatureLevel;
import eu.europa.esig.dss.SignaturePackaging;
import eu.europa.esig.dss.SignatureValue;
import eu.europa.esig.dss.ToBeSigned;
import eu.europa.esig.dss.signature.DataToSignMultipleDocumentsDTO;
import eu.europa.esig.dss.signature.DataToSignOneDocumentDTO;
import eu.europa.esig.dss.signature.DateAdapter;
import eu.europa.esig.dss.signature.ExtendDocumentDTO;
import eu.europa.esig.dss.signature.SignMultipleDocumentDTO;
import eu.europa.esig.dss.signature.SignOneDocumentDTO;
import eu.europa.esig.dss.signature.SoapDocumentSignatureService;
import eu.europa.esig.dss.signature.SoapMultipleDocumentsSignatureService;
import eu.europa.esig.dss.test.TestUtils;
import eu.europa.esig.dss.test.gen.CertificateService;
import eu.europa.esig.dss.test.mock.MockPrivateKeyEntry;
import eu.europa.esig.dss.utils.Utils;
import eu.europa.esig.dss.web.config.CXFConfig;

public class SignatureSoapServiceIT extends AbstractIT {

	private SoapDocumentSignatureService soapClient;
	private SoapMultipleDocumentsSignatureService soapMultiDocsClient;

	@Before
	public void init() {

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

		soapClient = factory.create(SoapDocumentSignatureService.class);

		dataBinding = new JAXBDataBinding();
		dataBinding.getConfiguredXmlAdapters().add(new DateAdapter());

		factory = new JaxWsProxyFactoryBean();
		factory.setServiceClass(SoapMultipleDocumentsSignatureService.class);
		factory.setProperties(props);
		factory.setDataBinding(dataBinding);
		factory.setAddress(getBaseCxf() + CXFConfig.SOAP_SIGNATURE_MULTIPLE_DOCUMENTS);

		loggingInInterceptor = new LoggingInInterceptor();
		factory.getInInterceptors().add(loggingInInterceptor);
		factory.getInFaultInterceptors().add(loggingInInterceptor);

		loggingOutInterceptor = new LoggingOutInterceptor();
		factory.getOutInterceptors().add(loggingOutInterceptor);
		factory.getOutFaultInterceptors().add(loggingOutInterceptor);

		soapMultiDocsClient = factory.create(SoapMultipleDocumentsSignatureService.class);
	}

	@Test
	public void testSigningAndExtension() throws Exception {
		CertificateService certificateService = new CertificateService();

		MockPrivateKeyEntry entry = certificateService.generateCertificateChain(SignatureAlgorithm.RSA_SHA256);

		RemoteSignatureParameters parameters = new RemoteSignatureParameters();
		parameters.setSignatureLevel(SignatureLevel.XAdES_BASELINE_B);
		parameters.setSigningCertificate(new RemoteCertificate(entry.getCertificate().getCertificate().getEncoded()));
		parameters.setSignaturePackaging(SignaturePackaging.ENVELOPING);
		parameters.setDigestAlgorithm(DigestAlgorithm.SHA256);

		FileDocument fileToSign = new FileDocument(new File("src/test/resources/sample.xml"));
		RemoteDocument toSignDocument = new RemoteDocument(Utils.toByteArray(fileToSign.openStream()), fileToSign.getMimeType(), fileToSign.getName());
		ToBeSigned dataToSign = soapClient.getDataToSign(new DataToSignOneDocumentDTO(toSignDocument, parameters));
		assertNotNull(dataToSign);

		SignatureValue signatureValue = TestUtils.sign(SignatureAlgorithm.RSA_SHA256, entry, dataToSign);
		SignOneDocumentDTO signDocument = new SignOneDocumentDTO(toSignDocument, parameters, signatureValue);
		RemoteDocument signedDocument = soapClient.signDocument(signDocument);

		assertNotNull(signedDocument);

		parameters = new RemoteSignatureParameters();
		parameters.setSignatureLevel(SignatureLevel.XAdES_BASELINE_T);

		RemoteDocument extendedDocument = soapClient.extendDocument(new ExtendDocumentDTO(signedDocument, parameters));

		assertNotNull(extendedDocument);

		InMemoryDocument iMD = new InMemoryDocument(extendedDocument.getBytes());
		iMD.save("target/test.xml");
	}

	@Test
	public void testSigningAndExtensionDigestDocument() throws Exception {
		CertificateService certificateService = new CertificateService();

		MockPrivateKeyEntry entry = certificateService.generateCertificateChain(SignatureAlgorithm.RSA_SHA256);

		RemoteSignatureParameters parameters = new RemoteSignatureParameters();
		parameters.setSignatureLevel(SignatureLevel.CAdES_BASELINE_B);
		parameters.setSigningCertificate(new RemoteCertificate(entry.getCertificate().getCertificate().getEncoded()));
		parameters.setSignaturePackaging(SignaturePackaging.DETACHED);
		parameters.setDigestAlgorithm(DigestAlgorithm.SHA256);

		FileDocument fileToSign = new FileDocument(new File("src/test/resources/dss-test.properties"));
		RemoteDocument toSignDocument = new RemoteDocument(DSSUtils.digest(DigestAlgorithm.SHA256, fileToSign), DigestAlgorithm.SHA256,
				fileToSign.getMimeType(), fileToSign.getName());

		ToBeSigned dataToSign = soapClient.getDataToSign(new DataToSignOneDocumentDTO(toSignDocument, parameters));
		assertNotNull(dataToSign);

		SignatureValue signatureValue = TestUtils.sign(SignatureAlgorithm.RSA_SHA256, entry, dataToSign);
		SignOneDocumentDTO signDocument = new SignOneDocumentDTO(toSignDocument, parameters, signatureValue);
		RemoteDocument signedDocument = soapClient.signDocument(signDocument);

		assertNotNull(signedDocument);

		parameters = new RemoteSignatureParameters();
		parameters.setSignatureLevel(SignatureLevel.CAdES_BASELINE_T);
		parameters.setDetachedContents(Arrays.asList(toSignDocument));

		RemoteDocument extendedDocument = soapClient.extendDocument(new ExtendDocumentDTO(signedDocument, parameters));

		assertNotNull(extendedDocument);

		InMemoryDocument iMD = new InMemoryDocument(extendedDocument.getBytes());
		iMD.save("target/test-digest.xml");
	}

	@Test
	public void testSigningAndExtensionMultiDocuments() throws Exception {
		CertificateService certificateService = new CertificateService();

		MockPrivateKeyEntry entry = certificateService.generateCertificateChain(SignatureAlgorithm.RSA_SHA256);

		RemoteSignatureParameters parameters = new RemoteSignatureParameters();
		parameters.setAsicContainerType(ASiCContainerType.ASiC_E);
		parameters.setSignatureLevel(SignatureLevel.XAdES_BASELINE_B);
		parameters.setSigningCertificate(new RemoteCertificate(entry.getCertificate().getCertificate().getEncoded()));
		parameters.setDigestAlgorithm(DigestAlgorithm.SHA256);

		FileDocument fileToSign = new FileDocument(new File("src/test/resources/sample.xml"));
		RemoteDocument toSignDocument = new RemoteDocument(Utils.toByteArray(fileToSign.openStream()), fileToSign.getMimeType(), fileToSign.getName());
		RemoteDocument toSignDoc2 = new RemoteDocument("Hello world!".getBytes("UTF-8"), MimeType.BINARY, "test.bin");
		List<RemoteDocument> toSignDocuments = new ArrayList<RemoteDocument>();
		toSignDocuments.add(toSignDocument);
		toSignDocuments.add(toSignDoc2);
		ToBeSigned dataToSign = soapMultiDocsClient.getDataToSign(new DataToSignMultipleDocumentsDTO(toSignDocuments, parameters));
		assertNotNull(dataToSign);

		SignatureValue signatureValue = TestUtils.sign(SignatureAlgorithm.RSA_SHA256, entry, dataToSign);
		SignMultipleDocumentDTO signDocument = new SignMultipleDocumentDTO(toSignDocuments, parameters, signatureValue);
		RemoteDocument signedDocument = soapMultiDocsClient.signDocument(signDocument);

		assertNotNull(signedDocument);

		parameters = new RemoteSignatureParameters();
		parameters.setAsicContainerType(ASiCContainerType.ASiC_E);
		parameters.setSignatureLevel(SignatureLevel.XAdES_BASELINE_T);

		RemoteDocument extendedDocument = soapMultiDocsClient.extendDocument(new ExtendDocumentDTO(signedDocument, parameters));

		assertNotNull(extendedDocument);

		InMemoryDocument iMD = new InMemoryDocument(extendedDocument.getBytes());
		iMD.save("target/test.asice");
	}

}
