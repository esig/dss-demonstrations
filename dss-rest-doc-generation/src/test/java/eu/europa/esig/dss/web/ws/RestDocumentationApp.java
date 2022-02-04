package eu.europa.esig.dss.web.ws;

import eu.europa.esig.dss.enumerations.ASiCContainerType;
import eu.europa.esig.dss.enumerations.DigestAlgorithm;
import eu.europa.esig.dss.enumerations.SignatureLevel;
import eu.europa.esig.dss.enumerations.SignaturePackaging;
import eu.europa.esig.dss.enumerations.SignerTextHorizontalAlignment;
import eu.europa.esig.dss.enumerations.SignerTextPosition;
import eu.europa.esig.dss.enumerations.TimestampContainerForm;
import eu.europa.esig.dss.model.DSSDocument;
import eu.europa.esig.dss.model.FileDocument;
import eu.europa.esig.dss.model.SignatureValue;
import eu.europa.esig.dss.spi.DSSUtils;
import eu.europa.esig.dss.token.DSSPrivateKeyEntry;
import eu.europa.esig.dss.token.Pkcs12SignatureToken;
import eu.europa.esig.dss.ws.cert.validation.dto.CertificateToValidateDTO;
import eu.europa.esig.dss.ws.converter.ColorConverter;
import eu.europa.esig.dss.ws.converter.DTOConverter;
import eu.europa.esig.dss.ws.converter.RemoteDocumentConverter;
import eu.europa.esig.dss.ws.dto.DigestDTO;
import eu.europa.esig.dss.ws.dto.RemoteCertificate;
import eu.europa.esig.dss.ws.dto.RemoteDocument;
import eu.europa.esig.dss.ws.dto.SignatureValueDTO;
import eu.europa.esig.dss.ws.dto.ToBeSignedDTO;
import eu.europa.esig.dss.ws.server.signing.dto.RemoteKeyEntry;
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
import eu.europa.esig.dss.ws.timestamp.dto.TimestampResponseDTO;
import eu.europa.esig.dss.ws.validation.dto.DataToValidateDTO;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.mapper.ObjectMapperType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.restdocs.restassured3.RestDocumentationFilter;

import java.awt.Color;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.security.KeyStore.PasswordProtection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessRequest;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.restassured3.RestAssuredRestDocumentation.document;
import static org.springframework.restdocs.restassured3.RestAssuredRestDocumentation.documentationConfiguration;

@ExtendWith(RestDocumentationExtension.class)
public class RestDocumentationApp {

	private RestDocumentationFilter documentationFilter;

	private RequestSpecification spec;

	@BeforeEach
	public void setUp(RestDocumentationContextProvider restDocumentation) throws Exception {
		this.documentationFilter = document("{method-name}/{step}/", preprocessRequest(prettyPrint()), preprocessResponse(prettyPrint()));
		this.spec = new RequestSpecBuilder().addFilter(documentationConfiguration(restDocumentation)).addFilter(this.documentationFilter).build();
	}

	@Test
	public void getKeys() throws Exception {
		given(this.spec).accept(ContentType.JSON).get("/services/rest/server-signing/keys").then().assertThat().statusCode(equalTo(200));
	}

	@Test
	public void getKey() throws Exception {
		Response response = given(this.spec).accept(ContentType.JSON).get("/services/rest/server-signing/key/{alias}", "certificate");
		response.then().assertThat().statusCode(equalTo(200));
		RemoteKeyEntry entry = response.andReturn().as(RemoteKeyEntry.class);
		assertNotNull(entry);
		assertNotNull(entry.getCertificate());
		assertNotNull(entry.getEncryptionAlgo());
		assertEquals("certificate", entry.getAlias());
	}

