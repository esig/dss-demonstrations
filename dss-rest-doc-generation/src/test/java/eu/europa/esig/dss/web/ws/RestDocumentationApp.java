package eu.europa.esig.dss.web.ws;

import static org.hamcrest.Matchers.equalTo;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessRequest;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.restassured3.RestAssuredRestDocumentation.document;
import static org.springframework.restdocs.restassured3.RestAssuredRestDocumentation.documentationConfiguration;

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

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.restdocs.JUnitRestDocumentation;
import org.springframework.restdocs.restassured3.RestDocumentationFilter;

import eu.europa.esig.dss.enumerations.ASiCContainerType;
import eu.europa.esig.dss.enumerations.DigestAlgorithm;
import eu.europa.esig.dss.enumerations.SignatureAlgorithm;
import eu.europa.esig.dss.enumerations.SignatureLevel;
import eu.europa.esig.dss.enumerations.SignaturePackaging;
import eu.europa.esig.dss.model.FileDocument;
import eu.europa.esig.dss.model.ToBeSigned;
import eu.europa.esig.dss.spi.DSSUtils;
import eu.europa.esig.dss.token.DSSPrivateKeyEntry;
import eu.europa.esig.dss.token.Pkcs12SignatureToken;
import eu.europa.esig.dss.ws.cert.validation.dto.CertificateToValidateDTO;
import eu.europa.esig.dss.ws.dto.RemoteCertificate;
import eu.europa.esig.dss.ws.dto.RemoteDocument;
import eu.europa.esig.dss.ws.dto.SignatureValueDTO;
import eu.europa.esig.dss.ws.signature.dto.DataToSignMultipleDocumentsDTO;
import eu.europa.esig.dss.ws.signature.dto.DataToSignOneDocumentDTO;
import eu.europa.esig.dss.ws.signature.dto.ExtendDocumentDTO;
import eu.europa.esig.dss.ws.signature.dto.SignMultipleDocumentDTO;
import eu.europa.esig.dss.ws.signature.dto.SignOneDocumentDTO;
import eu.europa.esig.dss.ws.signature.dto.parameters.RemoteBLevelParameters;
import eu.europa.esig.dss.ws.signature.dto.parameters.RemoteSignatureParameters;
import eu.europa.esig.dss.ws.validation.dto.DataToValidateDTO;
import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.mapper.ObjectMapperType;
import io.restassured.specification.RequestSpecification;

public class RestDocumentationApp {

	@Rule
	public final JUnitRestDocumentation restDocumentation = new JUnitRestDocumentation("target/generated-snippets");

	private RestDocumentationFilter documentationFilter;

	private RequestSpecification spec;

	private Pkcs12SignatureToken token;

	private Date signingDate;

	@Before
	public void setUp() throws IOException {
		this.documentationFilter = document("{method-name}", preprocessRequest(prettyPrint()), preprocessResponse(prettyPrint()));
		this.spec = new RequestSpecBuilder().addFilter(documentationConfiguration(this.restDocumentation)).addFilter(this.documentationFilter).build();
		this.token = new Pkcs12SignatureToken(new FileInputStream("src/test/resources/user_a_rsa.p12"), new PasswordProtection("password".toCharArray()));
		this.signingDate = new Date();
	}

	@Test
	public void getKeys() throws Exception {
		RestAssured.given(this.spec).accept(ContentType.JSON).get("/services/rest/server-signing/keys").then().assertThat().statusCode(equalTo(200));
	}

	@Test
	public void getKey() throws Exception {
		RestAssured.given(this.spec).accept(ContentType.JSON).get("/services/rest/server-signing/key/{alias}", "certificate").then().assertThat()
				.statusCode(equalTo(200));
	}

	@Test
	public void sign() throws Exception {
		DigestAlgorithm digestAlgorithm = DigestAlgorithm.SHA256;
		ToBeSigned tbs = new ToBeSigned(new byte[] { 1, 2, 3 });

		RestAssured.given(this.spec).accept(ContentType.JSON).contentType(ContentType.JSON).body(tbs)
				.post("/services/rest/server-signing/sign/{alias}/{digest-algo}", "certificate", digestAlgorithm).then().assertThat().statusCode(equalTo(200));
	}

