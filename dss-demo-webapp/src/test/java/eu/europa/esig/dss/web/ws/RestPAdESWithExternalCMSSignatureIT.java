package eu.europa.esig.dss.web.ws;

import eu.europa.esig.dss.enumerations.DigestAlgorithm;
import eu.europa.esig.dss.enumerations.SignatureLevel;
import eu.europa.esig.dss.model.FileDocument;
import eu.europa.esig.dss.model.InMemoryDocument;
import eu.europa.esig.dss.model.SignatureValue;
import eu.europa.esig.dss.token.DSSPrivateKeyEntry;
import eu.europa.esig.dss.token.Pkcs12SignatureToken;
import eu.europa.esig.dss.utils.Utils;
import eu.europa.esig.dss.web.config.CXFConfig;
import eu.europa.esig.dss.ws.converter.DTOConverter;
import eu.europa.esig.dss.ws.converter.RemoteCertificateConverter;
import eu.europa.esig.dss.ws.dto.DigestDTO;
import eu.europa.esig.dss.ws.dto.RemoteDocument;
import eu.europa.esig.dss.ws.dto.SignatureValueDTO;
import eu.europa.esig.dss.ws.dto.ToBeSignedDTO;
import eu.europa.esig.dss.ws.signature.dto.DataToSignExternalCmsDTO;
import eu.europa.esig.dss.ws.signature.dto.PDFExternalMessageDigestDTO;
import eu.europa.esig.dss.ws.signature.dto.PDFExternalSignDocumentDTO;
import eu.europa.esig.dss.ws.signature.dto.SignMessageDigestExternalCmsDTO;
import eu.europa.esig.dss.ws.signature.dto.parameters.RemoteSignatureParameters;
import eu.europa.esig.dss.ws.signature.rest.client.RestExternalCMSService;
import eu.europa.esig.dss.ws.signature.rest.client.RestPAdESWithExternalCMSService;
import org.apache.cxf.ext.logging.LoggingInInterceptor;
import org.apache.cxf.ext.logging.LoggingOutInterceptor;
import org.apache.cxf.jaxrs.client.JAXRSClientFactoryBean;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileInputStream;
import java.security.KeyStore;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class RestPAdESWithExternalCMSSignatureIT extends AbstractRestIT {

    private RestPAdESWithExternalCMSService padesWithExternalCmsService;
    private RestExternalCMSService externalCmsService;

    @BeforeEach
    public void init() {
        JAXRSClientFactoryBean factory = new JAXRSClientFactoryBean();

        factory.setAddress(getBaseCxf() + CXFConfig.REST_SIGNATURE_PAdES_WITH_EXTERNAL_CMS);
        factory.setServiceClass(RestPAdESWithExternalCMSService.class);
        factory.setProviders(Arrays.asList(jacksonJsonProvider()));

        LoggingInInterceptor loggingInInterceptor = new LoggingInInterceptor();
        factory.getInInterceptors().add(loggingInInterceptor);
        factory.getInFaultInterceptors().add(loggingInInterceptor);

        LoggingOutInterceptor loggingOutInterceptor = new LoggingOutInterceptor();
        factory.getOutInterceptors().add(loggingOutInterceptor);
        factory.getOutFaultInterceptors().add(loggingOutInterceptor);

        padesWithExternalCmsService = factory.create(RestPAdESWithExternalCMSService.class);

        factory = new JAXRSClientFactoryBean();

        factory.setAddress(getBaseCxf() + CXFConfig.REST_SIGNATURE_EXTERNAL_CMS);
        factory.setServiceClass(RestExternalCMSService.class);
        factory.setProviders(Arrays.asList(jacksonJsonProvider()));

        factory.getInInterceptors().add(loggingInInterceptor);
        factory.getInFaultInterceptors().add(loggingInInterceptor);

        factory.getOutInterceptors().add(loggingOutInterceptor);
        factory.getOutFaultInterceptors().add(loggingOutInterceptor);

        externalCmsService = factory.create(RestExternalCMSService.class);
    }

    @Test
    public void testBLevelSign() throws Exception {
        try (Pkcs12SignatureToken token = new Pkcs12SignatureToken(new FileInputStream("src/test/resources/user_a_rsa.p12"),
                new KeyStore.PasswordProtection("password".toCharArray()))) {

            List<DSSPrivateKeyEntry> keys = token.getKeys();
            DSSPrivateKeyEntry dssPrivateKeyEntry = keys.get(0);

            RemoteSignatureParameters padesParameters = new RemoteSignatureParameters();
            padesParameters.setSignatureLevel(SignatureLevel.PAdES_BASELINE_B);

            FileDocument fileToSign = new FileDocument(new File("src/test/resources/sample.pdf"));
            RemoteDocument toSignDocument = new RemoteDocument(Utils.toByteArray(fileToSign.openStream()), fileToSign.getName());
            DigestDTO messageDigest = padesWithExternalCmsService.getMessageDigest(
                    new PDFExternalMessageDigestDTO(toSignDocument, padesParameters));

            RemoteSignatureParameters cmsParameters = new RemoteSignatureParameters();
            cmsParameters.setSignatureLevel(SignatureLevel.PAdES_BASELINE_B);
            cmsParameters.setSigningCertificate(RemoteCertificateConverter.toRemoteCertificate(dssPrivateKeyEntry.getCertificate()));

            ToBeSignedDTO dataToSign = externalCmsService.getDataToSign(
                    new DataToSignExternalCmsDTO(messageDigest, cmsParameters));
            SignatureValue signatureValue = token.sign(
                    DTOConverter.toToBeSigned(dataToSign), DigestAlgorithm.SHA256, dssPrivateKeyEntry);
            SignatureValueDTO signatureValueDTO = DTOConverter.toSignatureValueDTO(signatureValue);
            RemoteDocument cmsSignature = externalCmsService.signMessageDigest(
                    new SignMessageDigestExternalCmsDTO(messageDigest, cmsParameters, signatureValueDTO));
            assertNotNull(cmsSignature);

            RemoteDocument signedDocument = padesWithExternalCmsService.signDocument(
                    new PDFExternalSignDocumentDTO(toSignDocument, padesParameters, cmsSignature));
            assertNotNull(signedDocument);

            InMemoryDocument iMD = new InMemoryDocument(signedDocument.getBytes());
            assertNotNull(iMD);
        }
    }

}
