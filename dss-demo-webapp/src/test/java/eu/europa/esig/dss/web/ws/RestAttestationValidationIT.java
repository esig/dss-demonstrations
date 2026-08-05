package eu.europa.esig.dss.web.ws;

import eu.europa.esig.dss.diagnostic.AttestationWrapper;
import eu.europa.esig.dss.diagnostic.DiagnosticData;
import eu.europa.esig.dss.diagnostic.SignatureWrapper;
import eu.europa.esig.dss.enumerations.AttestationProfile;
import eu.europa.esig.dss.model.FileDocument;
import eu.europa.esig.dss.validation.reports.Reports;
import eu.europa.esig.dss.web.config.CXFConfig;
import eu.europa.esig.dss.ws.attestation.validation.dto.AttestationToValidateDTO;
import eu.europa.esig.dss.ws.attestation.validation.rest.client.RestAttestationValidationService;
import eu.europa.esig.dss.ws.converter.RemoteDocumentConverter;
import eu.europa.esig.dss.ws.dto.RemoteDocument;
import eu.europa.esig.dss.ws.validation.dto.WSReportsDTO;
import org.apache.cxf.ext.logging.LoggingInInterceptor;
import org.apache.cxf.ext.logging.LoggingOutInterceptor;
import org.apache.cxf.jaxrs.client.JAXRSClientFactoryBean;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class RestAttestationValidationIT extends AbstractRestIT {

    private RestAttestationValidationService validationService;

    @BeforeEach
    public void init() {
        JAXRSClientFactoryBean factory = new JAXRSClientFactoryBean();

        factory.setAddress(getBaseCxf() + CXFConfig.REST_ATTESTATION_VALIDATION);
        factory.setServiceClass(RestAttestationValidationService.class);
        factory.setProviders(Collections.singletonList(jacksonJsonProvider()));

        LoggingInInterceptor loggingInInterceptor = new LoggingInInterceptor();
        factory.getInInterceptors().add(loggingInInterceptor);
        factory.getInFaultInterceptors().add(loggingInInterceptor);

        LoggingOutInterceptor loggingOutInterceptor = new LoggingOutInterceptor();
        factory.getOutInterceptors().add(loggingOutInterceptor);
        factory.getOutFaultInterceptors().add(loggingOutInterceptor);

        validationService = factory.create(RestAttestationValidationService.class);
    }

    @Test
    public void test() {
        RemoteDocument attestationPresentation = RemoteDocumentConverter.toRemoteDocument(new FileDocument("src/test/resources/sd-jwt-eaa.json"));
        AttestationToValidateDTO dto = new AttestationToValidateDTO(attestationPresentation);
        WSReportsDTO result = validationService.validateAttestation(dto);

        assertNotNull(result.getDiagnosticData());
        assertNotNull(result.getDetailedReport());
        assertNotNull(result.getSimpleReport());
        assertNotNull(result.getValidationReport());

        Reports reports = new Reports(result.getDiagnosticData(), result.getDetailedReport(), result.getSimpleReport(), result.getValidationReport());

        assertNotNull(reports);
        assertNotNull(reports.getDiagnosticData());
        assertNotNull(reports.getDetailedReport());
        assertNotNull(reports.getSimpleReport());

        DiagnosticData diagnosticData = reports.getDiagnosticData();
        AttestationWrapper attestation = diagnosticData.getAttestationById(diagnosticData.getFirstAttestationId());
        assertNotNull(attestation);
        assertEquals(AttestationProfile.SD_JWT_VC, attestation.getAttestationProfile());

        List<SignatureWrapper> attestationSignatures = attestation.getAttestationSignatures();
        assertEquals(1, attestationSignatures.size());
        SignatureWrapper signature = attestationSignatures.get(0);
        assertTrue(signature.isBLevelTechnicallyValid());
    }

}