	@Test
	public void signAndExtendOneDocument() throws Exception {

		try (Pkcs12SignatureToken token = new Pkcs12SignatureToken(new FileInputStream("src/test/resources/user_a_rsa.p12"),
				new PasswordProtection("password".toCharArray()))) {

			DSSPrivateKeyEntry dssPrivateKeyEntry = token.getKeys().get(0);

			DataToSignOneDocumentDTO dataToSign = new DataToSignOneDocumentDTO();

			RemoteSignatureParameters parameters = new RemoteSignatureParameters();
			parameters.setSignatureLevel(SignatureLevel.XAdES_BASELINE_B);
			parameters.setSignaturePackaging(SignaturePackaging.ENVELOPING);
			parameters.setDigestAlgorithm(DigestAlgorithm.SHA256);
			parameters.setSigningCertificate(new RemoteCertificate(dssPrivateKeyEntry.getCertificate().getEncoded()));
			dataToSign.setParameters(parameters);

			RemoteDocument toSignDocument = new RemoteDocument();
			toSignDocument.setBytes("Hello".getBytes("UTF-8"));
			dataToSign.setToSignDocument(toSignDocument);

			// get data to sign

			Response responseGetDataToSign = given(this.spec).accept(ContentType.JSON).contentType(ContentType.JSON).accept(
					ContentType.JSON)
					.body(dataToSign, ObjectMapperType.JACKSON_2)
					.post("/services/rest/signature/one-document/getDataToSign");
			responseGetDataToSign.then().assertThat().statusCode(equalTo(200));
			ToBeSignedDTO toBeSignedDTO = responseGetDataToSign.andReturn().as(ToBeSignedDTO.class);
			assertNotNull(toBeSignedDTO);

			// sign locally
			SignatureValue signatureValue = token.sign(DTOConverter.toToBeSigned(toBeSignedDTO), parameters.getDigestAlgorithm(), dssPrivateKeyEntry);
			assertNotNull(signatureValue);

			SignOneDocumentDTO signOneDoc = new SignOneDocumentDTO();
			signOneDoc.setToSignDocument(toSignDocument);
			signOneDoc.setParameters(parameters);
			signOneDoc.setSignatureValue(DTOConverter.toSignatureValueDTO(signatureValue));

			// sign document
			Response responseSignDocument = given(this.spec).accept(ContentType.JSON).contentType(ContentType.JSON).accept(ContentType.JSON)
					.accept(ContentType.JSON).body(signOneDoc, ObjectMapperType.JACKSON_2).post("/services/rest/signature/one-document/signDocument");
			responseSignDocument.then().assertThat().statusCode(equalTo(200));

			RemoteDocument signedDocument = responseSignDocument.andReturn().as(RemoteDocument.class);
			assertNotNull(signedDocument);
			assertNotNull(signedDocument.getBytes());

			ExtendDocumentDTO extendOneDoc = new ExtendDocumentDTO();
			parameters = new RemoteSignatureParameters();
			parameters.setSignatureLevel(SignatureLevel.XAdES_BASELINE_T);
			extendOneDoc.setParameters(parameters);
			extendOneDoc.setToExtendDocument(signedDocument);

			// extend signed document
			Response extendResponse = given(this.spec).accept(ContentType.JSON).contentType(ContentType.JSON).accept(ContentType.JSON)
					.body(extendOneDoc, ObjectMapperType.JACKSON_2).post("/services/rest/signature/one-document/extendDocument");
			extendResponse.then().assertThat().statusCode(equalTo(200));
			RemoteDocument extendedDocument = extendResponse.andReturn().as(RemoteDocument.class);
			assertNotNull(extendedDocument);
			assertNotNull(extendedDocument.getBytes());

		}
	}

