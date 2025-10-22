package eu.europa.esig.dss.validation;

import eu.europa.esig.dss.enumerations.MimeType;
import eu.europa.esig.dss.spi.exception.IllegalInputException;
import eu.europa.esig.dss.jaxb.object.Message;
import eu.europa.esig.dss.model.DSSDocument;
import eu.europa.esig.dss.model.FileDocument;
import eu.europa.esig.dss.model.InMemoryDocument;
import eu.europa.esig.dss.policy.EtsiValidationPolicy;
import eu.europa.esig.dss.model.policy.ValidationPolicy;
import eu.europa.esig.dss.policy.ValidationPolicyFacade;
import eu.europa.esig.dss.policy.jaxb.ConstraintsParameters;
import eu.europa.esig.dss.service.crl.OnlineCRLSource;
import eu.europa.esig.dss.service.http.commons.CommonsDataLoader;
import eu.europa.esig.dss.service.http.commons.FileCacheDataLoader;
import eu.europa.esig.dss.service.ocsp.OnlineOCSPSource;
import eu.europa.esig.dss.simplereport.SimpleReport;
import eu.europa.esig.dss.spi.DSSUtils;
import eu.europa.esig.dss.spi.client.http.DataLoader;
import eu.europa.esig.dss.spi.tsl.TrustedListsCertificateSource;
import eu.europa.esig.dss.spi.validation.CertificateVerifier;
import eu.europa.esig.dss.spi.validation.CommonCertificateVerifier;
import eu.europa.esig.dss.spi.x509.KeyStoreCertificateSource;
import eu.europa.esig.dss.spi.x509.aia.DefaultAIASource;
import eu.europa.esig.dss.tsl.job.TLValidationJob;
import eu.europa.esig.dss.tsl.source.LOTLSource;
import eu.europa.esig.dss.tsl.sync.AcceptAllStrategy;
import eu.europa.esig.dss.utils.Utils;
import eu.europa.esig.dss.validation.reports.Reports;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static org.junit.jupiter.api.Assertions.fail;

public class EsigValidationTest {

    private static final String URL_ACCESS_POINT = "https://eidas.ec.europa.eu/efda/api/v2/validation-tests/testcase/testFile/all";

    private static final String ZIP_ARCHIVE_EXTENSION = ".zip";

    private static final String KEY_STORE_FILE_NAME = "CERT-%s.p12";
    private static final String KEY_STORE_PASSWORD_FILE_NAME = "CERT-%s-PASSWORD.txt";

    private static final String KEY_STORE_TYPE = "PKCS12";

    private static final String LOTL_URL_SUFFIX = "LOTL URL.txt";
    private static final String TEST_FILE_SUFFIX = "TEST FILE.xml";
    private static final String CONCLUSION_SUFFIX = "CONCLUSION.txt";

    private static final String POLICY_URL = "src/test/resources/constraint.xml";

    private static final String OUTPUT_FILENAME = "target/validationTestsResult.csv";

    private static final File fileCacheDirectory = new File("target/cache");

    private static DataLoader dataLoader;

    private static ValidationPolicy validationPolicy;

    private static StringBuilder sb;

    @BeforeAll
    public static void init() throws Exception {
        ValidationPolicyFacade policyFacade = ValidationPolicyFacade.newFacade();
        ConstraintsParameters constraints = policyFacade.unmarshall(new File(POLICY_URL));
        validationPolicy = new EtsiValidationPolicy(constraints);
        dataLoader = new CommonsDataLoader();

        sb = new StringBuilder();

        sb.append("Test name,");
        sb.append("Expected Result,");
        sb.append("Obtained Result,");
        sb.append("Match,");
        sb.append("Indication,");
        sb.append("SubIndication,");
        sb.append("AdES Errors,");
        sb.append("AdES Warnings,");
        sb.append("Qualifications Errors,");
        sb.append("Qualifications Warnings");
        sb.append('\n');
    }

