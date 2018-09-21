package eu.europa.esig.dss.web.ws;

import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.cxf.ext.logging.LoggingInInterceptor;
import org.apache.cxf.ext.logging.LoggingOutInterceptor;
import org.apache.cxf.jaxrs.client.JAXRSClientFactoryBean;
import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;

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
import eu.europa.esig.dss.signature.ExtendDocumentDTO;
import eu.europa.esig.dss.signature.RestDocumentSignatureService;
import eu.europa.esig.dss.signature.RestMultipleDocumentSignatureService;
import eu.europa.esig.dss.signature.SignMultipleDocumentDTO;
import eu.europa.esig.dss.signature.SignOneDocumentDTO;
import eu.europa.esig.dss.test.TestUtils;
import eu.europa.esig.dss.test.gen.CertificateService;
import eu.europa.esig.dss.test.mock.MockPrivateKeyEntry;
import eu.europa.esig.dss.utils.Utils;
import eu.europa.esig.dss.web.config.CXFConfig;

public class SignatureRestServiceIT extends AbstractIT {

	private RestDocumentSignatureService restClient;
	private RestMultipleDocumentSignatureService restMultiDocsClient;

	@Before
	public void init() {
		JAXRSClientFactoryBean factory = new JAXRSClientFactoryBean();

		factory.setAddress(getBaseCxf() + CXFConfig.REST_SIGNATURE_ONE_DOCUMENT);
		factory.setServiceClass(RestDocumentSignatureService.class);
		factory.setProviders(Arrays.asList(new JacksonJsonProvider()));

		LoggingInInterceptor loggingInInterceptor = new LoggingInInterceptor();
		factory.getInInterceptors().add(loggingInInterceptor);
		factory.getInFaultInterceptors().add(loggingInInterceptor);

		LoggingOutInterceptor loggingOutInterceptor = new LoggingOutInterceptor();
		factory.getOutInterceptors().add(loggingOutInterceptor);
		factory.getOutFaultInterceptors().add(loggingOutInterceptor);

		restClient = factory.create(RestDocumentSignatureService.class);

		factory = new JAXRSClientFactoryBean();

		factory.setAddress(getBaseCxf() + CXFConfig.REST_SIGNATURE_MULTIPLE_DOCUMENTS);
		factory.setServiceClass(RestMultipleDocumentSignatureService.class);
		factory.setProviders(Arrays.asList(new JacksonJsonProvider()));

		factory.getInInterceptors().add(loggingInInterceptor);
		factory.getInFaultInterceptors().add(loggingInInterceptor);

		factory.getOutInterceptors().add(loggingOutInterceptor);
		factory.getOutFaultInterceptors().add(loggingOutInterceptor);

		restMultiDocsClient = factory.create(RestMultipleDocumentSignatureService.class);
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
		ToBeSigned dataToSign = restClient.getDataToSign(new DataToSignOneDocumentDTO(toSignDocument, parameters));
		assertNotNull(dataToSign);

		SignatureValue signatureValue = TestUtils.sign(SignatureAlgorithm.RSA_SHA256, entry, dataToSign);
		SignOneDocumentDTO signDocument = new SignOneDocumentDTO(toSignDocument, parameters, signatureValue);
		RemoteDocument signedDocument = restClient.signDocument(signDocument);

		assertNotNull(signedDocument);

		parameters = new RemoteSignatureParameters();
		parameters.setSignatureLevel(SignatureLevel.XAdES_BASELINE_T);

		RemoteDocument extendedDocument = restClient.extendDocument(new ExtendDocumentDTO(signedDocument, parameters));

		assertNotNull(extendedDocument);

		InMemoryDocument iMD = new InMemoryDocument(extendedDocument.getBytes());
		iMD.save("target/test.xml");
	}

	@Test
	public void testSigningAndExtensionDigestDocument() throws Exception {
		CertificateService certificateService = new CertificateService();

		MockPrivateKeyEntry entry = certificateService.generateCertificateChain(SignatureAlgorithm.RSA_SHA256);

		RemoteSignatureParameters parameters = new RemoteSignatureParameters();
		parameters.setSignatureLevel(SignatureLevel.XAdES_BASELINE_B);
		parameters.setSigningCertificate(new RemoteCertificate(entry.getCertificate().getCertificate().getEncoded()));
		parameters.setSignaturePackaging(SignaturePackaging.DETACHED);
		parameters.setDigestAlgorithm(DigestAlgorithm.SHA256);

		FileDocument fileToSign = new FileDocument(new File("src/test/resources/sample.xml"));
		RemoteDocument toSignDocument = new RemoteDocument(DSSUtils.digest(DigestAlgorithm.SHA256, fileToSign), DigestAlgorithm.SHA256,
				fileToSign.getMimeType(), fileToSign.getName());

		ToBeSigned dataToSign = restClient.getDataToSign(new DataToSignOneDocumentDTO(toSignDocument, parameters));
		assertNotNull(dataToSign);

		SignatureValue signatureValue = TestUtils.sign(SignatureAlgorithm.RSA_SHA256, entry, dataToSign);
		SignOneDocumentDTO signDocument = new SignOneDocumentDTO(toSignDocument, parameters, signatureValue);
		RemoteDocument signedDocument = restClient.signDocument(signDocument);

		assertNotNull(signedDocument);

		parameters = new RemoteSignatureParameters();
		parameters.setSignatureLevel(SignatureLevel.XAdES_BASELINE_T);
		parameters.setDetachedContents(Arrays.asList(toSignDocument));

		RemoteDocument extendedDocument = restClient.extendDocument(new ExtendDocumentDTO(signedDocument, parameters));

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
		ToBeSigned dataToSign = restMultiDocsClient.getDataToSign(new DataToSignMultipleDocumentsDTO(toSignDocuments, parameters));
		assertNotNull(dataToSign);

		SignatureValue signatureValue = TestUtils.sign(SignatureAlgorithm.RSA_SHA256, entry, dataToSign);
		SignMultipleDocumentDTO signDocument = new SignMultipleDocumentDTO(toSignDocuments, parameters, signatureValue);
		RemoteDocument signedDocument = restMultiDocsClient.signDocument(signDocument);

		assertNotNull(signedDocument);

		parameters = new RemoteSignatureParameters();
		parameters.setAsicContainerType(ASiCContainerType.ASiC_E);
		parameters.setSignatureLevel(SignatureLevel.XAdES_BASELINE_T);

		RemoteDocument extendedDocument = restMultiDocsClient.extendDocument(new ExtendDocumentDTO(signedDocument, parameters));

		assertNotNull(extendedDocument);

		InMemoryDocument iMD = new InMemoryDocument(extendedDocument.getBytes());
		iMD.save("target/test.asice");
	}

}
