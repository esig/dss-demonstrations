package eu.europa.esig.dss.standalone.source;

import eu.europa.esig.dss.spi.x509.CommonTrustedCertificateSource;
import eu.europa.esig.dss.spi.x509.KeyStoreCertificateSource;
import eu.europa.esig.dss.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;

public class TrustedCertificateSourceLoader {

    private static final Logger LOG = LoggerFactory.getLogger(TrustedCertificateSourceLoader.class);

    private static CommonTrustedCertificateSource trustedCertificateSource;

    public static CommonTrustedCertificateSource getTrustedCertificateSource() {
        if (trustedCertificateSource == null) {
            trustedCertificateSource = loadTrustedCertificateSource();
        }
        return trustedCertificateSource;
    }

    private static CommonTrustedCertificateSource loadTrustedCertificateSource() {
        final CommonTrustedCertificateSource trustedCertificateSource = new CommonTrustedCertificateSource();

            final String ksFilePath = PropertyReader.getProperty("trusted.source.keystore.filename");
            final String ksType = PropertyReader.getProperty("trusted.source.keystore.type");
            final char[] ksPassword = PropertyReader.getCharArrayProperty("trusted.source.keystore.password");
            if (Utils.isStringNotEmpty(ksFilePath)) {
                try (InputStream is = TSPSourceLoader.class.getResourceAsStream(ksFilePath)) {
                    KeyStoreCertificateSource keyStore = new KeyStoreCertificateSource(is, ksType, ksPassword);
                    trustedCertificateSource.importAsTrusted(keyStore);

                } catch (IOException e) {
                    LOG.error("Unable to load a trusted key store: {}", e.getMessage(), e);
                }
            }

        return trustedCertificateSource;
    }

}