    private static Stream<Arguments> data() {
        DSSDocument testArchive = getTestArchive();
        List<DSSDocument> allArchiveContent = extractContainerContent(testArchive);

        Collection<Arguments> dataToRun = new ArrayList<>();

        for (DSSDocument testPackage : allArchiveContent) {
            List<DSSDocument> zipArchiveContent = extractContainerContent(testPackage);
            Map<DSSDocument, String> documentsAndResults = getDocumentsToValidateAndExpectedResultsMap(zipArchiveContent);

            String testKey = getTestKey(testPackage);
            KeyStoreCertificateSource keyStore = getKeyStore(zipArchiveContent, testKey);

            TrustedListsCertificateSource trustedCertSource = new TrustedListsCertificateSource();

            TLValidationJob tlValidationJob = new TLValidationJob();
            tlValidationJob.setTrustedListCertificateSource(trustedCertSource);
            tlValidationJob.setSynchronizationStrategy(new AcceptAllStrategy());

            LOTLSource lotlSource = new LOTLSource();
            lotlSource.setUrl(getLotlUrl(zipArchiveContent));
            lotlSource.setCertificateSource(keyStore);
            tlValidationJob.setListOfTrustedListSources(lotlSource);

            FileCacheDataLoader fileCacheDataLoader = new FileCacheDataLoader();
            fileCacheDataLoader.setDataLoader(dataLoader);
            fileCacheDataLoader.setCacheExpirationTime(-1);
            fileCacheDataLoader.setFileCacheDirectory(fileCacheDirectory);

            tlValidationJob.setOnlineDataLoader(fileCacheDataLoader);

            tlValidationJob.onlineRefresh();

            CertificateVerifier certificateVerifier = new CommonCertificateVerifier();
            certificateVerifier.setTrustedCertSources(trustedCertSource);
            certificateVerifier.setAIASource(new DefaultAIASource(fileCacheDataLoader));
            certificateVerifier.setCrlSource(new OnlineCRLSource(fileCacheDataLoader));
            certificateVerifier.setOcspSource(new OnlineOCSPSource(fileCacheDataLoader));

            for (Map.Entry<DSSDocument, String> entry : documentsAndResults.entrySet()) {
                dataToRun.add(Arguments.of(entry.getKey(), entry.getValue(), certificateVerifier));
            }
        }

        return dataToRun.stream();
    }

    private static DSSDocument getTestArchive() {
        // -DeSig.validation.tests.bundle.path=...
        String eSigValidationTestsBundlePath = System.getProperty("eSig.validation.tests.bundle.path", null);
        if (Utils.isStringNotEmpty(eSigValidationTestsBundlePath)) {
            return new FileDocument(eSigValidationTestsBundlePath);
        }

        // -DeSig.validation.tests.url=...
        String eSigValidationTestsUrl = System.getProperty("eSig.validation.tests.url", URL_ACCESS_POINT);

        byte[] zipArchiveBinary = dataLoader.get(eSigValidationTestsUrl);
        return new InMemoryDocument(zipArchiveBinary);
    }

    private static List<DSSDocument> extractContainerContent(DSSDocument archive) {
        List<DSSDocument> extractedContent = new ArrayList<>();
        try (InputStream is = archive.openStream(); ZipInputStream zis = new ZipInputStream(is)) {
            ZipEntry zipEntry;
            while ((zipEntry = zis.getNextEntry()) != null) {
                byte[] data = readZipEntry(zis);
                DSSDocument currentDocument = new InMemoryDocument(data);
                currentDocument.setName(zipEntry.getName());
                currentDocument.setMimeType(MimeType.fromFileName(zipEntry.getName()));
                extractedContent.add(currentDocument);
            }
        } catch (IOException e) {
            throw new IllegalInputException("Unable to extract content from zip archive", e);
        }
        return extractedContent;
    }

