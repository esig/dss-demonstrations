package eu.europa.esig.dss.standalone.service;

import eu.europa.esig.dss.asic.cades.signature.ASiCWithCAdESService;
import eu.europa.esig.dss.asic.xades.signature.ASiCWithXAdESService;
import eu.europa.esig.dss.jades.signature.JAdESService;
import eu.europa.esig.dss.ws.signature.common.RemoteMultipleDocumentsSignatureServiceImpl;
import eu.europa.esig.dss.xades.signature.XAdESService;

public class RemoteMultipleDocumentSignatureServiceBuilder extends AbstractDocumentServiceBuilder {

    public RemoteMultipleDocumentsSignatureServiceImpl build() {
        RemoteMultipleDocumentsSignatureServiceImpl service = new RemoteMultipleDocumentsSignatureServiceImpl();
        service.setAsicWithCAdESService(asicWithCadesService());
        service.setAsicWithXAdESService(asicWithXadesService());
        service.setXadesService(xadesService());
        service.setJadesService(jadesService());
        return service;
    }

    private ASiCWithCAdESService asicWithCadesService() {
        ASiCWithCAdESService service = new ASiCWithCAdESService(certificateVerifier());
        service.setTspSource(tspSource());
        return service;
    }

    private ASiCWithXAdESService asicWithXadesService() {
        ASiCWithXAdESService service = new ASiCWithXAdESService(certificateVerifier());
        service.setTspSource(tspSource());
        return service;
    }

    private XAdESService xadesService() {
        XAdESService service = new XAdESService(certificateVerifier());
        service.setTspSource(tspSource());
        return service;
    }

    private JAdESService jadesService() {
        JAdESService service = new JAdESService(certificateVerifier());
        service.setTspSource(tspSource());
        return service;
    }

}
