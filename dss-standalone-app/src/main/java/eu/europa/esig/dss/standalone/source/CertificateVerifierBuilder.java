package eu.europa.esig.dss.standalone.source;

import eu.europa.esig.dss.alert.ExceptionOnStatusAlert;
import eu.europa.esig.dss.service.crl.OnlineCRLSource;
import eu.europa.esig.dss.service.http.commons.OCSPDataLoader;
import eu.europa.esig.dss.service.ocsp.OnlineOCSPSource;
import eu.europa.esig.dss.spi.tsl.TrustedListsCertificateSource;
import eu.europa.esig.dss.spi.x509.CertificateSource;
import eu.europa.esig.dss.spi.x509.CommonCertificateSource;
import eu.europa.esig.dss.spi.x509.aia.DefaultAIASource;
import eu.europa.esig.dss.spi.x509.aia.OnlineAIASource;
import eu.europa.esig.dss.validation.CertificateVerifier;
import eu.europa.esig.dss.validation.CommonCertificateVerifier;

public class CertificateVerifierBuilder {

    private TrustedListsCertificateSource tslCertificateSource = new TrustedListsCertificateSource();

    private CertificateSource adjunctCertificateSource = new CommonCertificateSource();

    public CertificateVerifierBuilder setTslCertificateSource(TrustedListsCertificateSource tslCertificateSource) {
        this.tslCertificateSource = tslCertificateSource;
        return this;
    }

    public CertificateVerifierBuilder setAdjunctCertificateSource(CertificateSource adjunctCertificateSource) {
        this.adjunctCertificateSource = adjunctCertificateSource;
        return this;
    }

    public CertificateVerifier build() {
        CommonCertificateVerifier certificateVerifier = new CommonCertificateVerifier();
        certificateVerifier.setCrlSource(onlineCRLSource());
        certificateVerifier.setOcspSource(onlineOCSPSource());
        certificateVerifier.setAIASource(onlineAIASource());
        certificateVerifier.setTrustedCertSources(tslCertificateSource);
        certificateVerifier.setAdjunctCertSources(adjunctCertificateSource);

        // Default configs
        certificateVerifier.setAlertOnMissingRevocationData(new ExceptionOnStatusAlert());
        certificateVerifier.setCheckRevocationForUntrustedChains(false);

        return certificateVerifier;
    }

    private OnlineAIASource onlineAIASource() {
        OnlineAIASource onlineAIASource = new DefaultAIASource();
        onlineAIASource.setDataLoader(DataLoaderConfigLoader.getDataLoader());
        return onlineAIASource;
    }

    private OnlineCRLSource onlineCRLSource() {
        OnlineCRLSource onlineCRLSource = new OnlineCRLSource();
        onlineCRLSource.setDataLoader(DataLoaderConfigLoader.getDataLoader());
        return onlineCRLSource;
    }

    private OnlineOCSPSource onlineOCSPSource() {
        OnlineOCSPSource onlineOCSPSource = new OnlineOCSPSource();
        OCSPDataLoader ocspDataLoader = new OCSPDataLoader();
        onlineOCSPSource.setDataLoader(DataLoaderConfigLoader.getDataLoader(ocspDataLoader));
        return onlineOCSPSource;
    }

}