	@Test
	public void signPdfVisible() throws Exception {

		try (Pkcs12SignatureToken token = new Pkcs12SignatureToken(new FileInputStream("src/test/resources/user_a_rsa.p12"),
				new PasswordProtection("password".toCharArray()))) {

			DSSPrivateKeyEntry dssPrivateKeyEntry = token.getKeys().get(0);

			DataToSignOneDocumentDTO dataToSign = new DataToSignOneDocumentDTO();

			RemoteSignatureParameters parameters = new RemoteSignatureParameters();
			parameters.setSignatureLevel(SignatureLevel.PAdES_BASELINE_B);
			parameters.setDigestAlgorithm(DigestAlgorithm.SHA256);
			parameters.setSigningCertificate(new RemoteCertificate(dssPrivateKeyEntry.getCertificate().getEncoded()));

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
			textParameters.setSignerTextPosition(SignerTextPosition.TOP);
			textParameters.setSignerTextHorizontalAlignment(SignerTextHorizontalAlignment.CENTER);
			textParameters.setTextColor(ColorConverter.toRemoteColor(Color.BLUE));
			textParameters.setBackgroundColor(ColorConverter.toRemoteColor(Color.WHITE));
			imageParameters.setTextParameters(textParameters);
			
			parameters.setImageParameters(imageParameters);
			dataToSign.setParameters(parameters);

			RemoteDocument toSignDocument = RemoteDocumentConverter.toRemoteDocument(new FileDocument("src/test/resources/sample.pdf"));
			dataToSign.setToSignDocument(toSignDocument);

			// get data to sign

			Response responseGetDataToSign = given(this.spec).accept(ContentType.JSON).contentType(ContentType.JSON).accept(ContentType.JSON)
					.body(dataToSign, ObjectMapperType.JACKSON_2).post("/services/rest/signature/one-document/getDataToSign");
			responseGetDataToSign.then().assertThat().statusCode(equalTo(200));
			ToBeSignedDTO toBeSignedDTO = responseGetDataToSign.andReturn().as(ToBeSignedDTO.class);
			assertNotNull(toBeSignedDTO);

			// sign locally
			SignatureValue signatureValue = token.sign(DTOConverter.toToBeSigned(toBeSignedDTO), parameters.getDigestAlgorithm(), dssPrivateKeyEntry);
			assertNotNull(signatureValue);

			SignOneDocumentDTO signOneDoc = new SignOneDocumentDTO();
			signOneDoc.setToSignDocument(toSignDocument);
			signOneDoc.setParameters(parameters);
			signOneDoc.setSignatureValue(DTOConverter.toSignatureValueDTO(signatureValue));

			// sign document
			Response responseSignDocument = given(this.spec).accept(ContentType.JSON).contentType(ContentType.JSON).accept(ContentType.JSON)
					.accept(ContentType.JSON).body(signOneDoc, ObjectMapperType.JACKSON_2).post("/services/rest/signature/one-document/signDocument");
			responseSignDocument.then().assertThat().statusCode(equalTo(200));

			RemoteDocument signedDocument = responseSignDocument.andReturn().as(RemoteDocument.class);
			assertNotNull(signedDocument);
			assertNotNull(signedDocument.getBytes());
		}
	}

	@Test
	public void signDigestDocumentRemotely() throws Exception {

		// retrieve key to be used on server A
		Response response = given(this.spec).accept(ContentType.JSON).get("/services/rest/server-signing/key/{alias}", "certificate");
		response.then().assertThat().statusCode(equalTo(200));
		RemoteKeyEntry entry = response.andReturn().as(RemoteKeyEntry.class);
		assertNotNull(entry);
		assertNotNull(entry.getCertificate());
		assertNotNull(entry.getEncryptionAlgo());
		assertEquals("certificate", entry.getAlias());

		DataToSignOneDocumentDTO dataToSign = new DataToSignOneDocumentDTO();
		RemoteSignatureParameters parameters = new RemoteSignatureParameters();
		parameters.setSignatureLevel(SignatureLevel.CAdES_BASELINE_B);
		parameters.setSignaturePackaging(SignaturePackaging.DETACHED);
		parameters.setDigestAlgorithm(DigestAlgorithm.SHA256);
		parameters.setSigningCertificate(entry.getCertificate());
		parameters.setCertificateChain(Arrays.asList(entry.getCertificateChain()));
		dataToSign.setParameters(parameters);

		FileDocument doc = new FileDocument("src/test/resources/sample.xml");

		RemoteDocument toSignDocument = new RemoteDocument();
		toSignDocument.setDigestAlgorithm(DigestAlgorithm.SHA256);
		toSignDocument.setBytes(DSSUtils.digest(DigestAlgorithm.SHA256, doc));
		dataToSign.setToSignDocument(toSignDocument);

		// get data to sign on server B
		Response responseGetDataToSign = given(this.spec).accept(ContentType.JSON).contentType(ContentType.JSON).body(dataToSign, ObjectMapperType.JACKSON_2)
				.post("/services/rest/signature/one-document/getDataToSign");
		responseGetDataToSign.then().assertThat().statusCode(equalTo(200));
		ToBeSignedDTO toBeSignedDTO = responseGetDataToSign.andReturn().as(ToBeSignedDTO.class);
		assertNotNull(toBeSignedDTO);

		// sign on server A
		Response responseSignature = given(this.spec).accept(ContentType.JSON).contentType(ContentType.JSON).body(toBeSignedDTO)
				.post("/services/rest/server-signing/sign/{alias}/{digest-algo}", "certificate", parameters.getDigestAlgorithm());

		responseSignature.then().assertThat().statusCode(equalTo(200));
		SignatureValueDTO signatureValue = responseSignature.andReturn().as(SignatureValueDTO.class);
		assertNotNull(signatureValue);
		assertNotNull(signatureValue.getAlgorithm());
		assertNotNull(signatureValue.getValue());

		SignOneDocumentDTO signOneDoc = new SignOneDocumentDTO();
		signOneDoc.setParameters(parameters);
		signOneDoc.setToSignDocument(toSignDocument);
		signOneDoc.setSignatureValue(signatureValue);

		// sign document on server B
		Response respondeSignDocuments = given(this.spec).accept(ContentType.JSON).contentType(ContentType.JSON).body(signOneDoc, ObjectMapperType.JACKSON_2)
				.post("/services/rest/signature/one-document/signDocument");
		respondeSignDocuments.then().assertThat().statusCode(equalTo(200));
		RemoteDocument signedDocument = respondeSignDocuments.andReturn().as(RemoteDocument.class);
		assertNotNull(signedDocument);
		assertNotNull(signedDocument.getBytes());
	}

