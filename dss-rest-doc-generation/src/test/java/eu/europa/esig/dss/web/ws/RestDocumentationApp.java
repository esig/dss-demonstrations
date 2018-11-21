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
import java.util.Date;
import java.util.List;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.restdocs.JUnitRestDocumentation;
import org.springframework.restdocs.restassured3.RestDocumentationFilter;

import eu.europa.esig.dss.ASiCContainerType;
import eu.europa.esig.dss.BLevelParameters;
import eu.europa.esig.dss.DSSUtils;
import eu.europa.esig.dss.DataToValidateDTO;
import eu.europa.esig.dss.DigestAlgorithm;
import eu.europa.esig.dss.FileDocument;
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
import eu.europa.esig.dss.signature.SignMultipleDocumentDTO;
import eu.europa.esig.dss.signature.SignOneDocumentDTO;
import eu.europa.esig.dss.token.DSSPrivateKeyEntry;
import eu.europa.esig.dss.token.Pkcs12SignatureToken;
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

		BLevelParameters bLevelParams = new BLevelParameters();
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

		BLevelParameters bLevelParams = new BLevelParameters();
		bLevelParams.setSigningDate(signingDate);
		parameters.setBLevelParams(bLevelParams);
		signOneDoc.setParameters(parameters);

		RemoteDocument toSignDocument = new RemoteDocument();
		toSignDocument.setBytes("Hello".getBytes("UTF-8"));
		signOneDoc.setToSignDocument(toSignDocument);

		signOneDoc.setSignatureValue(new SignatureValue(SignatureAlgorithm.RSA_SHA256, new byte[] { 1, 2, 3, 4 }));

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
		detachedDoc.setMimeType(MimeType.XML);
		detachedDoc.setName(detached.getName());
		detachedContents.add(detachedDoc);

		parameters.setDetachedContents(detachedContents);
		extendOneDoc.setParameters(parameters);

		File signature = new File("src/test/resources/xades-detached.xml");

		RemoteDocument toExtendDocument = new RemoteDocument();
		toExtendDocument.setBytes(toByteArray(signature));
		toExtendDocument.setMimeType(MimeType.XML);
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

		BLevelParameters bLevelParams = new BLevelParameters();
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

		BLevelParameters bLevelParams = new BLevelParameters();
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

		signMultiDocsDto.setSignatureValue(new SignatureValue(SignatureAlgorithm.RSA_SHA256, new byte[] { 1, 2, 3, 4 }));

		RestAssured.given(this.spec).accept(ContentType.JSON).contentType(ContentType.JSON).body(signMultiDocsDto, ObjectMapperType.JACKSON_2)
				.post("/services/rest/signature/multiple-documents/signDocument").then().assertThat().statusCode(equalTo(200));
	}

	@Test
	public void validateDoc() throws IOException {

		DataToValidateDTO dataToValidateDTO = new DataToValidateDTO();

		File signature = new File("src/test/resources/xades-detached.xml");
		RemoteDocument signedDoc = new RemoteDocument();
		signedDoc.setBytes(toByteArray(signature));
		signedDoc.setMimeType(MimeType.XML);
		signedDoc.setName(signature.getName());
		dataToValidateDTO.setSignedDocument(signedDoc);

		File detached = new File("src/test/resources/sample.xml");
		RemoteDocument originalDoc = new RemoteDocument();
		originalDoc.setBytes(toByteArray(detached));
		originalDoc.setMimeType(MimeType.XML);
		originalDoc.setName(detached.getName());

		dataToValidateDTO.setOriginalDocuments(Arrays.asList(originalDoc));

		RestAssured.given(this.spec).accept(ContentType.JSON).contentType(ContentType.JSON).body(dataToValidateDTO, ObjectMapperType.JACKSON_2)
				.post("/services/rest/validation/validateSignature").then().assertThat().statusCode(equalTo(200));

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

		BLevelParameters bLevelParams = new BLevelParameters();
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

		BLevelParameters bLevelParams = new BLevelParameters();
		bLevelParams.setSigningDate(signingDate);
		parameters.setBLevelParams(bLevelParams);
		signOneDoc.setParameters(parameters);

		FileDocument doc = new FileDocument("src/test/resources/sample.xml");

		RemoteDocument toSignDocument = new RemoteDocument();
		toSignDocument.setDigestAlgorithm(DigestAlgorithm.SHA256);
		toSignDocument.setBytes(DSSUtils.digest(DigestAlgorithm.SHA256, doc));
		signOneDoc.setToSignDocument(toSignDocument);

		signOneDoc.setSignatureValue(new SignatureValue(SignatureAlgorithm.RSA_SHA256, new byte[] { 1, 2, 3, 4 }));

		RestAssured.given(this.spec).accept(ContentType.JSON).contentType(ContentType.JSON).body(signOneDoc, ObjectMapperType.JACKSON_2)
				.post("/services/rest/signature/one-document/signDocument").then().assertThat().statusCode(equalTo(200));
	}

	@Test
	public void validateDigestDoc() throws IOException {

		DataToValidateDTO dataToValidateDTO = new DataToValidateDTO();

		File signature = new File("src/test/resources/xades-detached.xml");
		RemoteDocument signedDoc = new RemoteDocument();
		signedDoc.setBytes(toByteArray(signature));
		signedDoc.setMimeType(MimeType.XML);
		signedDoc.setName(signature.getName());
		dataToValidateDTO.setSignedDocument(signedDoc);

		FileDocument detached = new FileDocument("src/test/resources/sample.xml");
		RemoteDocument originalDoc = new RemoteDocument();
		originalDoc.setDigestAlgorithm(DigestAlgorithm.SHA256);
		originalDoc.setBytes(DSSUtils.digest(DigestAlgorithm.SHA256, detached));
		originalDoc.setMimeType(MimeType.XML);
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
		signedDoc.setMimeType(MimeType.XML);
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
