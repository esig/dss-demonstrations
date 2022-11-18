package eu.europa.esig.dss.standalone.service;

import eu.europa.esig.dss.spi.tsl.TrustedListsCertificateSource;
import eu.europa.esig.dss.spi.x509.CertificateSource;
import eu.europa.esig.dss.spi.x509.CommonCertificateSource;
import eu.europa.esig.dss.spi.x509.tsp.TSPSource;
import eu.europa.esig.dss.standalone.source.CertificateVerifierBuilder;
import eu.europa.esig.dss.standalone.source.TSPSourceLoader;
import eu.europa.esig.dss.validation.CertificateVerifier;

public class AbstractDocumentServiceBuilder {

    private TrustedListsCertificateSource tslCertificateSource = new TrustedListsCertificateSource();

    private CertificateSource adjunctCertificateSource = new CommonCertificateSource();

    public AbstractDocumentServiceBuilder setTslCertificateSource(TrustedListsCertificateSource tslCertificateSource) {
        this.tslCertificateSource = tslCertificateSource;
        return this;
    }

    public AbstractDocumentServiceBuilder setAdjunctCertificateSource(CertificateSource adjunctCertificateSource) {
        this.adjunctCertificateSource = adjunctCertificateSource;
        return this;
    }

    protected TSPSource tspSource() {
        return TSPSourceLoader.getTspSource();
    }

    protected CertificateVerifier certificateVerifier() {
        return new CertificateVerifierBuilder()
                .setTslCertificateSource(tslCertificateSource)
                .setAdjunctCertificateSource(adjunctCertificateSource)
                .build();
    }

}