	@Test
	public void getDataToSignOneDocument() throws Exception {

		DSSPrivateKeyEntry dssPrivateKeyEntry = token.getKeys().get(0);	

		DataToSignOneDocumentDTO dataToSign = new DataToSignOneDocumentDTO();

		RemoteSignatureParameters parameters = new RemoteSignatureParameters();
		parameters.setSignatureLevel(SignatureLevel.CAdES_BASELINE_B);
		parameters.setSignaturePackaging(SignaturePackaging.ENVELOPING);
		parameters.setDigestAlgorithm(DigestAlgorithm.SHA256);
		parameters.setSigningCertificate(new RemoteCertificate(dssPrivateKeyEntry.getCertificate().getEncoded()));

		RemoteBLevelParameters bLevelParams = new RemoteBLevelParameters();
		bLevelParams.setSigningDate(signingDate);
		parameters.setBLevelParams(bLevelParams);
		dataToSign.setParameters(parameters);

		RemoteDocument toSignDocument = new RemoteDocument();
		toSignDocument.setBytes("Hello".getBytes("UTF-8"));
		dataToSign.setToSignDocument(toSignDocument);

		RestAssured.given(this.spec).accept(ContentType.JSON).contentType(ContentType.JSON).body(dataToSign, ObjectMapperType.JACKSON_2)
				.post("/services/rest/signature/one-document/getDataToSign").then().assertThat().statusCode(equalTo(200));

	}

	@Test
	public void signDocumentOneDocument() throws Exception {

		DSSPrivateKeyEntry dssPrivateKeyEntry = token.getKeys().get(0);

		SignOneDocumentDTO signOneDoc = new SignOneDocumentDTO();

		RemoteSignatureParameters parameters = new RemoteSignatureParameters();
		parameters.setSignatureLevel(SignatureLevel.CAdES_BASELINE_B);
		parameters.setSignaturePackaging(SignaturePackaging.ENVELOPING);
		parameters.setDigestAlgorithm(DigestAlgorithm.SHA256);
		parameters.setSigningCertificate(new RemoteCertificate(dssPrivateKeyEntry.getCertificate().getEncoded()));

		RemoteBLevelParameters bLevelParams = new RemoteBLevelParameters();
		bLevelParams.setSigningDate(signingDate);
		parameters.setBLevelParams(bLevelParams);
		signOneDoc.setParameters(parameters);

		RemoteDocument toSignDocument = new RemoteDocument();
		toSignDocument.setBytes("Hello".getBytes("UTF-8"));
		signOneDoc.setToSignDocument(toSignDocument);

		signOneDoc.setSignatureValue(new SignatureValueDTO(SignatureAlgorithm.RSA_SHA256, new byte[] { 1, 2, 3, 4 }));

		RestAssured.given(this.spec).accept(ContentType.JSON).contentType(ContentType.JSON).body(signOneDoc, ObjectMapperType.JACKSON_2)
				.post("/services/rest/signature/one-document/signDocument").then().assertThat().statusCode(equalTo(200));
	}

	@Test
	public void extendOneDocument() throws Exception {
		ExtendDocumentDTO extendOneDoc = new ExtendDocumentDTO();
		RemoteSignatureParameters parameters = new RemoteSignatureParameters();
		parameters.setSignatureLevel(SignatureLevel.XAdES_BASELINE_T);

		List<RemoteDocument> detachedContents = new ArrayList<RemoteDocument>();

		File detached = new File("src/test/resources/sample.xml");
		RemoteDocument detachedDoc = new RemoteDocument();
		detachedDoc.setBytes(toByteArray(detached));
		detachedDoc.setName(detached.getName());
		detachedContents.add(detachedDoc);

		parameters.setDetachedContents(detachedContents);
		extendOneDoc.setParameters(parameters);

		File signature = new File("src/test/resources/xades-detached.xml");

		RemoteDocument toExtendDocument = new RemoteDocument();
		toExtendDocument.setBytes(toByteArray(signature));
		toExtendDocument.setName(signature.getName());
		extendOneDoc.setToExtendDocument(toExtendDocument);

		RestAssured.given(this.spec).accept(ContentType.JSON).contentType(ContentType.JSON).body(extendOneDoc, ObjectMapperType.JACKSON_2)
				.post("/services/rest/signature/one-document/extendDocument").then().assertThat().statusCode(equalTo(200));
	}