	@Test
	public void signMultipleDocuments() throws Exception {

		try (Pkcs12SignatureToken token = new Pkcs12SignatureToken(new FileInputStream("src/test/resources/user_a_rsa.p12"),
				new PasswordProtection("password".toCharArray()))) {

			DSSPrivateKeyEntry dssPrivateKeyEntry = token.getKeys().get(0);

			DataToSignMultipleDocumentsDTO dataToSignMultiDocs = new DataToSignMultipleDocumentsDTO();

			RemoteSignatureParameters parameters = new RemoteSignatureParameters();
			parameters.setAsicContainerType(ASiCContainerType.ASiC_E);
			parameters.setSignatureLevel(SignatureLevel.XAdES_BASELINE_B);
			parameters.setDigestAlgorithm(DigestAlgorithm.SHA256);
			parameters.setSigningCertificate(new RemoteCertificate(dssPrivateKeyEntry.getCertificate().getEncoded()));
			dataToSignMultiDocs.setParameters(parameters);

			List<RemoteDocument> toSignDocuments = new ArrayList<>();
			RemoteDocument doc1 = new RemoteDocument();
			doc1.setBytes("Hello".getBytes("UTF-8"));
			doc1.setName("test1.bin");
			toSignDocuments.add(doc1);

			RemoteDocument doc2 = new RemoteDocument();
			doc2.setBytes("World".getBytes("UTF-8"));
			doc2.setName("test2.bin");
			toSignDocuments.add(doc2);
			dataToSignMultiDocs.setToSignDocuments(toSignDocuments);

			// get data to sign
			Response responseGetDataToSign = given(this.spec).accept(ContentType.JSON).contentType(ContentType.JSON)
					.body(dataToSignMultiDocs, ObjectMapperType.JACKSON_2).post("/services/rest/signature/multiple-documents/getDataToSignMultiple");
			responseGetDataToSign.then().assertThat().statusCode(equalTo(200));
			ToBeSignedDTO toBeSignedDTO = responseGetDataToSign.andReturn().as(ToBeSignedDTO.class);
			assertNotNull(toBeSignedDTO);

			// sign locally
			SignatureValue signatureValue = token.sign(DTOConverter.toToBeSigned(toBeSignedDTO), parameters.getDigestAlgorithm(), dssPrivateKeyEntry);
			assertNotNull(signatureValue);

			SignMultipleDocumentDTO signMultiDocsDto = new SignMultipleDocumentDTO();
			signMultiDocsDto.setToSignDocuments(toSignDocuments);
			signMultiDocsDto.setParameters(parameters);
			signMultiDocsDto.setSignatureValue(DTOConverter.toSignatureValueDTO(signatureValue));

			// sign documents
			Response respondeSignDocuments = given(this.spec).accept(ContentType.JSON).contentType(ContentType.JSON)
					.body(signMultiDocsDto, ObjectMapperType.JACKSON_2).post("/services/rest/signature/multiple-documents/signDocument");
			respondeSignDocuments.then().assertThat().statusCode(equalTo(200));
			RemoteDocument signedDocument = respondeSignDocuments.andReturn().as(RemoteDocument.class);
			assertNotNull(signedDocument);
			assertNotNull(signedDocument.getBytes());
		}
	}

