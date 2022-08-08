package eu.europa.esig.dss.standalone;

import eu.europa.esig.dss.validation.CommonCertificateVerifier;
import eu.europa.esig.dss.ws.signature.common.RemoteTrustedListSignatureService;
import eu.europa.esig.dss.ws.signature.common.RemoteTrustedListSignatureServiceImpl;
import eu.europa.esig.dss.xades.signature.XAdESService;

public class RemoteTrustedListSignatureServiceBuilder {

    public RemoteTrustedListSignatureService build() {
        RemoteTrustedListSignatureServiceImpl service = new RemoteTrustedListSignatureServiceImpl();
        service.setXadesService(xadesService());
        return service;
    }

    private XAdESService xadesService() {
        return new XAdESService(new CommonCertificateVerifier());
    }

}
