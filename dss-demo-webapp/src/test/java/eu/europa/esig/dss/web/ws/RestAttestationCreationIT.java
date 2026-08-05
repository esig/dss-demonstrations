package eu.europa.esig.dss.web.ws;

import eu.europa.esig.dss.attestation.mdoc.MdocConstants;
import eu.europa.esig.dss.enumerations.AttestationForm;
import eu.europa.esig.dss.enumerations.DigestAlgorithm;
import eu.europa.esig.dss.model.InMemoryDocument;
import eu.europa.esig.dss.model.SignatureValue;
import eu.europa.esig.dss.spi.DSSUtils;
import eu.europa.esig.dss.token.DSSPrivateKeyEntry;
import eu.europa.esig.dss.token.Pkcs12SignatureToken;
import eu.europa.esig.dss.utils.Utils;
import eu.europa.esig.dss.web.config.CXFConfig;
import eu.europa.esig.dss.ws.attestation.creation.dto.CreateKeyBindingSignatureDTO;
import eu.europa.esig.dss.ws.attestation.creation.dto.DataToSignAttestationDTO;
import eu.europa.esig.dss.ws.attestation.creation.dto.DataToSignForKeyBindingSignatureDTO;
import eu.europa.esig.dss.ws.attestation.creation.dto.DisclosuresDTO;
import eu.europa.esig.dss.ws.attestation.creation.dto.IssueAttestationDTO;
import eu.europa.esig.dss.ws.attestation.creation.dto.IssuePresentationDTO;
import eu.europa.esig.dss.ws.attestation.creation.dto.ParseAttestationDTO;
import eu.europa.esig.dss.ws.attestation.creation.dto.SignAttestationDTO;
import eu.europa.esig.dss.ws.attestation.creation.dto.parameters.ClaimDTO;
import eu.europa.esig.dss.ws.attestation.creation.dto.parameters.ClaimValueDTO;
import eu.europa.esig.dss.ws.attestation.creation.dto.parameters.DisclosureDTO;
import eu.europa.esig.dss.ws.attestation.creation.dto.parameters.DrivingPrivilegeDTO;
import eu.europa.esig.dss.ws.attestation.creation.dto.parameters.RemoteAttestationClaimParameters;
import eu.europa.esig.dss.ws.attestation.creation.dto.parameters.RemoteAttestationDocument;
import eu.europa.esig.dss.ws.attestation.creation.dto.parameters.RemoteAttestationParsingParameters;
import eu.europa.esig.dss.ws.attestation.creation.dto.parameters.RemoteAttestationPayloadParameters;
import eu.europa.esig.dss.ws.attestation.creation.dto.parameters.RemoteAttestationPresentationParameters;
import eu.europa.esig.dss.ws.attestation.creation.dto.parameters.RemoteIdentifierList;
import eu.europa.esig.dss.ws.attestation.creation.dto.parameters.RemoteKeyBindingParameters;
import eu.europa.esig.dss.ws.attestation.creation.dto.parameters.RemotePublicKey;
import eu.europa.esig.dss.ws.attestation.creation.dto.parameters.RemoteTokenStatusList;
import eu.europa.esig.dss.ws.attestation.creation.rest.client.RestAttestationPresentationService;
import eu.europa.esig.dss.ws.attestation.creation.rest.client.RestAttestationSDCreationService;
import eu.europa.esig.dss.ws.converter.DTOConverter;
import eu.europa.esig.dss.ws.converter.RemoteCertificateConverter;
import eu.europa.esig.dss.ws.dto.DigestDTO;
import eu.europa.esig.dss.ws.dto.RemoteDocument;
import eu.europa.esig.dss.ws.dto.SignatureValueDTO;
import eu.europa.esig.dss.ws.dto.ToBeSignedDTO;
import eu.europa.esig.dss.ws.signature.dto.parameters.RemoteBLevelParameters;
import eu.europa.esig.dss.ws.signature.dto.parameters.RemoteSignatureParameters;
import org.apache.cxf.ext.logging.LoggingInInterceptor;
import org.apache.cxf.ext.logging.LoggingOutInterceptor;
import org.apache.cxf.jaxrs.client.JAXRSClientFactoryBean;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.FileInputStream;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class RestAttestationCreationIT extends AbstractRestIT {

    private RestAttestationSDCreationService attestationService;
    private RestAttestationPresentationService presentationService;

    @BeforeEach
    public void init() {
        JAXRSClientFactoryBean factory = new JAXRSClientFactoryBean();

        factory.setAddress(getBaseCxf() + CXFConfig.REST_ATTESTATION_CREATION);
        factory.setServiceClass(RestAttestationSDCreationService.class);
        factory.setProviders(Collections.singletonList(jacksonJsonProvider()));

        LoggingInInterceptor loggingInInterceptor = new LoggingInInterceptor();
        factory.getInInterceptors().add(loggingInInterceptor);
        factory.getInFaultInterceptors().add(loggingInInterceptor);

        LoggingOutInterceptor loggingOutInterceptor = new LoggingOutInterceptor();
        factory.getOutInterceptors().add(loggingOutInterceptor);
        factory.getOutFaultInterceptors().add(loggingOutInterceptor);

        attestationService = factory.create(RestAttestationSDCreationService.class);

        factory = new JAXRSClientFactoryBean();

        factory.setAddress(getBaseCxf() + CXFConfig.REST_ATTESTATION_PRESENTATION);
        factory.setServiceClass(RestAttestationPresentationService.class);
        factory.setProviders(Collections.singletonList(jacksonJsonProvider()));

        loggingInInterceptor = new LoggingInInterceptor();
        factory.getInInterceptors().add(loggingInInterceptor);
        factory.getInFaultInterceptors().add(loggingInInterceptor);

        loggingOutInterceptor = new LoggingOutInterceptor();
        factory.getOutInterceptors().add(loggingOutInterceptor);
        factory.getOutFaultInterceptors().add(loggingOutInterceptor);

        presentationService = factory.create(RestAttestationPresentationService.class);
    }

    @Test
    void sdjwtTest() throws Exception {
        try (Pkcs12SignatureToken rsaToken = new Pkcs12SignatureToken(new FileInputStream("src/test/resources/user_a_rsa.p12"),
                new KeyStore.PasswordProtection("password".toCharArray()));
             Pkcs12SignatureToken ecdsaToken = new Pkcs12SignatureToken(new FileInputStream("src/test/resources/user_ecdsa.p12"),
                     new KeyStore.PasswordProtection("password".toCharArray()))) {

            DSSPrivateKeyEntry rsaKey = rsaToken.getKeys().get(0);
            DSSPrivateKeyEntry ecdsaKey = ecdsaToken.getKeys().get(0);

            Date signingTime = new Date();

            RemoteSignatureParameters signatureParameters = new RemoteSignatureParameters();
            RemoteBLevelParameters bLevelParameters = new RemoteBLevelParameters();
            bLevelParameters.setSigningDate(signingTime);
            signatureParameters.setBLevelParams(bLevelParameters);
            signatureParameters.setSigningCertificate(RemoteCertificateConverter.toRemoteCertificate(rsaKey.getCertificate()));
            signatureParameters.setDigestAlgorithm(DigestAlgorithm.SHA256);

            RemoteAttestationPayloadParameters payloadParameters = new RemoteAttestationPayloadParameters(AttestationForm.SD_JWT);

            payloadParameters.setNotBeforeDate(signingTime);
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.MONTH, 3);
            Date expirationTime = calendar.getTime();
            payloadParameters.setExpirationDate(expirationTime);

            payloadParameters.setIssuer("EAA provider");

            RemotePublicKey publicKey = new RemotePublicKey();
            publicKey.setPublicKey(ecdsaKey.getCertificate().getPublicKey().getEncoded());
            payloadParameters.setDeviceKey(publicKey);

            payloadParameters.setVerifiableCredentialsType("urn:eudi:eaa:1");
            DigestDTO digest = new DigestDTO(DigestAlgorithm.SHA256, DSSUtils.digest(DigestAlgorithm.SHA256, "vct".getBytes()));
            payloadParameters.setVerifiableCredentialsTypeIntegrity(digest);

            payloadParameters.setStatusList(new RemoteTokenStatusList(1, "https://pki.nowina.lu/eaa/status_list"));
            payloadParameters.setCategory("urn:etsi:esi:eaa:eu:qualified");

            RemoteAttestationClaimParameters selectivelyDisclosable = new RemoteAttestationClaimParameters();
            selectivelyDisclosable.setGivenName("John");
            selectivelyDisclosable.setFamilyName("Doe");
            payloadParameters.setSelectivelyDisclosable(selectivelyDisclosable);

            RemoteAttestationClaimParameters nonSelectivelyDisclosable = new RemoteAttestationClaimParameters();
            nonSelectivelyDisclosable.setSubject("good-ecdsa-user");
            nonSelectivelyDisclosable.setIssuingAuthority("TEST Authority");
            nonSelectivelyDisclosable.setIssuingCountry("LU");
            nonSelectivelyDisclosable.setIssuingAuthorityRegistrationIdentifier("VATLU-123456");
            payloadParameters.setNonSelectivelyDisclosable(nonSelectivelyDisclosable);

            List<ClaimDTO> petsArray = new ArrayList<>();
            ClaimValueDTO petsValue = new ClaimValueDTO();
            petsValue.setArrayValue(petsArray);
            ClaimDTO pets = new ClaimDTO("pets", petsValue, true);
            pets.setSelectivelyDisclosable(true);

            List<ClaimDTO> bellaObject = new ArrayList<>();
            bellaObject.add(new ClaimDTO("name", new ClaimValueDTO("Bella"), true));
            bellaObject.add(new ClaimDTO("type", new ClaimValueDTO("dog"), true));
            ClaimValueDTO bellaValue = new ClaimValueDTO();
            bellaValue.setObjectValue(bellaObject);
            petsArray.add(new ClaimDTO(bellaValue, true));

            List<ClaimDTO> slinkyObject = new ArrayList<>();
            slinkyObject.add(new ClaimDTO("name", new ClaimValueDTO("Slinky"), true));
            slinkyObject.add(new ClaimDTO("type", new ClaimValueDTO("cat"), true));
            ClaimValueDTO slinkyValue = new ClaimValueDTO();
            slinkyValue.setObjectValue(slinkyObject);
            petsArray.add(new ClaimDTO(slinkyValue, true));

            payloadParameters.getSelectivelyDisclosable().setOtherClaims(Collections.singletonList(pets));

            DataToSignAttestationDTO dataToSignAttestationDTO = new DataToSignAttestationDTO(payloadParameters, signatureParameters);
            ToBeSignedDTO dataToSign = attestationService.getDataToSign(dataToSignAttestationDTO);
            assertNotNull(dataToSign);

            SignatureValue signatureValue = rsaToken.sign(DTOConverter.toToBeSigned(dataToSign), DigestAlgorithm.SHA256, rsaKey);

            SignAttestationDTO signAttestationDTO = new SignAttestationDTO(payloadParameters, signatureParameters,
                    new SignatureValueDTO(signatureValue.getAlgorithm(), signatureValue.getValue()));
            RemoteDocument signedAttestation = attestationService.signAttestation(signAttestationDTO);
            assertNotNull(signedAttestation);

            DisclosuresDTO disclosuresDTO = new DisclosuresDTO(payloadParameters);
            List<DisclosureDTO> disclosures = attestationService.generateDisclosures(disclosuresDTO);
            assertTrue(Utils.isCollectionNotEmpty(disclosures));

            IssueAttestationDTO issueAttestationDTO = new IssueAttestationDTO(signedAttestation, AttestationForm.SD_JWT, disclosures);
            RemoteDocument issuedAttestation = attestationService.issueAttestation(issueAttestationDTO);

            ParseAttestationDTO parseAttestationDTO = new ParseAttestationDTO(issuedAttestation, new RemoteAttestationParsingParameters(AttestationForm.SD_JWT));
            RemoteAttestationDocument parsedAttestation = presentationService.parseAttestation(parseAttestationDTO);
            signedAttestation = parsedAttestation.getSignedAttestation();
            disclosures = parsedAttestation.getDisclosures();

            RemoteSignatureParameters keyBindingSignatureParameters = new RemoteSignatureParameters();
            keyBindingSignatureParameters.setSigningCertificate(RemoteCertificateConverter.toRemoteCertificate(ecdsaKey.getCertificate()));
            keyBindingSignatureParameters.setDigestAlgorithm(DigestAlgorithm.SHA256);

            RemoteKeyBindingParameters keyBindingParameters = new RemoteKeyBindingParameters();
            keyBindingParameters.setAttestationForm(AttestationForm.SD_JWT);
            keyBindingParameters.setNonce("123456");
            keyBindingParameters.setAudience("audience");

            DataToSignForKeyBindingSignatureDTO dataToSignForKeyBindingSignatureDTO =
                    new DataToSignForKeyBindingSignatureDTO(signedAttestation, disclosures, keyBindingParameters, keyBindingSignatureParameters);
            dataToSign = presentationService.getDataToSignForKeyBindingSignature(dataToSignForKeyBindingSignatureDTO);
            assertNotNull(dataToSign);
            signatureValue = ecdsaToken.sign(DTOConverter.toToBeSigned(dataToSign), DigestAlgorithm.SHA256, ecdsaKey);

            CreateKeyBindingSignatureDTO createKeyBindingSignatureDTO = new CreateKeyBindingSignatureDTO(signedAttestation, disclosures, keyBindingParameters,
                    keyBindingSignatureParameters, new SignatureValueDTO(signatureValue.getAlgorithm(), signatureValue.getValue()));
            RemoteDocument keyBindingSignature = presentationService.createKeyBindingSignature(createKeyBindingSignatureDTO);
            assertNotNull(keyBindingSignature);

            IssuePresentationDTO issuePresentationDTO = new IssuePresentationDTO(signedAttestation, disclosures, keyBindingSignature,
                    new RemoteAttestationPresentationParameters(AttestationForm.SD_JWT));
            RemoteDocument attestationPresentation = presentationService.issuePresentation(issuePresentationDTO);

            InMemoryDocument iMD = new InMemoryDocument(attestationPresentation.getBytes());
            assertNotNull(iMD);
        }
    }

    @Test
    void mdocTest() throws Exception {
        try (Pkcs12SignatureToken token = new Pkcs12SignatureToken(new FileInputStream("src/test/resources/user_ecdsa.p12"),
                     new KeyStore.PasswordProtection("password".toCharArray()))) {

            DSSPrivateKeyEntry privateKey = token.getKeys().get(0);

            RemoteSignatureParameters signatureParameters = new RemoteSignatureParameters();
            RemoteBLevelParameters bLevelParameters = new RemoteBLevelParameters();
            bLevelParameters.setSigningDate(new Date());
            signatureParameters.setBLevelParams(bLevelParameters);
            signatureParameters.setSigningCertificate(RemoteCertificateConverter.toRemoteCertificate(privateKey.getCertificate()));
            signatureParameters.setDigestAlgorithm(DigestAlgorithm.SHA256);

            RemoteAttestationPayloadParameters payloadParameters = new RemoteAttestationPayloadParameters(AttestationForm.MDOC);

            payloadParameters.setDocType(MdocConstants.ISO18013_5_MDL_DOC_TYPE);
            RemotePublicKey publicKey = new RemotePublicKey();
            publicKey.setCertificate(RemoteCertificateConverter.toRemoteCertificate(privateKey.getCertificate()));
            payloadParameters.setDeviceKey(publicKey);

            Calendar calendar = Calendar.getInstance();
            Date signingDate = calendar.getTime();
            payloadParameters.setSigned(signingDate);

            calendar.add(Calendar.DATE, -1);
            Date validFrom = calendar.getTime();
            payloadParameters.setValidFrom(validFrom);

            calendar.add(Calendar.MONTH, 3);
            Date validUntil = calendar.getTime();
            payloadParameters.setValidUntil(validUntil);

            calendar.add(Calendar.MONTH, -2);
            Date nextUpdate = calendar.getTime();
            payloadParameters.setExpectedUpdate(nextUpdate);

            payloadParameters.setIdentifierList(new RemoteIdentifierList(new byte[] { 1 }, "https://pki.nowina.lu/eaa/identifier_list"));

            RemoteAttestationClaimParameters selectivelyDisclosable = new RemoteAttestationClaimParameters();
            selectivelyDisclosable.setFamilyName("Doe");
            selectivelyDisclosable.setGivenName("John");
            selectivelyDisclosable.setBirthdate(DSSUtils.getUtcDate(2001, Calendar.JANUARY, 1));
            selectivelyDisclosable.setAdministrativeIssuanceDate(DSSUtils.getUtcDate(2026, Calendar.JUNE, 1));
            selectivelyDisclosable.setAdministrativeExpirationDate(DSSUtils.getUtcDate(2026, Calendar.AUGUST, 31));
            selectivelyDisclosable.setIssuingCountry("LU");

            selectivelyDisclosable.setIssuingAuthority("TEST Authority");
            selectivelyDisclosable.setIssuingAuthorityRegistrationIdentifier("VATLU-123456789");
            selectivelyDisclosable.setDocumentNumber("123456789");

            DrivingPrivilegeDTO drivingPrivilege = new DrivingPrivilegeDTO("B");
            drivingPrivilege.setIssueDate(DSSUtils.getUtcDate(2020, Calendar.JANUARY, 1));
            drivingPrivilege.setExpiryDate(DSSUtils.getUtcDate(2030, Calendar.JANUARY, 1));
            selectivelyDisclosable.setDrivingPrivileges(Collections.singletonList(drivingPrivilege));

            payloadParameters.setSelectivelyDisclosable(selectivelyDisclosable);

            DataToSignAttestationDTO dataToSignAttestationDTO = new DataToSignAttestationDTO(payloadParameters, signatureParameters);
            ToBeSignedDTO dataToSign = attestationService.getDataToSign(dataToSignAttestationDTO);
            assertNotNull(dataToSign);

            SignatureValue signatureValue = token.sign(DTOConverter.toToBeSigned(dataToSign), DigestAlgorithm.SHA256, privateKey);

            SignAttestationDTO signAttestationDTO = new SignAttestationDTO(payloadParameters, signatureParameters,
                    new SignatureValueDTO(signatureValue.getAlgorithm(), signatureValue.getValue()));
            RemoteDocument signedAttestation = attestationService.signAttestation(signAttestationDTO);
            assertNotNull(signedAttestation);

            DisclosuresDTO disclosuresDTO = new DisclosuresDTO(payloadParameters);
            List<DisclosureDTO> disclosures = attestationService.generateDisclosures(disclosuresDTO);
            assertTrue(Utils.isCollectionNotEmpty(disclosures));

            IssueAttestationDTO issueAttestationDTO = new IssueAttestationDTO(signedAttestation, AttestationForm.MDOC, disclosures);
            RemoteDocument issuedAttestation = attestationService.issueAttestation(issueAttestationDTO);

            ParseAttestationDTO parseAttestationDTO = new ParseAttestationDTO(issuedAttestation, new RemoteAttestationParsingParameters(AttestationForm.MDOC));
            RemoteAttestationDocument parsedAttestation = presentationService.parseAttestation(parseAttestationDTO);
            signedAttestation = parsedAttestation.getSignedAttestation();
            disclosures = parsedAttestation.getDisclosures();

            RemoteSignatureParameters keyBindingSignatureParameters = new RemoteSignatureParameters();
            keyBindingSignatureParameters.setSigningCertificate(RemoteCertificateConverter.toRemoteCertificate(privateKey.getCertificate()));
            keyBindingSignatureParameters.setDigestAlgorithm(DigestAlgorithm.SHA256);

            RemoteKeyBindingParameters keyBindingParameters = new RemoteKeyBindingParameters();
            keyBindingParameters.setAttestationForm(AttestationForm.MDOC);
            keyBindingParameters.setSessionTranscript(new RemoteDocument(Utils.fromHex("80")));
            keyBindingParameters.setDocType(MdocConstants.ISO18013_5_MDL_DOC_TYPE);

            DataToSignForKeyBindingSignatureDTO dataToSignForKeyBindingSignatureDTO =
                    new DataToSignForKeyBindingSignatureDTO(signedAttestation, disclosures, keyBindingParameters, keyBindingSignatureParameters);
            dataToSign = presentationService.getDataToSignForKeyBindingSignature(dataToSignForKeyBindingSignatureDTO);
            assertNotNull(dataToSign);
            signatureValue = token.sign(DTOConverter.toToBeSigned(dataToSign), DigestAlgorithm.SHA256, privateKey);

            CreateKeyBindingSignatureDTO createKeyBindingSignatureDTO = new CreateKeyBindingSignatureDTO(signedAttestation, disclosures, keyBindingParameters,
                    keyBindingSignatureParameters, new SignatureValueDTO(signatureValue.getAlgorithm(), signatureValue.getValue()));
            RemoteDocument keyBindingSignature = presentationService.createKeyBindingSignature(createKeyBindingSignatureDTO);
            assertNotNull(keyBindingSignature);

            IssuePresentationDTO issuePresentationDTO = new IssuePresentationDTO(signedAttestation, disclosures, keyBindingSignature,
                    new RemoteAttestationPresentationParameters(AttestationForm.MDOC));
            RemoteDocument attestationPresentation = presentationService.issuePresentation(issuePresentationDTO);

            InMemoryDocument iMD = new InMemoryDocument(attestationPresentation.getBytes());
            assertNotNull(iMD);
        }
    }

}