	@Test
	public void validateDoc() throws IOException {

		DataToValidateDTO dataToValidateDTO = new DataToValidateDTO();

		File signature = new File("src/test/resources/xades-detached.xml");
		RemoteDocument signedDoc = new RemoteDocument();
		signedDoc.setBytes(toByteArray(signature));
		signedDoc.setName(signature.getName());
		dataToValidateDTO.setSignedDocument(signedDoc);

		File detached = new File("src/test/resources/sample.xml");
		RemoteDocument originalDoc = new RemoteDocument();
		originalDoc.setBytes(toByteArray(detached));
		originalDoc.setName(detached.getName());

		dataToValidateDTO.setOriginalDocuments(Arrays.asList(originalDoc));

		given(this.spec).accept(ContentType.JSON).contentType(ContentType.JSON).body(dataToValidateDTO, ObjectMapperType.JACKSON_2)
				.post("/services/rest/validation/validateSignature").then().assertThat().statusCode(equalTo(200));

	}

	@Test
	public void validateCert() throws IOException {

		CertificateToValidateDTO dataToValidateDTO = new CertificateToValidateDTO();

		dataToValidateDTO.setCertificate(new RemoteCertificate(Base64.getDecoder().decode(
				"MIIC6jCCAdKgAwIBAgIGLtYU17tXMA0GCSqGSIb3DQEBCwUAMDAxGzAZBgNVBAMMElJvb3RTZWxmU2lnbmVkRmFrZTERMA8GA1UECgwIRFNTLXRlc3QwHhcNMTcwNjA4MTEyNjAxWhcNNDcwNzA0MDc1NzI0WjAoMRMwEQYDVQQDDApTaWduZXJGYWtlMREwDwYDVQQKDAhEU1MtdGVzdDCCASIwDQYJKoZIhvcNAQEBBQADggEPADCCAQoCggEBAMI3kZhtnipn+iiZHZ9ax8FlfE5Ow/cFwBTfAEb3R1ZQUp6/BQnBt7Oo0JWBtc9qkv7JUDdcBJXPV5QWS5AyMPHpqQ75Hitjsq/Fzu8eHtkKpFizcxGa9BZdkQjh4rSrtO1Kjs0Rd5DQtWSgkeVCCN09kN0ZsZ0ENY+Ip8QxSmyztsStkYXdULqpwz4JEXW9vz64eTbde4vQJ6pjHGarJf1gQNEc2XzhmI/prXLysWNqC7lZg7PUZUTrdegABTUzYCRJ1kWBRPm4qo0LN405c94QQd45a5kTgowHzEgLnAQI28x0M3A59TKC+ieNc6VF1PsTLpUw7PNI2VstX5jAuasCAwEAAaMSMBAwDgYDVR0PAQH/BAQDAgEGMA0GCSqGSIb3DQEBCwUAA4IBAQCK6LGA01TR+rmU8p6yhAi4OkDN2b1dbIL8l8iCMYopLCxx8xqq3ubZCOxqh1X2j6pgWzarb0b/MUix00IoUvNbFOxAW7PBZIKDLnm6LsckRxs1U32sC9d1LOHe3WKBNB6GZALT1ewjh7hSbWjftlmcovq+6eVGA5cvf2u/2+TkKkyHV/NR394nXrdsdpvygwypEtXjetzD7UT93Nuw3xcV8VIftIvHf9LjU7h+UjGmKXG9c15eYr3SzUmv6kyOI0Bvw14PWtsWGl0QdOSRvIBBrP4adCnGTgjgjk9LTcO8B8FKrr+8lHGuc0bp4lIUToiUkGILXsiEeEg9WAqm+XqO")));

		dataToValidateDTO.setCertificateChain(Arrays.asList(new RemoteCertificate(Base64.getDecoder().decode(
				"MIIC6jCCAdKgAwIBAgIGLtYU17tXMA0GCSqGSIb3DQEBCwUAMDAxGzAZBgNVBAMMElJvb3RTZWxmU2lnbmVkRmFrZTERMA8GA1UECgwIRFNTLXRlc3QwHhcNMTcwNjA4MTEyNjAxWhcNNDcwNzA0MDc1NzI0WjAoMRMwEQYDVQQDDApTaWduZXJGYWtlMREwDwYDVQQKDAhEU1MtdGVzdDCCASIwDQYJKoZIhvcNAQEBBQADggEPADCCAQoCggEBAMI3kZhtnipn+iiZHZ9ax8FlfE5Ow/cFwBTfAEb3R1ZQUp6/BQnBt7Oo0JWBtc9qkv7JUDdcBJXPV5QWS5AyMPHpqQ75Hitjsq/Fzu8eHtkKpFizcxGa9BZdkQjh4rSrtO1Kjs0Rd5DQtWSgkeVCCN09kN0ZsZ0ENY+Ip8QxSmyztsStkYXdULqpwz4JEXW9vz64eTbde4vQJ6pjHGarJf1gQNEc2XzhmI/prXLysWNqC7lZg7PUZUTrdegABTUzYCRJ1kWBRPm4qo0LN405c94QQd45a5kTgowHzEgLnAQI28x0M3A59TKC+ieNc6VF1PsTLpUw7PNI2VstX5jAuasCAwEAAaMSMBAwDgYDVR0PAQH/BAQDAgEGMA0GCSqGSIb3DQEBCwUAA4IBAQCK6LGA01TR+rmU8p6yhAi4OkDN2b1dbIL8l8iCMYopLCxx8xqq3ubZCOxqh1X2j6pgWzarb0b/MUix00IoUvNbFOxAW7PBZIKDLnm6LsckRxs1U32sC9d1LOHe3WKBNB6GZALT1ewjh7hSbWjftlmcovq+6eVGA5cvf2u/2+TkKkyHV/NR394nXrdsdpvygwypEtXjetzD7UT93Nuw3xcV8VIftIvHf9LjU7h+UjGmKXG9c15eYr3SzUmv6kyOI0Bvw14PWtsWGl0QdOSRvIBBrP4adCnGTgjgjk9LTcO8B8FKrr+8lHGuc0bp4lIUToiUkGILXsiEeEg9WAqm+XqO"))));

		given(this.spec).accept(ContentType.JSON).contentType(ContentType.JSON).body(dataToValidateDTO, ObjectMapperType.JACKSON_2)
				.post("/services/rest/certificate-validation/validateCertificate").then().assertThat().statusCode(equalTo(200));

	}

