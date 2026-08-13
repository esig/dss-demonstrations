package eu.europa.esig.dss.standalone.service;

import eu.europa.esig.dss.cbades.signature.CBAdESService;
import eu.europa.esig.dss.jades.signature.JAdESService;
import eu.europa.esig.dss.spi.validation.CommonCertificateVerifier;
import eu.europa.esig.dss.ws.signature.common.RemoteListOfTrustedEntitiesSignatureService;
import eu.europa.esig.dss.ws.signature.common.RemoteListOfTrustedEntitiesSignatureServiceImpl;
import eu.europa.esig.dss.xades.signature.XAdESService;

public class RemoteListOfTrustedEntitiesSignatureServiceBuilder {

    public RemoteListOfTrustedEntitiesSignatureService build() {
        RemoteListOfTrustedEntitiesSignatureServiceImpl service = new RemoteListOfTrustedEntitiesSignatureServiceImpl();
        service.setXadesService(xadesService());
        service.setJadesService(jadesService());
        service.setCbadesService(cbadesService());
        return service;
    }

    private XAdESService xadesService() {
        return new XAdESService(new CommonCertificateVerifier());
    }

    private JAdESService jadesService() {
        return new JAdESService(new CommonCertificateVerifier());
    }

    private CBAdESService cbadesService() {
        return new CBAdESService(new CommonCertificateVerifier());
    }

}
