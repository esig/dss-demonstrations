package eu.europa.esig.dss.web.ws;

import eu.europa.esig.dss.asic.cades.extract.ASiCWithCAdESContainerExtractor;
import eu.europa.esig.dss.asic.common.ASiCContent;
import eu.europa.esig.dss.asic.common.validation.ASiCManifestParser;
import eu.europa.esig.dss.asic.common.validation.ASiCManifestValidator;
import eu.europa.esig.dss.enumerations.ASiCContainerType;
import eu.europa.esig.dss.enumerations.DigestAlgorithm;
import eu.europa.esig.dss.enumerations.JWSSerializationType;
import eu.europa.esig.dss.enumerations.SigDMechanism;
import eu.europa.esig.dss.enumerations.SignatureLevel;
import eu.europa.esig.dss.enumerations.SignaturePackaging;
import eu.europa.esig.dss.enumerations.SignatureProfile;
import eu.europa.esig.dss.enumerations.SignerTextHorizontalAlignment;
import eu.europa.esig.dss.enumerations.SignerTextPosition;
import eu.europa.esig.dss.enumerations.TextWrapping;
import eu.europa.esig.dss.enumerations.TimestampContainerForm;
import eu.europa.esig.dss.jades.DSSJsonUtils;
import eu.europa.esig.dss.jades.JWSConverter;
import eu.europa.esig.dss.model.DSSDocument;
import eu.europa.esig.dss.model.FileDocument;
import eu.europa.esig.dss.model.InMemoryDocument;
import eu.europa.esig.dss.model.ManifestEntry;
import eu.europa.esig.dss.model.ManifestFile;
import eu.europa.esig.dss.model.SignatureValue;
import eu.europa.esig.dss.spi.DSSUtils;
import eu.europa.esig.dss.token.DSSPrivateKeyEntry;
import eu.europa.esig.dss.token.Pkcs12SignatureToken;
import eu.europa.esig.dss.utils.Utils;
import eu.europa.esig.dss.web.config.CXFConfig;
import eu.europa.esig.dss.ws.converter.ColorConverter;
import eu.europa.esig.dss.ws.converter.DTOConverter;
import eu.europa.esig.dss.ws.converter.RemoteCertificateConverter;
import eu.europa.esig.dss.ws.converter.RemoteDocumentConverter;
import eu.europa.esig.dss.ws.dto.RemoteCertificate;
import eu.europa.esig.dss.ws.dto.RemoteDocument;
import eu.europa.esig.dss.ws.dto.SignatureValueDTO;
import eu.europa.esig.dss.ws.dto.ToBeSignedDTO;
import eu.europa.esig.dss.ws.signature.dto.CounterSignSignatureDTO;
import eu.europa.esig.dss.ws.signature.dto.DataToBeCounterSignedDTO;
import eu.europa.esig.dss.ws.signature.dto.DataToSignMultipleDocumentsDTO;
import eu.europa.esig.dss.ws.signature.dto.DataToSignOneDocumentDTO;
import eu.europa.esig.dss.ws.signature.dto.DataToSignTrustedListDTO;
import eu.europa.esig.dss.ws.signature.dto.ExtendDocumentDTO;
import eu.europa.esig.dss.ws.signature.dto.SignMultipleDocumentDTO;
import eu.europa.esig.dss.ws.signature.dto.SignOneDocumentDTO;
import eu.europa.esig.dss.ws.signature.dto.SignTrustedListDTO;
import eu.europa.esig.dss.ws.signature.dto.TimestampMultipleDocumentDTO;
import eu.europa.esig.dss.ws.signature.dto.TimestampOneDocumentDTO;
import eu.europa.esig.dss.ws.signature.dto.parameters.RemoteBLevelParameters;
import eu.europa.esig.dss.ws.signature.dto.parameters.RemoteSignatureFieldParameters;
import eu.europa.esig.dss.ws.signature.dto.parameters.RemoteSignatureImageParameters;
import eu.europa.esig.dss.ws.signature.dto.parameters.RemoteSignatureImageTextParameters;
import eu.europa.esig.dss.ws.signature.dto.parameters.RemoteSignatureParameters;
import eu.europa.esig.dss.ws.signature.dto.parameters.RemoteTimestampParameters;
import eu.europa.esig.dss.ws.signature.dto.parameters.RemoteTrustedListSignatureParameters;
import eu.europa.esig.dss.ws.signature.rest.client.RestDocumentSignatureService;
import eu.europa.esig.dss.ws.signature.rest.client.RestMultipleDocumentSignatureService;
import eu.europa.esig.dss.ws.signature.rest.client.RestTrustedListSignatureService;
import jakarta.ws.rs.InternalServerErrorException;
import org.apache.cxf.ext.logging.LoggingInInterceptor;
import org.apache.cxf.ext.logging.LoggingOutInterceptor;
import org.apache.cxf.jaxrs.client.JAXRSClientFactoryBean;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.awt.Color;
import java.io.File;
import java.io.FileInputStream;
import java.security.KeyStore.PasswordProtection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class RestSignatureServiceIT extends AbstractRestIT {

	private RestDocumentSignatureService restClient;
	private RestMultipleDocumentSignatureService restMultiDocsClient;
	private RestTrustedListSignatureService restTLSigningClient;

	@BeforeEach
	public void init() {
		JAXRSClientFactoryBean factory = new JAXRSClientFactoryBean();

		factory.setAddress(getBaseCxf() + CXFConfig.REST_SIGNATURE_ONE_DOCUMENT);
		factory.setServiceClass(RestDocumentSignatureService.class);
		factory.setProviders(Arrays.asList(jacksonJsonProvider()));

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
		factory.setProviders(Arrays.asList(jacksonJsonProvider()));

		factory.getInInterceptors().add(loggingInInterceptor);
		factory.getInFaultInterceptors().add(loggingInInterceptor);

		factory.getOutInterceptors().add(loggingOutInterceptor);
		factory.getOutFaultInterceptors().add(loggingOutInterceptor);

		restMultiDocsClient = factory.create(RestMultipleDocumentSignatureService.class);

		factory = new JAXRSClientFactoryBean();

		factory.setAddress(getBaseCxf() + CXFConfig.REST_SIGNATURE_TRUSTED_LIST);
		factory.setServiceClass(RestTrustedListSignatureService.class);
		factory.setProviders(Arrays.asList(jacksonJsonProvider()));

		factory.getInInterceptors().add(loggingInInterceptor);
		factory.getInFaultInterceptors().add(loggingInInterceptor);

		factory.getOutInterceptors().add(loggingOutInterceptor);
		factory.getOutFaultInterceptors().add(loggingOutInterceptor);

		restTLSigningClient = factory.create(RestTrustedListSignatureService.class);
	}

	@Test
	public void testSigningAndExtension() throws Exception {
		try (Pkcs12SignatureToken token = new Pkcs12SignatureToken(new FileInputStream("src/test/resources/user_a_rsa.p12"),
				new PasswordProtection("password".toCharArray()))) {

			List<DSSPrivateKeyEntry> keys = token.getKeys();
			DSSPrivateKeyEntry dssPrivateKeyEntry = keys.get(0);

			RemoteSignatureParameters parameters = new RemoteSignatureParameters();
			parameters.setSignatureLevel(SignatureLevel.XAdES_BASELINE_B);
			parameters.setSigningCertificate(new RemoteCertificate(dssPrivateKeyEntry.getCertificate().getCertificate().getEncoded()));
			parameters.setSignaturePackaging(SignaturePackaging.ENVELOPING);
			parameters.setDigestAlgorithm(DigestAlgorithm.SHA256);

			FileDocument fileToSign = new FileDocument(new File("src/test/resources/sample.xml"));
			RemoteDocument toSignDocument = new RemoteDocument(Utils.toByteArray(fileToSign.openStream()), fileToSign.getName());
			ToBeSignedDTO dataToSign = restClient.getDataToSign(new DataToSignOneDocumentDTO(toSignDocument, parameters));
			assertNotNull(dataToSign);

			SignatureValue signatureValue = token.sign(DTOConverter.toToBeSigned(dataToSign), DigestAlgorithm.SHA256, dssPrivateKeyEntry);
			SignOneDocumentDTO signDocument = new SignOneDocumentDTO(toSignDocument, parameters,
					new SignatureValueDTO(signatureValue.getAlgorithm(), signatureValue.getValue()));
			RemoteDocument signedDocument = restClient.signDocument(signDocument);

			assertNotNull(signedDocument);

			parameters = new RemoteSignatureParameters();
			parameters.setSignatureLevel(SignatureLevel.XAdES_BASELINE_T);

			RemoteDocument extendedDocument = restClient.extendDocument(new ExtendDocumentDTO(signedDocument, parameters));

			assertNotNull(extendedDocument);

			InMemoryDocument iMD = new InMemoryDocument(extendedDocument.getBytes());
			// iMD.save("target/test.xml");
			assertNotNull(iMD);
		}
	}

	@Test
	public void testSigningAndExtensionWithProfile() throws Exception {
		try (Pkcs12SignatureToken token = new Pkcs12SignatureToken(new FileInputStream("src/test/resources/user_a_rsa.p12"),
				new PasswordProtection("password".toCharArray()))) {

			List<DSSPrivateKeyEntry> keys = token.getKeys();
			DSSPrivateKeyEntry dssPrivateKeyEntry = keys.get(0);

			RemoteSignatureParameters parameters = new RemoteSignatureParameters();
			parameters.setSignatureLevel(SignatureLevel.XAdES_BASELINE_B);
			parameters.setSigningCertificate(new RemoteCertificate(dssPrivateKeyEntry.getCertificate().getCertificate().getEncoded()));
			parameters.setSignaturePackaging(SignaturePackaging.ENVELOPING);
			parameters.setDigestAlgorithm(DigestAlgorithm.SHA256);

			FileDocument fileToSign = new FileDocument(new File("src/test/resources/sample.xml"));
			RemoteDocument toSignDocument = new RemoteDocument(Utils.toByteArray(fileToSign.openStream()), fileToSign.getName());
			ToBeSignedDTO dataToSign = restClient.getDataToSign(new DataToSignOneDocumentDTO(toSignDocument, parameters));
			assertNotNull(dataToSign);

			SignatureValue signatureValue = token.sign(DTOConverter.toToBeSigned(dataToSign), DigestAlgorithm.SHA256, dssPrivateKeyEntry);
			SignOneDocumentDTO signDocument = new SignOneDocumentDTO(toSignDocument, parameters,
					new SignatureValueDTO(signatureValue.getAlgorithm(), signatureValue.getValue()));
			RemoteDocument signedDocument = restClient.signDocument(signDocument);

			assertNotNull(signedDocument);

			RemoteDocument extendedDocument = restClient.extendDocument(new ExtendDocumentDTO(signedDocument, SignatureProfile.BASELINE_T));

			assertNotNull(extendedDocument);

			InMemoryDocument iMD = new InMemoryDocument(extendedDocument.getBytes());
			// iMD.save("target/test.xml");
			assertNotNull(iMD);
		}
	}

	@Test
	public void testSigningAndExtensionDigestDocument() throws Exception {
		try (Pkcs12SignatureToken token = new Pkcs12SignatureToken(new FileInputStream("src/test/resources/user_a_rsa.p12"),
				new PasswordProtection("password".toCharArray()))) {

			List<DSSPrivateKeyEntry> keys = token.getKeys();
			DSSPrivateKeyEntry dssPrivateKeyEntry = keys.get(0);

			RemoteSignatureParameters parameters = new RemoteSignatureParameters();
			parameters.setSignatureLevel(SignatureLevel.XAdES_BASELINE_B);
			parameters.setSigningCertificate(new RemoteCertificate(dssPrivateKeyEntry.getCertificate().getCertificate().getEncoded()));
			parameters.setSignaturePackaging(SignaturePackaging.DETACHED);
			parameters.setDigestAlgorithm(DigestAlgorithm.SHA256);

			FileDocument fileToSign = new FileDocument(new File("src/test/resources/sample.xml"));
			RemoteDocument toSignDocument = new RemoteDocument(DSSUtils.digest(DigestAlgorithm.SHA256, fileToSign), DigestAlgorithm.SHA256,
					fileToSign.getName());

			ToBeSignedDTO dataToSign = restClient.getDataToSign(new DataToSignOneDocumentDTO(toSignDocument, parameters));
			assertNotNull(dataToSign);

			SignatureValue signatureValue = token.sign(DTOConverter.toToBeSigned(dataToSign), DigestAlgorithm.SHA256, dssPrivateKeyEntry);
			SignOneDocumentDTO signDocument = new SignOneDocumentDTO(toSignDocument, parameters,
					new SignatureValueDTO(signatureValue.getAlgorithm(), signatureValue.getValue()));
			RemoteDocument signedDocument = restClient.signDocument(signDocument);

			assertNotNull(signedDocument);

			parameters = new RemoteSignatureParameters();
			parameters.setSignatureLevel(SignatureLevel.XAdES_BASELINE_T);
			parameters.setDetachedContents(Arrays.asList(toSignDocument));

			RemoteDocument extendedDocument = restClient.extendDocument(new ExtendDocumentDTO(signedDocument, parameters));

			assertNotNull(extendedDocument);

			InMemoryDocument iMD = new InMemoryDocument(extendedDocument.getBytes());
			// iMD.save("target/test-digest.xml");
			assertNotNull(iMD);
		}
	}

	@Test
	public void testSigningAndExtensionDigestDocumentWithProfile() throws Exception {
		try (Pkcs12SignatureToken token = new Pkcs12SignatureToken(new FileInputStream("src/test/resources/user_a_rsa.p12"),
				new PasswordProtection("password".toCharArray()))) {

			List<DSSPrivateKeyEntry> keys = token.getKeys();
			DSSPrivateKeyEntry dssPrivateKeyEntry = keys.get(0);

			RemoteSignatureParameters parameters = new RemoteSignatureParameters();
			parameters.setSignatureLevel(SignatureLevel.XAdES_BASELINE_B);
			parameters.setSigningCertificate(new RemoteCertificate(dssPrivateKeyEntry.getCertificate().getCertificate().getEncoded()));
			parameters.setSignaturePackaging(SignaturePackaging.DETACHED);
			parameters.setDigestAlgorithm(DigestAlgorithm.SHA256);

			FileDocument fileToSign = new FileDocument(new File("src/test/resources/sample.xml"));
			RemoteDocument toSignDocument = new RemoteDocument(DSSUtils.digest(DigestAlgorithm.SHA256, fileToSign), DigestAlgorithm.SHA256,
					fileToSign.getName());

			ToBeSignedDTO dataToSign = restClient.getDataToSign(new DataToSignOneDocumentDTO(toSignDocument, parameters));
			assertNotNull(dataToSign);

			SignatureValue signatureValue = token.sign(DTOConverter.toToBeSigned(dataToSign), DigestAlgorithm.SHA256, dssPrivateKeyEntry);
			SignOneDocumentDTO signDocument = new SignOneDocumentDTO(toSignDocument, parameters,
					new SignatureValueDTO(signatureValue.getAlgorithm(), signatureValue.getValue()));
			RemoteDocument signedDocument = restClient.signDocument(signDocument);

			assertNotNull(signedDocument);

			parameters = new RemoteSignatureParameters();
			parameters.setDetachedContents(Arrays.asList(toSignDocument));

			RemoteDocument extendedDocument = restClient.extendDocument(new ExtendDocumentDTO(signedDocument, SignatureProfile.BASELINE_T, parameters));

			assertNotNull(extendedDocument);

			InMemoryDocument iMD = new InMemoryDocument(extendedDocument.getBytes());
			// iMD.save("target/test-digest.xml");
			assertNotNull(iMD);
		}
	}

	@Test
	public void testSigningAndExtensionMultiDocuments() throws Exception {
		try (Pkcs12SignatureToken token = new Pkcs12SignatureToken(new FileInputStream("src/test/resources/user_a_rsa.p12"),
				new PasswordProtection("password".toCharArray()))) {

			List<DSSPrivateKeyEntry> keys = token.getKeys();
			DSSPrivateKeyEntry dssPrivateKeyEntry = keys.get(0);

			RemoteSignatureParameters parameters = new RemoteSignatureParameters();
			parameters.setAsicContainerType(ASiCContainerType.ASiC_E);
			parameters.setSignatureLevel(SignatureLevel.XAdES_BASELINE_B);
			parameters.setSigningCertificate(new RemoteCertificate(dssPrivateKeyEntry.getCertificate().getCertificate().getEncoded()));
			parameters.setDigestAlgorithm(DigestAlgorithm.SHA256);

			FileDocument fileToSign = new FileDocument(new File("src/test/resources/sample.xml"));
			RemoteDocument toSignDocument = new RemoteDocument(DSSUtils.toByteArray(fileToSign), fileToSign.getName());
			RemoteDocument toSignDoc2 = new RemoteDocument("Hello world!".getBytes("UTF-8"), "test.bin");
			List<RemoteDocument> toSignDocuments = new ArrayList<>();
			toSignDocuments.add(toSignDocument);
			toSignDocuments.add(toSignDoc2);
			ToBeSignedDTO dataToSign = restMultiDocsClient.getDataToSign(new DataToSignMultipleDocumentsDTO(toSignDocuments, parameters));
			assertNotNull(dataToSign);

			SignatureValue signatureValue = token.sign(DTOConverter.toToBeSigned(dataToSign), DigestAlgorithm.SHA256, dssPrivateKeyEntry);
			SignMultipleDocumentDTO signDocument = new SignMultipleDocumentDTO(toSignDocuments, parameters,
					new SignatureValueDTO(signatureValue.getAlgorithm(), signatureValue.getValue()));
			RemoteDocument signedDocument = restMultiDocsClient.signDocument(signDocument);

			assertNotNull(signedDocument);

			parameters = new RemoteSignatureParameters();
			parameters.setAsicContainerType(ASiCContainerType.ASiC_E);
			parameters.setSignatureLevel(SignatureLevel.XAdES_BASELINE_T);

			RemoteDocument extendedDocument = restMultiDocsClient.extendDocument(new ExtendDocumentDTO(signedDocument, parameters));

			assertNotNull(extendedDocument);

			InMemoryDocument iMD = new InMemoryDocument(extendedDocument.getBytes());
			// iMD.save("target/test.asice");
			assertNotNull(iMD);
		}
	}

	@Test
	public void testSigningAndExtensionMultiDocumentsWithProfile() throws Exception {
		try (Pkcs12SignatureToken token = new Pkcs12SignatureToken(new FileInputStream("src/test/resources/user_a_rsa.p12"),
				new PasswordProtection("password".toCharArray()))) {

			List<DSSPrivateKeyEntry> keys = token.getKeys();
			DSSPrivateKeyEntry dssPrivateKeyEntry = keys.get(0);

			RemoteSignatureParameters parameters = new RemoteSignatureParameters();
			parameters.setAsicContainerType(ASiCContainerType.ASiC_E);
			parameters.setSignatureLevel(SignatureLevel.XAdES_BASELINE_B);
			parameters.setSigningCertificate(new RemoteCertificate(dssPrivateKeyEntry.getCertificate().getCertificate().getEncoded()));
			parameters.setDigestAlgorithm(DigestAlgorithm.SHA256);

			FileDocument fileToSign = new FileDocument(new File("src/test/resources/sample.xml"));
			RemoteDocument toSignDocument = new RemoteDocument(DSSUtils.toByteArray(fileToSign), fileToSign.getName());
			RemoteDocument toSignDoc2 = new RemoteDocument("Hello world!".getBytes("UTF-8"), "test.bin");
			List<RemoteDocument> toSignDocuments = new ArrayList<>();
			toSignDocuments.add(toSignDocument);
			toSignDocuments.add(toSignDoc2);
			ToBeSignedDTO dataToSign = restMultiDocsClient.getDataToSign(new DataToSignMultipleDocumentsDTO(toSignDocuments, parameters));
			assertNotNull(dataToSign);

			SignatureValue signatureValue = token.sign(DTOConverter.toToBeSigned(dataToSign), DigestAlgorithm.SHA256, dssPrivateKeyEntry);
			SignMultipleDocumentDTO signDocument = new SignMultipleDocumentDTO(toSignDocuments, parameters,
					new SignatureValueDTO(signatureValue.getAlgorithm(), signatureValue.getValue()));
			RemoteDocument signedDocument = restMultiDocsClient.signDocument(signDocument);

			assertNotNull(signedDocument);

			RemoteDocument extendedDocument = restMultiDocsClient.extendDocument(new ExtendDocumentDTO(signedDocument, SignatureProfile.BASELINE_T));

			assertNotNull(extendedDocument);

			InMemoryDocument iMD = new InMemoryDocument(extendedDocument.getBytes());
			// iMD.save("target/test.asice");
			assertNotNull(iMD);
		}
	}
	
	@Test
	public void testVisibleSignature() throws Exception {
		try (Pkcs12SignatureToken token = new Pkcs12SignatureToken(new FileInputStream("src/test/resources/user_a_rsa.p12"),
				new PasswordProtection("password".toCharArray()))) {

			List<DSSPrivateKeyEntry> keys = token.getKeys();
			DSSPrivateKeyEntry dssPrivateKeyEntry = keys.get(0);

			RemoteSignatureParameters parameters = new RemoteSignatureParameters();
			parameters.setSignatureLevel(SignatureLevel.PAdES_BASELINE_B);
			parameters.setSigningCertificate(RemoteCertificateConverter.toRemoteCertificate(dssPrivateKeyEntry.getCertificate()));
			parameters.setDigestAlgorithm(DigestAlgorithm.SHA256);

			RemoteSignatureImageParameters imageParameters = new RemoteSignatureImageParameters();
			
			RemoteSignatureFieldParameters fieldParameters = new RemoteSignatureFieldParameters();
			fieldParameters.setPage(1);
			fieldParameters.setOriginX(200.F);
			fieldParameters.setOriginY(100.F);
			fieldParameters.setWidth(130.F);
			fieldParameters.setHeight(50.F);
			imageParameters.setFieldParameters(fieldParameters);

			RemoteSignatureImageTextParameters textParameters = new RemoteSignatureImageTextParameters();
			textParameters.setText("Signature");
			textParameters.setSize(24);
			textParameters.setSignerTextPosition(SignerTextPosition.TOP);
			textParameters.setSignerTextHorizontalAlignment(SignerTextHorizontalAlignment.CENTER);
			textParameters.setTextColor(ColorConverter.toRemoteColor(Color.BLUE));
			textParameters.setBackgroundColor(ColorConverter.toRemoteColor(Color.WHITE));
			imageParameters.setTextParameters(textParameters);
			
			parameters.setImageParameters(imageParameters);

			FileDocument fileToSign = new FileDocument(new File("src/test/resources/sample.pdf"));
			RemoteDocument toSignDocument = new RemoteDocument(Utils.toByteArray(fileToSign.openStream()), fileToSign.getName());

			DataToSignOneDocumentDTO dataToSignDTO = new DataToSignOneDocumentDTO(toSignDocument, parameters);
			ToBeSignedDTO dataToSign = restClient.getDataToSign(dataToSignDTO);
			assertNotNull(dataToSign);

			SignatureValue signatureValue = token.sign(DTOConverter.toToBeSigned(dataToSign), DigestAlgorithm.SHA256, dssPrivateKeyEntry);
			SignOneDocumentDTO signOneDocumentDTO = new SignOneDocumentDTO(toSignDocument, parameters,
					new SignatureValueDTO(signatureValue.getAlgorithm(), signatureValue.getValue()));
			RemoteDocument signedDocument = restClient.signDocument(signOneDocumentDTO);

			assertNotNull(signedDocument);

			InMemoryDocument iMD = new InMemoryDocument(signedDocument.getBytes());
			// iMD.save("target/pades-rest-visible.pdf");
			assertNotNull(iMD);
		}
	}

	@Test
	public void testVisibleSignatureWithTextLineBreaks() throws Exception {
		try (Pkcs12SignatureToken token = new Pkcs12SignatureToken(new FileInputStream("src/test/resources/user_a_rsa.p12"),
				new PasswordProtection("password".toCharArray()))) {

			List<DSSPrivateKeyEntry> keys = token.getKeys();
			DSSPrivateKeyEntry dssPrivateKeyEntry = keys.get(0);

			RemoteSignatureParameters parameters = new RemoteSignatureParameters();
			parameters.setSignatureLevel(SignatureLevel.PAdES_BASELINE_B);
			parameters.setSigningCertificate(RemoteCertificateConverter.toRemoteCertificate(dssPrivateKeyEntry.getCertificate()));
			parameters.setDigestAlgorithm(DigestAlgorithm.SHA256);

			RemoteSignatureImageParameters imageParameters = new RemoteSignatureImageParameters();

			RemoteSignatureFieldParameters fieldParameters = new RemoteSignatureFieldParameters();
			fieldParameters.setPage(1);
			fieldParameters.setOriginX(200.F);
			fieldParameters.setOriginY(100.F);
			fieldParameters.setWidth(130.F);
			fieldParameters.setHeight(50.F);
			imageParameters.setFieldParameters(fieldParameters);

			RemoteSignatureImageTextParameters textParameters = new RemoteSignatureImageTextParameters();
			textParameters.setText("Digitally signed by JOHN GEORGE ANTHONY WILLIAMS\n" +
					"Date: 2021.01.01 01:01:01 WET\n" +
					"Reason: my-reason\n" +
					"Location: my-location");
			textParameters.setTextWrapping(TextWrapping.FILL_BOX_AND_LINEBREAK);
			imageParameters.setTextParameters(textParameters);

			parameters.setImageParameters(imageParameters);

			FileDocument fileToSign = new FileDocument(new File("src/test/resources/sample.pdf"));
			RemoteDocument toSignDocument = new RemoteDocument(Utils.toByteArray(fileToSign.openStream()), fileToSign.getName());

			DataToSignOneDocumentDTO dataToSignDTO = new DataToSignOneDocumentDTO(toSignDocument, parameters);
			ToBeSignedDTO dataToSign = restClient.getDataToSign(dataToSignDTO);
			assertNotNull(dataToSign);

			SignatureValue signatureValue = token.sign(DTOConverter.toToBeSigned(dataToSign), DigestAlgorithm.SHA256, dssPrivateKeyEntry);
			SignOneDocumentDTO signOneDocumentDTO = new SignOneDocumentDTO(toSignDocument, parameters,
					new SignatureValueDTO(signatureValue.getAlgorithm(), signatureValue.getValue()));
			RemoteDocument signedDocument = restClient.signDocument(signOneDocumentDTO);

			assertNotNull(signedDocument);

			InMemoryDocument iMD = new InMemoryDocument(signedDocument.getBytes());
			// iMD.save("target/pades-rest-text-with-breaks-visible.pdf");
			assertNotNull(iMD);
		}
	}
	
	@Test
	public void jadesParallelSigningTest() throws Exception {
		try (Pkcs12SignatureToken token = new Pkcs12SignatureToken(new FileInputStream("src/test/resources/user_a_rsa.p12"),
				new PasswordProtection("password".toCharArray()))) {

			List<DSSPrivateKeyEntry> keys = token.getKeys();
			DSSPrivateKeyEntry dssPrivateKeyEntry = keys.get(0);

			RemoteSignatureParameters parameters = new RemoteSignatureParameters();
			parameters.setSignatureLevel(SignatureLevel.JAdES_BASELINE_B);
			parameters.setSigningCertificate(new RemoteCertificate(dssPrivateKeyEntry.getCertificate().getCertificate().getEncoded()));
			parameters.setSignaturePackaging(SignaturePackaging.ENVELOPING);
			parameters.setDigestAlgorithm(DigestAlgorithm.SHA256);
			parameters.setJwsSerializationType(JWSSerializationType.COMPACT_SERIALIZATION);

			RemoteBLevelParameters bLevelParameters = new RemoteBLevelParameters();
			bLevelParameters.setClaimedSignerRoles(Arrays.asList("Manager", "Administrator"));
			bLevelParameters.setSignedAssertions(Collections.singletonList("SignedAssertion"));
			parameters.setBLevelParams(bLevelParameters);

			FileDocument fileToSign = new FileDocument(new File("src/test/resources/sample.xml"));
			RemoteDocument toSignDocument = new RemoteDocument(Utils.toByteArray(fileToSign.openStream()), fileToSign.getName());
			ToBeSignedDTO dataToSign = restClient.getDataToSign(new DataToSignOneDocumentDTO(toSignDocument, parameters));
			assertNotNull(dataToSign);

			SignatureValue signatureValue = token.sign(DTOConverter.toToBeSigned(dataToSign), DigestAlgorithm.SHA256, dssPrivateKeyEntry);
			SignOneDocumentDTO signDocument = new SignOneDocumentDTO(toSignDocument, parameters,
					new SignatureValueDTO(signatureValue.getAlgorithm(), signatureValue.getValue()));
			RemoteDocument signedDocument = restClient.signDocument(signDocument);
			assertNotNull(signedDocument);

			String strSignature = new String(signedDocument.getBytes());
			String protectedHeader = new String(DSSJsonUtils.fromBase64Url(strSignature.split("\\.")[0]));
			assertTrue(bLevelParameters.getClaimedSignerRoles().stream().allMatch(protectedHeader::contains));
			assertTrue(bLevelParameters.getSignedAssertions().stream().allMatch(protectedHeader::contains));
			
			DSSDocument dssSignedDocument = RemoteDocumentConverter.toDSSDocument(signedDocument);
			DSSDocument flattenedJson = JWSConverter.fromJWSCompactToJSONFlattenedSerialization(dssSignedDocument);
			assertNotNull(flattenedJson);
			RemoteDocument flattenedRemoteDocument = RemoteDocumentConverter.toRemoteDocument(flattenedJson);

			parameters = new RemoteSignatureParameters();
			parameters.setSignatureLevel(SignatureLevel.JAdES_BASELINE_B);
			parameters.setSigningCertificate(new RemoteCertificate(dssPrivateKeyEntry.getCertificate().getCertificate().getEncoded()));
			parameters.setSignaturePackaging(SignaturePackaging.ENVELOPING);
			parameters.setDigestAlgorithm(DigestAlgorithm.SHA256);
			parameters.setJwsSerializationType(JWSSerializationType.JSON_SERIALIZATION);
			
			dataToSign = restClient.getDataToSign(new DataToSignOneDocumentDTO(flattenedRemoteDocument, parameters));
			assertNotNull(dataToSign);

			signatureValue = token.sign(DTOConverter.toToBeSigned(dataToSign), DigestAlgorithm.SHA256, dssPrivateKeyEntry);
			signDocument = new SignOneDocumentDTO(flattenedRemoteDocument, parameters,
					new SignatureValueDTO(signatureValue.getAlgorithm(), signatureValue.getValue()));
			RemoteDocument doubleSignedDocument = restClient.signDocument(signDocument);

			assertNotNull(doubleSignedDocument);

			InMemoryDocument iMD = new InMemoryDocument(doubleSignedDocument.getBytes());
			// iMD.save("target/test.json");
			assertNotNull(iMD);
		}
	}

	@Test
	public void jadesMultiDocumentsSignTest() throws Exception {
		try (Pkcs12SignatureToken token = new Pkcs12SignatureToken(new FileInputStream("src/test/resources/user_a_rsa.p12"),
				new PasswordProtection("password".toCharArray()))) {

			List<DSSPrivateKeyEntry> keys = token.getKeys();
			DSSPrivateKeyEntry dssPrivateKeyEntry = keys.get(0);

			RemoteSignatureParameters parameters = new RemoteSignatureParameters();
			parameters.setSignatureLevel(SignatureLevel.JAdES_BASELINE_B);
			parameters.setSigningCertificate(new RemoteCertificate(dssPrivateKeyEntry.getCertificate().getCertificate().getEncoded()));
			parameters.setDigestAlgorithm(DigestAlgorithm.SHA256);
			parameters.setJwsSerializationType(JWSSerializationType.FLATTENED_JSON_SERIALIZATION);
			parameters.setSignaturePackaging(SignaturePackaging.DETACHED);
			parameters.setSigDMechanism(SigDMechanism.OBJECT_ID_BY_URI_HASH);

			FileDocument fileToSign = new FileDocument(new File("src/test/resources/sample.xml"));
			RemoteDocument toSignDocument = new RemoteDocument(DSSUtils.toByteArray(fileToSign), fileToSign.getName());
			RemoteDocument toSignDoc2 = new RemoteDocument("Hello world!".getBytes("UTF-8"), "test.bin");
			List<RemoteDocument> toSignDocuments = new ArrayList<>();
			toSignDocuments.add(toSignDocument);
			toSignDocuments.add(toSignDoc2);
			ToBeSignedDTO dataToSign = restMultiDocsClient.getDataToSign(new DataToSignMultipleDocumentsDTO(toSignDocuments, parameters));
			assertNotNull(dataToSign);

			SignatureValue signatureValue = token.sign(DTOConverter.toToBeSigned(dataToSign), DigestAlgorithm.SHA256, dssPrivateKeyEntry);
			SignMultipleDocumentDTO signDocument = new SignMultipleDocumentDTO(toSignDocuments, parameters,
					new SignatureValueDTO(signatureValue.getAlgorithm(), signatureValue.getValue()));
			RemoteDocument signedDocument = restMultiDocsClient.signDocument(signDocument);

			assertNotNull(signedDocument);

			InMemoryDocument iMD = new InMemoryDocument(signedDocument.getBytes());
			// iMD.save("target/test.json");
			assertNotNull(iMD);
		}
	}
	
	@Test
	public void jadesMultiDocsEnvelopingSignTest() throws Exception {
		try (Pkcs12SignatureToken token = new Pkcs12SignatureToken(new FileInputStream("src/test/resources/user_a_rsa.p12"),
			new PasswordProtection("password".toCharArray()))) {

			List<DSSPrivateKeyEntry> keys = token.getKeys();
			DSSPrivateKeyEntry dssPrivateKeyEntry = keys.get(0);
	
			RemoteSignatureParameters parameters = new RemoteSignatureParameters();
			parameters.setSignatureLevel(SignatureLevel.JAdES_BASELINE_B);
			parameters.setSigningCertificate(new RemoteCertificate(dssPrivateKeyEntry.getCertificate().getCertificate().getEncoded()));
			parameters.setDigestAlgorithm(DigestAlgorithm.SHA256);
			parameters.setJwsSerializationType(JWSSerializationType.FLATTENED_JSON_SERIALIZATION);
			parameters.setSignaturePackaging(SignaturePackaging.ENVELOPING);
	
			FileDocument fileToSign = new FileDocument(new File("src/test/resources/sample.xml"));
			RemoteDocument toSignDocument = new RemoteDocument(DSSUtils.toByteArray(fileToSign), fileToSign.getName());
			RemoteDocument toSignDoc2 = new RemoteDocument("Hello world!".getBytes("UTF-8"), "test.bin");
			List<RemoteDocument> toSignDocuments = new ArrayList<>();
			toSignDocuments.add(toSignDocument);
			toSignDocuments.add(toSignDoc2);
			
			assertThrows(Exception.class, () -> restMultiDocsClient.getDataToSign(new DataToSignMultipleDocumentsDTO(toSignDocuments, parameters)));
		}
	}

	@Test
	void testSignJAdESWithPlainPayload() throws Exception {
		try (Pkcs12SignatureToken token = new Pkcs12SignatureToken(new FileInputStream("src/test/resources/user_a_rsa.p12"),
				new PasswordProtection("password".toCharArray()))) {

			List<DSSPrivateKeyEntry> keys = token.getKeys();
			DSSPrivateKeyEntry dssPrivateKeyEntry = keys.get(0);

			RemoteSignatureParameters parameters = new RemoteSignatureParameters();
			parameters.setSignatureLevel(SignatureLevel.JAdES_BASELINE_T);
			parameters.setSigningCertificate(new RemoteCertificate(dssPrivateKeyEntry.getCertificate().getCertificate().getEncoded()));
			parameters.setSignaturePackaging(SignaturePackaging.ENVELOPING);
			parameters.setDigestAlgorithm(DigestAlgorithm.SHA256);
			parameters.setJwsSerializationType(JWSSerializationType.JSON_SERIALIZATION);
			parameters.setBase64UrlEncodedPayload(false);

			DSSDocument fileToSign = new InMemoryDocument("HelloWorld".getBytes(), "helloWorld");
			RemoteDocument toSignDocument = new RemoteDocument(Utils.toByteArray(fileToSign.openStream()), fileToSign.getName());
			ToBeSignedDTO dataToSign = restClient.getDataToSign(new DataToSignOneDocumentDTO(toSignDocument, parameters));
			assertNotNull(dataToSign);

			SignatureValue signatureValue = token.sign(DTOConverter.toToBeSigned(dataToSign), DigestAlgorithm.SHA256, dssPrivateKeyEntry);
			SignOneDocumentDTO signDocumentDTO = new SignOneDocumentDTO(toSignDocument, parameters,
					new SignatureValueDTO(signatureValue.getAlgorithm(), signatureValue.getValue()));
			RemoteDocument signedDocument = restClient.signDocument(signDocumentDTO);

			assertNotNull(signedDocument);

			InMemoryDocument iMD = new InMemoryDocument(signedDocument.getBytes());
			String strSignature = new String(DSSUtils.toByteArray(iMD));
			assertTrue(strSignature.contains(new String(DSSUtils.toByteArray(fileToSign))));
			assertFalse(strSignature.contains("sigT"));
			assertNotNull(iMD);
		}
	}

	@Test
	void testSignJAdESWithPlainEtsiU() throws Exception {
		try (Pkcs12SignatureToken token = new Pkcs12SignatureToken(new FileInputStream("src/test/resources/user_a_rsa.p12"),
				new PasswordProtection("password".toCharArray()))) {

			List<DSSPrivateKeyEntry> keys = token.getKeys();
			DSSPrivateKeyEntry dssPrivateKeyEntry = keys.get(0);

			RemoteSignatureParameters parameters = new RemoteSignatureParameters();
			parameters.setSignatureLevel(SignatureLevel.JAdES_BASELINE_T);
			parameters.setSigningCertificate(new RemoteCertificate(dssPrivateKeyEntry.getCertificate().getCertificate().getEncoded()));
			parameters.setSignaturePackaging(SignaturePackaging.ENVELOPING);
			parameters.setDigestAlgorithm(DigestAlgorithm.SHA256);
			parameters.setJwsSerializationType(JWSSerializationType.JSON_SERIALIZATION);
			parameters.setBase64UrlEncodedEtsiUComponents(false);

			DSSDocument fileToSign = new InMemoryDocument("HelloWorld".getBytes(), "helloWorld");
			RemoteDocument toSignDocument = new RemoteDocument(Utils.toByteArray(fileToSign.openStream()), fileToSign.getName());
			ToBeSignedDTO dataToSign = restClient.getDataToSign(new DataToSignOneDocumentDTO(toSignDocument, parameters));
			assertNotNull(dataToSign);

			SignatureValue signatureValue = token.sign(DTOConverter.toToBeSigned(dataToSign), DigestAlgorithm.SHA256, dssPrivateKeyEntry);
			SignOneDocumentDTO signDocumentDTO = new SignOneDocumentDTO(toSignDocument, parameters,
					new SignatureValueDTO(signatureValue.getAlgorithm(), signatureValue.getValue()));
			RemoteDocument signedDocument = restClient.signDocument(signDocumentDTO);

			assertNotNull(signedDocument);

			InMemoryDocument iMD = new InMemoryDocument(signedDocument.getBytes());
			String strSignature = new String(DSSUtils.toByteArray(iMD));
			assertFalse(strSignature.contains(new String(DSSUtils.toByteArray(fileToSign))));
			assertTrue(strSignature.contains("sigT"));
			assertNotNull(iMD);
		}
	}

	@Test
	public void testTimestamping() throws Exception {
		RemoteTimestampParameters timestampParameters = new RemoteTimestampParameters(TimestampContainerForm.PDF, DigestAlgorithm.SHA512);
		
		FileDocument fileToTimestamp = new FileDocument(new File("src/test/resources/sample.pdf"));
		RemoteDocument remoteDocument = RemoteDocumentConverter.toRemoteDocument(fileToTimestamp);
		
		TimestampOneDocumentDTO timestampOneDocumentDTO = new TimestampOneDocumentDTO(remoteDocument, timestampParameters);
		RemoteDocument timestampedDocument = restClient.timestampDocument(timestampOneDocumentDTO);

		assertNotNull(timestampedDocument);

		InMemoryDocument iMD = new InMemoryDocument(timestampedDocument.getBytes());
		// iMD.save("target/testSigned.pdf");
		assertNotNull(iMD);
	}
	
	@Test
	public void timestampMultipleDocumentsTest() throws Exception {
		RemoteTimestampParameters timestampParameters = new RemoteTimestampParameters(TimestampContainerForm.ASiC_E, DigestAlgorithm.SHA512);
		
		List<DSSDocument> documentsToSign = new ArrayList<>(
				Arrays.asList(new FileDocument(new File("src/test/resources/sample.xml")),
						new FileDocument(new File("src/test/resources/sample.pdf"))));
		
		List<RemoteDocument> remoteDocuments = RemoteDocumentConverter.toRemoteDocuments(documentsToSign);
		
		TimestampMultipleDocumentDTO timestampMultipleDocumentDTO = new TimestampMultipleDocumentDTO(remoteDocuments, timestampParameters);
		RemoteDocument timestampedDocument = restMultiDocsClient.timestampDocuments(timestampMultipleDocumentDTO);
		
		assertNotNull(timestampedDocument);

		InMemoryDocument iMD = new InMemoryDocument(timestampedDocument.getBytes());
		// iMD.save("target/testSigned.asice");
		assertNotNull(iMD);
		
		ASiCWithCAdESContainerExtractor extractor = new ASiCWithCAdESContainerExtractor(iMD);
		ASiCContent extractedResult = extractor.extract();
		
		assertEquals(1, extractedResult.getTimestampDocuments().size());
		DSSDocument timestamp = extractedResult.getTimestampDocuments().get(0);
		
		DSSDocument timestampManifest = ASiCManifestParser.getLinkedManifest(extractedResult.getManifestDocuments(), timestamp.getName());
		ManifestFile manifestFile = ASiCManifestParser.getManifestFile(timestampManifest);

		ASiCManifestValidator manifestValidator = new ASiCManifestValidator(manifestFile, extractedResult.getSignedDocuments());
		List<ManifestEntry> manifestEntries = manifestValidator.validateEntries();
		
		assertEquals(2, manifestEntries.size());
		
		for (ManifestEntry manifestEntry : manifestEntries) {
			boolean signedDocFound = false;
			for (DSSDocument document : documentsToSign) {
				if (manifestEntry.getUri().equals(document.getName())) {
					signedDocFound = true;
				}
			}
			assertTrue(signedDocFound);
		}
	}
	
	@Test
	public void timestampASiCSTest() throws Exception {
		RemoteTimestampParameters timestampParameters = new RemoteTimestampParameters(TimestampContainerForm.ASiC_S, DigestAlgorithm.SHA512);
		
		List<DSSDocument> documentsToSign = new ArrayList<>(
				Arrays.asList(new FileDocument(new File("src/test/resources/sample.xml")),
						new FileDocument(new File("src/test/resources/sample.pdf"))));
		
		List<RemoteDocument> remoteDocuments = RemoteDocumentConverter.toRemoteDocuments(documentsToSign);
		
		TimestampMultipleDocumentDTO timestampMultipleDocumentDTO = new TimestampMultipleDocumentDTO(remoteDocuments, timestampParameters);
		RemoteDocument timestampedDocument = restMultiDocsClient.timestampDocuments(timestampMultipleDocumentDTO);
		
		assertNotNull(timestampedDocument);

		InMemoryDocument iMD = new InMemoryDocument(timestampedDocument.getBytes());
		// iMD.save("target/testSigned.asics");
		assertNotNull(iMD);
		
		ASiCWithCAdESContainerExtractor extractor = new ASiCWithCAdESContainerExtractor(iMD);
		ASiCContent extractedResult = extractor.extract();
		
		assertEquals(1, extractedResult.getTimestampDocuments().size());
		
		List<DSSDocument> signedDocuments = extractedResult.getSignedDocuments();
		assertEquals(1, signedDocuments.size()); // ZIP container
		
		List<DSSDocument> containerDocuments = extractedResult.getContainerDocuments();
		assertEquals(2, containerDocuments.size()); // Zip Container content - original docs
		
		for (DSSDocument conteinerContent : containerDocuments) {
			boolean signedDocFound = false;
			for (DSSDocument document : documentsToSign) {
				if (conteinerContent.getName().equals(document.getName())) {
					signedDocFound = true;
				}
			}
			assertTrue(signedDocFound);
		}
	}

	@Test
	public void testCounterSignature() throws Exception {
		try (Pkcs12SignatureToken token = new Pkcs12SignatureToken(
				new FileInputStream("src/test/resources/user_a_rsa.p12"),
				new PasswordProtection("password".toCharArray()))) {
			List<DSSPrivateKeyEntry> keys = token.getKeys();
			DSSPrivateKeyEntry dssPrivateKeyEntry = keys.get(0);

			DSSDocument fileToCounterSign = new FileDocument(new File("src/test/resources/xades-detached.xml"));
			RemoteDocument signatureDocument = RemoteDocumentConverter.toRemoteDocument(fileToCounterSign);

			RemoteSignatureParameters parameters = new RemoteSignatureParameters();
			parameters.setSignatureLevel(SignatureLevel.XAdES_BASELINE_B);
			parameters.setSigningCertificate(
					new RemoteCertificate(dssPrivateKeyEntry.getCertificate().getCertificate().getEncoded()));
			parameters.setDigestAlgorithm(DigestAlgorithm.SHA256);
			parameters.setSignatureIdToCounterSign("id-3fcf6164656fd9b93f2ddbd81c1c4b4d");

			DataToBeCounterSignedDTO dataToBeCounterSignedDTO = new DataToBeCounterSignedDTO(signatureDocument,
					parameters);
			ToBeSignedDTO dataToBeCounterSigned = restClient.getDataToBeCounterSigned(dataToBeCounterSignedDTO);
			assertNotNull(dataToBeCounterSigned);

			SignatureValue signatureValue = token.sign(DTOConverter.toToBeSigned(dataToBeCounterSigned),
					DigestAlgorithm.SHA256, dssPrivateKeyEntry);

			CounterSignSignatureDTO counterSignSignatureDTO = new CounterSignSignatureDTO(signatureDocument, parameters,
					new SignatureValueDTO(signatureValue.getAlgorithm(), signatureValue.getValue()));
			RemoteDocument counterSignedDocument = restClient.counterSignSignature(counterSignSignatureDTO);
			assertNotNull(counterSignedDocument);
		}
	}

	@Test
	public void testPAdESCounterSign() throws Exception {
		try (Pkcs12SignatureToken token = new Pkcs12SignatureToken(
				new FileInputStream("src/test/resources/user_a_rsa.p12"),
				new PasswordProtection("password".toCharArray()))) {
			List<DSSPrivateKeyEntry> keys = token.getKeys();
			DSSPrivateKeyEntry dssPrivateKeyEntry = keys.get(0);

			FileDocument fileToCounterSign = new FileDocument(new File("src/test/resources/sample.pdf"));
			RemoteDocument signatureDocument = RemoteDocumentConverter.toRemoteDocument(fileToCounterSign);

			RemoteSignatureParameters parameters = new RemoteSignatureParameters();
			parameters.setSignatureLevel(SignatureLevel.PAdES_BASELINE_B);
			parameters.setSigningCertificate(
					new RemoteCertificate(dssPrivateKeyEntry.getCertificate().getCertificate().getEncoded()));
			parameters.setDigestAlgorithm(DigestAlgorithm.SHA256);

			final DataToBeCounterSignedDTO dataToBeCounterSignedDTO = new DataToBeCounterSignedDTO(signatureDocument,
					parameters);
			assertThrows(InternalServerErrorException.class,
					() -> restClient.getDataToBeCounterSigned(dataToBeCounterSignedDTO));
		}
	}

	@Test
	public void testLotlSignature() throws Exception {
		try (Pkcs12SignatureToken token = new Pkcs12SignatureToken(
				new FileInputStream("src/test/resources/user_a_rsa.p12"),
				new PasswordProtection("password".toCharArray()))) {
			List<DSSPrivateKeyEntry> keys = token.getKeys();
			DSSPrivateKeyEntry dssPrivateKeyEntry = keys.get(0);

			DSSDocument documentToSign = new FileDocument(new File("src/test/resources/eu-lotl-no-sig.xml"));
			RemoteDocument lotlToSign = RemoteDocumentConverter.toRemoteDocument(documentToSign);

			RemoteCertificate signingCertificate = new RemoteCertificate(
					dssPrivateKeyEntry.getCertificate().getCertificate().getEncoded());

			RemoteTrustedListSignatureParameters tlSignatureParameters = new RemoteTrustedListSignatureParameters();
			tlSignatureParameters.setSigningCertificate(signingCertificate);
			tlSignatureParameters.setReferenceId("lotl");
			tlSignatureParameters.setReferenceDigestAlgorithm(DigestAlgorithm.SHA512);
			tlSignatureParameters.setTlVersion("5");

			DataToSignTrustedListDTO dataToBeSignedDTO = new DataToSignTrustedListDTO(lotlToSign, tlSignatureParameters);
			ToBeSignedDTO dataToBeSigned = restTLSigningClient.getDataToSign(dataToBeSignedDTO);
			assertNotNull(dataToBeSigned);

			SignatureValue signatureValue = token.sign(DTOConverter.toToBeSigned(dataToBeSigned),
					DigestAlgorithm.SHA256, dssPrivateKeyEntry);

			SignTrustedListDTO signTrustedListDTO = new SignTrustedListDTO(lotlToSign, tlSignatureParameters,
					new SignatureValueDTO(signatureValue.getAlgorithm(), signatureValue.getValue()));
			RemoteDocument counterSignedDocument = restTLSigningClient.signDocument(signTrustedListDTO);
			assertNotNull(counterSignedDocument);

			tlSignatureParameters.setTlVersion("7");
			assertThrows(InternalServerErrorException.class, () -> restTLSigningClient.getDataToSign(dataToBeSignedDTO));
		}
	}

	@Test
	public void testXmlManifestSignature() throws Exception {
		try (Pkcs12SignatureToken token = new Pkcs12SignatureToken(new FileInputStream("src/test/resources/user_a_rsa.p12"),
				new PasswordProtection("password".toCharArray()))) {

			List<DSSPrivateKeyEntry> keys = token.getKeys();
			DSSPrivateKeyEntry dssPrivateKeyEntry = keys.get(0);

			RemoteSignatureParameters parameters = new RemoteSignatureParameters();
			parameters.setSignatureLevel(SignatureLevel.XAdES_BASELINE_B);
			parameters.setSigningCertificate(new RemoteCertificate(dssPrivateKeyEntry.getCertificate().getCertificate().getEncoded()));
			parameters.setSignaturePackaging(SignaturePackaging.ENVELOPING);
			parameters.setDigestAlgorithm(DigestAlgorithm.SHA256);
			parameters.setManifestSignature(true);

			FileDocument fileToSign = new FileDocument(new File("src/test/resources/manifest-joue.xml"));
			RemoteDocument toSignDocument = new RemoteDocument(Utils.toByteArray(fileToSign.openStream()), fileToSign.getName());
			ToBeSignedDTO dataToSign = restClient.getDataToSign(new DataToSignOneDocumentDTO(toSignDocument, parameters));
			assertNotNull(dataToSign);

			SignatureValue signatureValue = token.sign(DTOConverter.toToBeSigned(dataToSign), DigestAlgorithm.SHA256, dssPrivateKeyEntry);
			SignOneDocumentDTO signDocument = new SignOneDocumentDTO(toSignDocument, parameters,
					new SignatureValueDTO(signatureValue.getAlgorithm(), signatureValue.getValue()));
			RemoteDocument signedDocument = restClient.signDocument(signDocument);
			assertNotNull(signedDocument);

			assertTrue(new String(signedDocument.getBytes()).contains("Type=\"http://www.w3.org/2000/09/xmldsig#Manifest\""));
		}
	}

	@Test
	public void testEmbedXmlSignature() throws Exception {
		try (Pkcs12SignatureToken token = new Pkcs12SignatureToken(new FileInputStream("src/test/resources/user_a_rsa.p12"),
				new PasswordProtection("password".toCharArray()))) {

			List<DSSPrivateKeyEntry> keys = token.getKeys();
			DSSPrivateKeyEntry dssPrivateKeyEntry = keys.get(0);

			RemoteSignatureParameters parameters = new RemoteSignatureParameters();
			parameters.setSignatureLevel(SignatureLevel.XAdES_BASELINE_B);
			parameters.setSigningCertificate(new RemoteCertificate(dssPrivateKeyEntry.getCertificate().getCertificate().getEncoded()));
			parameters.setSignaturePackaging(SignaturePackaging.ENVELOPING);
			parameters.setDigestAlgorithm(DigestAlgorithm.SHA256);
			parameters.setEmbedXML(true);

			FileDocument fileToSign = new FileDocument(new File("src/test/resources/sample.xml"));
			RemoteDocument toSignDocument = new RemoteDocument(Utils.toByteArray(fileToSign.openStream()), fileToSign.getName());
			ToBeSignedDTO dataToSign = restClient.getDataToSign(new DataToSignOneDocumentDTO(toSignDocument, parameters));
			assertNotNull(dataToSign);

			SignatureValue signatureValue = token.sign(DTOConverter.toToBeSigned(dataToSign), DigestAlgorithm.SHA256, dssPrivateKeyEntry);
			SignOneDocumentDTO signDocument = new SignOneDocumentDTO(toSignDocument, parameters,
					new SignatureValueDTO(signatureValue.getAlgorithm(), signatureValue.getValue()));
			RemoteDocument signedDocument = restClient.signDocument(signDocument);
			assertNotNull(signedDocument);

			String documentContent = new String(signedDocument.getBytes());
			assertTrue(documentContent.contains("<h:td>Hello</h:td>"));
			assertTrue(documentContent.contains("<h:td>World</h:td>"));
		}
	}

}