	@Test
	public void validateDigestDoc() throws IOException {

		DataToValidateDTO dataToValidateDTO = new DataToValidateDTO();

		File signature = new File("src/test/resources/xades-detached.xml");
		RemoteDocument signedDoc = new RemoteDocument();
		signedDoc.setBytes(toByteArray(signature));
		signedDoc.setName(signature.getName());
		dataToValidateDTO.setSignedDocument(signedDoc);

		FileDocument detached = new FileDocument("src/test/resources/sample.xml");
		RemoteDocument originalDoc = new RemoteDocument();
		originalDoc.setDigestAlgorithm(DigestAlgorithm.SHA256);
		originalDoc.setBytes(DSSUtils.digest(DigestAlgorithm.SHA256, detached));
		originalDoc.setName(detached.getName());

		dataToValidateDTO.setOriginalDocuments(Arrays.asList(originalDoc));

		given(this.spec).accept(ContentType.JSON).contentType(ContentType.JSON).body(dataToValidateDTO, ObjectMapperType.JACKSON_2)
				.post("/services/rest/validation/validateSignature").then().assertThat().statusCode(equalTo(200));

	}

	@Test
	@SuppressWarnings("unchecked")
	public void getOriginalDocuments() throws IOException {

		DataToValidateDTO dataToValidateDTO = new DataToValidateDTO();

		File signature = new File("src/test/resources/hello-signed-xades.xml");
		RemoteDocument signedDoc = new RemoteDocument();
		signedDoc.setBytes(toByteArray(signature));
		signedDoc.setName(signature.getName());
		dataToValidateDTO.setSignedDocument(signedDoc);

//		dataToValidateDTO.setSignatureId("id-signature");

		dataToValidateDTO.setSignedDocument(signedDoc);

		Response response = given(this.spec).accept(ContentType.JSON).contentType(ContentType.JSON).body(dataToValidateDTO, ObjectMapperType.JACKSON_2)
				.post("/services/rest/validation/getOriginalDocuments");
		response.then().assertThat().statusCode(equalTo(200));
		List<RemoteDocument> originals = response.andReturn().as(List.class);
		assertNotNull(originals);
		assertEquals(1, originals.size());
	}