	@Test
	public void getDataToSignMultiDocuments() throws Exception {

		DSSPrivateKeyEntry dssPrivateKeyEntry = token.getKeys().get(0);

		DataToSignMultipleDocumentsDTO dataToSignMultiDocs = new DataToSignMultipleDocumentsDTO();

		RemoteSignatureParameters parameters = new RemoteSignatureParameters();
		parameters.setAsicContainerType(ASiCContainerType.ASiC_E);
		parameters.setSignatureLevel(SignatureLevel.XAdES_BASELINE_B);
		parameters.setSignaturePackaging(SignaturePackaging.DETACHED);
		parameters.setDigestAlgorithm(DigestAlgorithm.SHA256);
		parameters.setSigningCertificate(new RemoteCertificate(dssPrivateKeyEntry.getCertificate().getEncoded()));

		RemoteBLevelParameters bLevelParams = new RemoteBLevelParameters();
		bLevelParams.setSigningDate(signingDate);
		parameters.setBLevelParams(bLevelParams);
		dataToSignMultiDocs.setParameters(parameters);

		List<RemoteDocument> toSignDocuments = new ArrayList<RemoteDocument>();
		RemoteDocument doc1 = new RemoteDocument();
		doc1.setBytes("Hello".getBytes("UTF-8"));
		doc1.setName("test1.bin");
		toSignDocuments.add(doc1);

		RemoteDocument doc2 = new RemoteDocument();
		doc2.setBytes("World".getBytes("UTF-8"));
		doc2.setName("test2.bin");
		toSignDocuments.add(doc2);
		dataToSignMultiDocs.setToSignDocuments(toSignDocuments);

		RestAssured.given(this.spec).accept(ContentType.JSON).contentType(ContentType.JSON).body(dataToSignMultiDocs, ObjectMapperType.JACKSON_2)
				.post("/services/rest/signature/multiple-documents/getDataToSignMultiple").then().assertThat().statusCode(equalTo(200));
	}

	@Test
	public void signDocumentMultiDocuments() throws Exception {

		DSSPrivateKeyEntry dssPrivateKeyEntry = token.getKeys().get(0);

		SignMultipleDocumentDTO signMultiDocsDto = new SignMultipleDocumentDTO();

		RemoteSignatureParameters parameters = new RemoteSignatureParameters();
		parameters.setAsicContainerType(ASiCContainerType.ASiC_E);
		parameters.setSignatureLevel(SignatureLevel.XAdES_BASELINE_B);
		parameters.setSignaturePackaging(SignaturePackaging.DETACHED);
		parameters.setDigestAlgorithm(DigestAlgorithm.SHA256);
		parameters.setSigningCertificate(new RemoteCertificate(dssPrivateKeyEntry.getCertificate().getEncoded()));

		RemoteBLevelParameters bLevelParams = new RemoteBLevelParameters();
		bLevelParams.setSigningDate(signingDate);
		parameters.setBLevelParams(bLevelParams);
		signMultiDocsDto.setParameters(parameters);

		List<RemoteDocument> toSignDocuments = new ArrayList<RemoteDocument>();
		RemoteDocument doc1 = new RemoteDocument();
		doc1.setBytes("Hello".getBytes("UTF-8"));
		doc1.setName("test1.bin");
		toSignDocuments.add(doc1);

		RemoteDocument doc2 = new RemoteDocument();
		doc2.setBytes("World".getBytes("UTF-8"));
		doc2.setName("test2.bin");
		toSignDocuments.add(doc2);
		signMultiDocsDto.setToSignDocuments(toSignDocuments);

		signMultiDocsDto.setSignatureValue(new SignatureValueDTO(SignatureAlgorithm.RSA_SHA256, new byte[] { 1, 2, 3, 4 }));

		RestAssured.given(this.spec).accept(ContentType.JSON).contentType(ContentType.JSON).body(signMultiDocsDto, ObjectMapperType.JACKSON_2)
				.post("/services/rest/signature/multiple-documents/signDocument").then().assertThat().statusCode(equalTo(200));
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

		RestAssured.given(this.spec).accept(ContentType.JSON).contentType(ContentType.JSON).body(dataToValidateDTO, ObjectMapperType.JACKSON_2)
				.post("/services/rest/validation/validateSignature").then().assertThat().statusCode(equalTo(200));

	}