    private static byte[] readZipEntry(ZipInputStream zis) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            byte[] data = new byte[2048];
            int nRead;
            while ((nRead = zis.read(data)) != -1) {
                baos.write(data, 0, nRead);
            }
            baos.flush();
            return baos.toByteArray();
        } catch (IOException e) {
            throw new IllegalInputException("Unable to read a zip entry", e);
        }
    }

    private static String getTestKey(DSSDocument testPackage) {
        if (testPackage.getName() != null && testPackage.getName().endsWith(ZIP_ARCHIVE_EXTENSION)) {
            return testPackage.getName().replace(ZIP_ARCHIVE_EXTENSION, "");
        }
        fail(String.format("Unsupported filename [%s]", testPackage.getName()));
        return null;
    }

    private static KeyStoreCertificateSource getKeyStore(List<DSSDocument> documents, String testKey) {
        DSSDocument keyStore = getKeyStoreDocument(documents, testKey);
        char[] keyStorePassword = getKeyStorePassword(documents, testKey);
        if (keyStore != null && keyStorePassword != null) {
            try (InputStream is = keyStore.openStream()) {
                return new KeyStoreCertificateSource(is, KEY_STORE_TYPE, keyStorePassword);
            } catch (IOException e) {
                fail(e);
            }
        }
        return null;
    }

    private static DSSDocument getKeyStoreDocument(List<DSSDocument> documents, String testKey) {
        for (DSSDocument document : documents) {
            if (String.format(KEY_STORE_FILE_NAME, testKey).equals(document.getName())) {
                return document;
            }
        }
        fail("Unable to find the key store!");
        return null;
    }

    private static char[] getKeyStorePassword(List<DSSDocument> documents, String testKey) {
        for (DSSDocument document : documents) {
            if (String.format(KEY_STORE_PASSWORD_FILE_NAME, testKey).equals(document.getName())) {
                return readToCharArray(document);
            }
        }
        fail("Unable to find the key store password!");
        return null;
    }

    public static char[] readToCharArray(DSSDocument document) {
        try (InputStream is = document.openStream(); InputStreamReader reader = new InputStreamReader(is)) {
            char[] buffer = new char[1024];

            int totalCharsRead = 0;
            int charsRead;
            while ((charsRead = reader.read(buffer, totalCharsRead, buffer.length - totalCharsRead)) != -1) {
                totalCharsRead += charsRead;
                if (totalCharsRead == buffer.length) {
                    char[] newBuffer = new char[buffer.length * 2];
                    System.arraycopy(buffer, 0, newBuffer, 0, buffer.length);
                    buffer = newBuffer;
                }
            }
            reader.close();

            char[] content = new char[totalCharsRead];
            System.arraycopy(buffer, 0, content, 0, totalCharsRead);
            return content;
            
        } catch (IOException e) {
            fail(String.format("An error occurred while reading document with name [%s] : %s",
                    document.getName(), e.getMessage()));
            return null;
        }
    }

    private static String getLotlUrl(List<DSSDocument> documents) {
        for (DSSDocument document : documents) {
            if (document.getName().endsWith(LOTL_URL_SUFFIX)) {
                return new String(DSSUtils.toByteArray(document));
            }
        }
        fail("Unable to find the LOTL URL!");
        return null;
    }

    private static Map<DSSDocument, String> getDocumentsToValidateAndExpectedResultsMap(List<DSSDocument> documents) {
        Map<DSSDocument, String> documentsMap = new LinkedHashMap<>();
        for (DSSDocument document : documents) {
            if (document.getName().endsWith(TEST_FILE_SUFFIX)) {
                String testFileId = StringUtils.substringBefore(document.getName(), "-" + TEST_FILE_SUFFIX);
                String conclusion = getConclusionForTestFileWithId(documents, testFileId);
                documentsMap.put(document, conclusion);
            }
        }
        return documentsMap;
    }

    private static String getConclusionForTestFileWithId(List<DSSDocument> documents, String testFileId) {
        for (DSSDocument document : documents) {
            if (document.getName().equals(testFileId + "-" + CONCLUSION_SUFFIX)) {
                return new String(DSSUtils.toByteArray(document));
            }
        }
        fail("Unable to find the relative conclusion for the test case with id '" + testFileId + "'!");
        return null;
    }

    @ParameterizedTest(name = "Validation {index} : {0}")
    @MethodSource("data")
    public void test(DSSDocument document, String expectedResult, CertificateVerifier certificateVerifier) {
        SignedDocumentValidator validator = SignedDocumentValidator.fromDocument(document);
        validator.setCertificateVerifier(certificateVerifier);

        Reports reports = validator.validateDocument(validationPolicy);
        // reports.print();

        SimpleReport simpleReport = reports.getSimpleReport();
        String obtainedResult = simpleReport.getSignatureQualification(simpleReport.getFirstSignatureId()).getReadable();

        sb.append(document.getName()).append(",");
        sb.append(expectedResult).append(",");
        sb.append(obtainedResult).append(",");
        sb.append(expectedResult.equals(obtainedResult)).append(",");
        sb.append(simpleReport.getIndication(simpleReport.getFirstSignatureId())).append(",");
        sb.append((simpleReport.getSubIndication(simpleReport.getFirstSignatureId()) != null ?
                simpleReport.getSubIndication(simpleReport.getFirstSignatureId()) : "-")).append(",");
        sb.append(toString(simpleReport.getAdESValidationErrors(simpleReport.getFirstSignatureId()))).append(",");
        sb.append(toString(simpleReport.getAdESValidationWarnings(simpleReport.getFirstSignatureId()))).append(",");
        sb.append(toString(simpleReport.getQualificationErrors(simpleReport.getFirstSignatureId()))).append(",");
        sb.append(toString(simpleReport.getQualificationWarnings(simpleReport.getFirstSignatureId())));
        sb.append('\n');

        // TODO: add equivalence map between eSig validation test cases and DSS results
        //assertEquals(expectedResult, obtainedResult);
    }

    private String toString(List<Message> messages) {
        if (Utils.isCollectionEmpty(messages)) {
            return "-";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("[");
        Iterator<Message> iterator = messages.iterator();
        while (iterator.hasNext()) {
            String value = iterator.next().getValue().replaceAll(", ", ";"); // avoid moving to a new line
            sb.append(value);
            if (iterator.hasNext()) {
                sb.append("; ");
            }
        }
        sb.append("]");

        return sb.toString();
    }

    @AfterAll
    public static void storeResult() throws Exception {
        try (FileOutputStream fos = new FileOutputStream(OUTPUT_FILENAME);
                PrintWriter writer = new PrintWriter(fos)) {
            writer.write(sb.toString());
        }
    }

}