	@Test
	public void getTimestampResponse() {

		DigestDTO digestToTimestamp = new DigestDTO(DigestAlgorithm.SHA256, DSSUtils.digest(DigestAlgorithm.SHA256, "Hello world".getBytes()));

		Response response = given(this.spec).accept(ContentType.JSON).contentType(ContentType.JSON).body(digestToTimestamp, ObjectMapperType.JACKSON_2)
				.post("/services/rest/timestamp-service/getTimestampResponse");
		response.then().assertThat().statusCode(equalTo(200));
		TimestampResponseDTO timestampResponse = response.andReturn().as(TimestampResponseDTO.class);
		assertNotNull(timestampResponse);
		assertNotNull(timestampResponse.getBinaries());
	}

	@Test
	public void timestampOneDocument() {
		RemoteDocument pdfToTimestamp = RemoteDocumentConverter.toRemoteDocument(new FileDocument("src/test/resources/sample.pdf"));

		RemoteTimestampParameters timestampParameters = new RemoteTimestampParameters(TimestampContainerForm.PDF, DigestAlgorithm.SHA256);
		TimestampOneDocumentDTO dto = new TimestampOneDocumentDTO(pdfToTimestamp, timestampParameters);

		Response response = given(this.spec).accept(ContentType.JSON).contentType(ContentType.JSON).body(dto, ObjectMapperType.JACKSON_2)
				.post("/services/rest/signature/one-document/timestampDocument");
		response.then().assertThat().statusCode(equalTo(200));
		RemoteDocument timestampResponse = response.andReturn().as(RemoteDocument.class);
		assertNotNull(timestampResponse);
		assertNotNull(timestampResponse.getBytes());
	}

	@Test
	public void timestampMultipleDocuments() {
		RemoteDocument pdfToTimestamp = RemoteDocumentConverter.toRemoteDocument(new FileDocument("src/test/resources/sample.pdf"));
		RemoteDocument xmlToTimestamp = RemoteDocumentConverter.toRemoteDocument(new FileDocument("src/test/resources/sample.xml"));

		RemoteTimestampParameters timestampParameters = new RemoteTimestampParameters(TimestampContainerForm.ASiC_E, DigestAlgorithm.SHA256);
		TimestampMultipleDocumentDTO dto = new TimestampMultipleDocumentDTO(Arrays.asList(pdfToTimestamp, xmlToTimestamp), timestampParameters);

		Response response = given(this.spec).accept(ContentType.JSON).contentType(ContentType.JSON).body(dto, ObjectMapperType.JACKSON_2)
				.post("/services/rest/signature/multiple-documents/timestampDocument");
		response.then().assertThat().statusCode(equalTo(200));
		RemoteDocument timestampResponse = response.andReturn().as(RemoteDocument.class);
		assertNotNull(timestampResponse);
		assertNotNull(timestampResponse.getBytes());
	}