	@Test
	public void validateCert() throws IOException {

		CertificateToValidateDTO dataToValidateDTO = new CertificateToValidateDTO();

		dataToValidateDTO.setCertificate(new RemoteCertificate(Base64.getDecoder().decode(
				"MIIC6jCCAdKgAwIBAgIGLtYU17tXMA0GCSqGSIb3DQEBCwUAMDAxGzAZBgNVBAMMElJvb3RTZWxmU2lnbmVkRmFrZTERMA8GA1UECgwIRFNTLXRlc3QwHhcNMTcwNjA4MTEyNjAxWhcNNDcwNzA0MDc1NzI0WjAoMRMwEQYDVQQDDApTaWduZXJGYWtlMREwDwYDVQQKDAhEU1MtdGVzdDCCASIwDQYJKoZIhvcNAQEBBQADggEPADCCAQoCggEBAMI3kZhtnipn+iiZHZ9ax8FlfE5Ow/cFwBTfAEb3R1ZQUp6/BQnBt7Oo0JWBtc9qkv7JUDdcBJXPV5QWS5AyMPHpqQ75Hitjsq/Fzu8eHtkKpFizcxGa9BZdkQjh4rSrtO1Kjs0Rd5DQtWSgkeVCCN09kN0ZsZ0ENY+Ip8QxSmyztsStkYXdULqpwz4JEXW9vz64eTbde4vQJ6pjHGarJf1gQNEc2XzhmI/prXLysWNqC7lZg7PUZUTrdegABTUzYCRJ1kWBRPm4qo0LN405c94QQd45a5kTgowHzEgLnAQI28x0M3A59TKC+ieNc6VF1PsTLpUw7PNI2VstX5jAuasCAwEAAaMSMBAwDgYDVR0PAQH/BAQDAgEGMA0GCSqGSIb3DQEBCwUAA4IBAQCK6LGA01TR+rmU8p6yhAi4OkDN2b1dbIL8l8iCMYopLCxx8xqq3ubZCOxqh1X2j6pgWzarb0b/MUix00IoUvNbFOxAW7PBZIKDLnm6LsckRxs1U32sC9d1LOHe3WKBNB6GZALT1ewjh7hSbWjftlmcovq+6eVGA5cvf2u/2+TkKkyHV/NR394nXrdsdpvygwypEtXjetzD7UT93Nuw3xcV8VIftIvHf9LjU7h+UjGmKXG9c15eYr3SzUmv6kyOI0Bvw14PWtsWGl0QdOSRvIBBrP4adCnGTgjgjk9LTcO8B8FKrr+8lHGuc0bp4lIUToiUkGILXsiEeEg9WAqm+XqO")));

		dataToValidateDTO.setCertificateChain(Arrays.asList(new RemoteCertificate(Base64.getDecoder().decode(
				"MIIC6jCCAdKgAwIBAgIGLtYU17tXMA0GCSqGSIb3DQEBCwUAMDAxGzAZBgNVBAMMElJvb3RTZWxmU2lnbmVkRmFrZTERMA8GA1UECgwIRFNTLXRlc3QwHhcNMTcwNjA4MTEyNjAxWhcNNDcwNzA0MDc1NzI0WjAoMRMwEQYDVQQDDApTaWduZXJGYWtlMREwDwYDVQQKDAhEU1MtdGVzdDCCASIwDQYJKoZIhvcNAQEBBQADggEPADCCAQoCggEBAMI3kZhtnipn+iiZHZ9ax8FlfE5Ow/cFwBTfAEb3R1ZQUp6/BQnBt7Oo0JWBtc9qkv7JUDdcBJXPV5QWS5AyMPHpqQ75Hitjsq/Fzu8eHtkKpFizcxGa9BZdkQjh4rSrtO1Kjs0Rd5DQtWSgkeVCCN09kN0ZsZ0ENY+Ip8QxSmyztsStkYXdULqpwz4JEXW9vz64eTbde4vQJ6pjHGarJf1gQNEc2XzhmI/prXLysWNqC7lZg7PUZUTrdegABTUzYCRJ1kWBRPm4qo0LN405c94QQd45a5kTgowHzEgLnAQI28x0M3A59TKC+ieNc6VF1PsTLpUw7PNI2VstX5jAuasCAwEAAaMSMBAwDgYDVR0PAQH/BAQDAgEGMA0GCSqGSIb3DQEBCwUAA4IBAQCK6LGA01TR+rmU8p6yhAi4OkDN2b1dbIL8l8iCMYopLCxx8xqq3ubZCOxqh1X2j6pgWzarb0b/MUix00IoUvNbFOxAW7PBZIKDLnm6LsckRxs1U32sC9d1LOHe3WKBNB6GZALT1ewjh7hSbWjftlmcovq+6eVGA5cvf2u/2+TkKkyHV/NR394nXrdsdpvygwypEtXjetzD7UT93Nuw3xcV8VIftIvHf9LjU7h+UjGmKXG9c15eYr3SzUmv6kyOI0Bvw14PWtsWGl0QdOSRvIBBrP4adCnGTgjgjk9LTcO8B8FKrr+8lHGuc0bp4lIUToiUkGILXsiEeEg9WAqm+XqO"))));

		RestAssured.given(this.spec).accept(ContentType.JSON).contentType(ContentType.JSON).body(dataToValidateDTO, ObjectMapperType.JACKSON_2)
				.post("/services/rest/certificate-validation/validateCertificate").then().assertThat().statusCode(equalTo(200));

	}