	@Test
	public void counterSignSignature() throws Exception {
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
			parameters.setSignatureIdToCounterSign("id-afde782436468dd74eeb181f7ce110e1");

			// get data to be counter signed

			DataToBeCounterSignedDTO dataToBeCounterSignedDTO = new DataToBeCounterSignedDTO(signatureDocument,
					parameters);
			Response responseGetDataToSign = given(this.spec).accept(ContentType.JSON).contentType(ContentType.JSON)
					.accept(ContentType.JSON).body(dataToBeCounterSignedDTO, ObjectMapperType.JACKSON_2)
					.post("/services/rest/signature/one-document/getDataToBeCounterSigned");
			responseGetDataToSign.then().assertThat().statusCode(equalTo(200));
			ToBeSignedDTO toBeCounterSignedDTO = responseGetDataToSign.andReturn().as(ToBeSignedDTO.class);
			assertNotNull(toBeCounterSignedDTO);

			// sign locally

			SignatureValue signatureValue = token.sign(DTOConverter.toToBeSigned(toBeCounterSignedDTO),
					parameters.getDigestAlgorithm(), dssPrivateKeyEntry);
			assertNotNull(signatureValue);

			// sign document

			CounterSignSignatureDTO counterSignSignatureDTO = new CounterSignSignatureDTO(signatureDocument, parameters,
					new SignatureValueDTO(signatureValue.getAlgorithm(), signatureValue.getValue()));

			Response responseSignDocument = given(this.spec).accept(ContentType.JSON).contentType(ContentType.JSON)
					.accept(ContentType.JSON).accept(ContentType.JSON)
					.body(counterSignSignatureDTO, ObjectMapperType.JACKSON_2)
					.post("/services/rest/signature/one-document/counterSignSignature");
			responseSignDocument.then().assertThat().statusCode(equalTo(200));

			RemoteDocument signedDocument = responseSignDocument.andReturn().as(RemoteDocument.class);
			assertNotNull(signedDocument);
			assertNotNull(signedDocument.getBytes());
		}
	}

	@Test
	public void tlSignature() throws Exception {
		try (Pkcs12SignatureToken token = new Pkcs12SignatureToken(
				new FileInputStream("src/test/resources/user_a_rsa.p12"),
				new PasswordProtection("password".toCharArray()))) {

			List<DSSPrivateKeyEntry> keys = token.getKeys();
			DSSPrivateKeyEntry dssPrivateKeyEntry = keys.get(0);

			DSSDocument documentToSign = new FileDocument(new File("src/test/resources/trusted-list.xml"));
			documentToSign.setName("tl.xml");
			RemoteDocument tlToSign = RemoteDocumentConverter.toRemoteDocument(documentToSign);

			RemoteCertificate signingCertificate = new RemoteCertificate(
					dssPrivateKeyEntry.getCertificate().getCertificate().getEncoded());

			RemoteTrustedListSignatureParameters tlSignatureParameters = new RemoteTrustedListSignatureParameters();
			tlSignatureParameters.setSigningCertificate(signingCertificate);
			tlSignatureParameters.setReferenceId("tl");
			tlSignatureParameters.setReferenceDigestAlgorithm(DigestAlgorithm.SHA512);

			Date signingTime = DSSUtils.getUtcDate(2021, 9, 3);
			RemoteBLevelParameters bLevelParameters = new RemoteBLevelParameters();
			bLevelParameters.setSigningDate(signingTime);
			tlSignatureParameters.setBLevelParameters(bLevelParameters);

			// get data to be signed
			DataToSignTrustedListDTO dataToBeSignedDTO = new DataToSignTrustedListDTO(tlToSign, tlSignatureParameters);
			Response responseGetDataToSign = given(this.spec).accept(ContentType.JSON).contentType(ContentType.JSON)
					.accept(ContentType.JSON).body(dataToBeSignedDTO, ObjectMapperType.JACKSON_2)
					.post("/services/rest/signature/trusted-list/getDataToSign");
			responseGetDataToSign.then().assertThat().statusCode(equalTo(200));
			ToBeSignedDTO dataToBeSigned = responseGetDataToSign.andReturn().as(ToBeSignedDTO.class);
			assertNotNull(dataToBeSigned);

			SignatureValue signatureValue = token.sign(DTOConverter.toToBeSigned(dataToBeSigned),
					DigestAlgorithm.SHA256, dssPrivateKeyEntry);

			SignTrustedListDTO signTrustedListDTO = new SignTrustedListDTO(tlToSign, tlSignatureParameters,
					new SignatureValueDTO(signatureValue.getAlgorithm(), signatureValue.getValue()));

			Response responseSignDocument = given(this.spec).accept(ContentType.JSON).contentType(ContentType.JSON)
					.accept(ContentType.JSON).accept(ContentType.JSON)
					.body(signTrustedListDTO, ObjectMapperType.JACKSON_2)
					.post("/services/rest/signature/trusted-list/signDocument");
			responseSignDocument.then().assertThat().statusCode(equalTo(200));

			RemoteDocument signedDocument = responseSignDocument.andReturn().as(RemoteDocument.class);
			assertNotNull(signedDocument);
			assertNotNull(signedDocument.getBytes());
		}
	}

	private byte[] toByteArray(File file) throws IOException {
		return Files.readAllBytes(file.toPath());
	}

}