	@Test
	public void getDataToSignDigestDocument() throws Exception {

		DSSPrivateKeyEntry dssPrivateKeyEntry = token.getKeys().get(0);

		DataToSignOneDocumentDTO dataToSign = new DataToSignOneDocumentDTO();

		RemoteSignatureParameters parameters = new RemoteSignatureParameters();
		parameters.setSignatureLevel(SignatureLevel.CAdES_BASELINE_B);
		parameters.setSignaturePackaging(SignaturePackaging.DETACHED);
		parameters.setDigestAlgorithm(DigestAlgorithm.SHA256);
		parameters.setSigningCertificate(new RemoteCertificate(dssPrivateKeyEntry.getCertificate().getEncoded()));

		RemoteBLevelParameters bLevelParams = new RemoteBLevelParameters();
		bLevelParams.setSigningDate(signingDate);
		parameters.setBLevelParams(bLevelParams);
		dataToSign.setParameters(parameters);

		FileDocument doc = new FileDocument("src/test/resources/sample.xml");

		RemoteDocument toSignDocument = new RemoteDocument();
		toSignDocument.setDigestAlgorithm(DigestAlgorithm.SHA256);
		toSignDocument.setBytes(DSSUtils.digest(DigestAlgorithm.SHA256, doc));
		dataToSign.setToSignDocument(toSignDocument);

		RestAssured.given(this.spec).accept(ContentType.JSON).contentType(ContentType.JSON).body(dataToSign, ObjectMapperType.JACKSON_2)
				.post("/services/rest/signature/one-document/getDataToSign").then().assertThat().statusCode(equalTo(200));

	}

	@Test
	public void signDocumentDigestDocument() throws Exception {

		DSSPrivateKeyEntry dssPrivateKeyEntry = token.getKeys().get(0);

		SignOneDocumentDTO signOneDoc = new SignOneDocumentDTO();

		RemoteSignatureParameters parameters = new RemoteSignatureParameters();
		parameters.setSignatureLevel(SignatureLevel.CAdES_BASELINE_B);
		parameters.setSignaturePackaging(SignaturePackaging.DETACHED);
		parameters.setDigestAlgorithm(DigestAlgorithm.SHA256);
		parameters.setSigningCertificate(new RemoteCertificate(dssPrivateKeyEntry.getCertificate().getEncoded()));

		RemoteBLevelParameters bLevelParams = new RemoteBLevelParameters();
		bLevelParams.setSigningDate(signingDate);
		parameters.setBLevelParams(bLevelParams);
		signOneDoc.setParameters(parameters);

		FileDocument doc = new FileDocument("src/test/resources/sample.xml");

		RemoteDocument toSignDocument = new RemoteDocument();
		toSignDocument.setDigestAlgorithm(DigestAlgorithm.SHA256);
		toSignDocument.setBytes(DSSUtils.digest(DigestAlgorithm.SHA256, doc));
		signOneDoc.setToSignDocument(toSignDocument);

		signOneDoc.setSignatureValue(new SignatureValueDTO(SignatureAlgorithm.RSA_SHA256, new byte[] { 1, 2, 3, 4 }));

		RestAssured.given(this.spec).accept(ContentType.JSON).contentType(ContentType.JSON).body(signOneDoc, ObjectMapperType.JACKSON_2)
				.post("/services/rest/signature/one-document/signDocument").then().assertThat().statusCode(equalTo(200));
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

		RestAssured.given(this.spec).accept(ContentType.JSON).contentType(ContentType.JSON).body(dataToValidateDTO, ObjectMapperType.JACKSON_2)
				.post("/services/rest/validation/validateSignature").then().assertThat().statusCode(equalTo(200));

	}

	@Test
	public void getOriginalDocuments() throws IOException {

		DataToValidateDTO dataToValidateDTO = new DataToValidateDTO();

		File signature = new File("src/test/resources/hello-signed-xades.xml");
		RemoteDocument signedDoc = new RemoteDocument();
		signedDoc.setBytes(toByteArray(signature));
		signedDoc.setName(signature.getName());
		dataToValidateDTO.setSignedDocument(signedDoc);

		dataToValidateDTO.setSignatureId("id-ea10a0517cbc7f549ea3e685867ac95e");

		dataToValidateDTO.setSignedDocument(signedDoc);

		RestAssured.given(this.spec).accept(ContentType.JSON).contentType(ContentType.JSON).body(dataToValidateDTO, ObjectMapperType.JACKSON_2)
				.post("/services/rest/validation/getOriginalDocuments").then().assertThat().statusCode(equalTo(200));

	}

	private byte[] toByteArray(File file) throws IOException {
		return Files.readAllBytes(file.toPath());
	}

}
