package eu.europa.esig.dss.standalone.utils;

import eu.europa.esig.dss.model.x509.CertificateToken;
import eu.europa.esig.dss.spi.DSSUtils;
import eu.europa.esig.dss.spi.x509.CertificateSource;
import eu.europa.esig.dss.spi.x509.CommonCertificateSource;
import eu.europa.esig.dss.utils.Utils;

import java.io.File;
import java.util.Collection;

public final class StandaloneAppUtils {

    private StandaloneAppUtils() {
        // empty
    }

    public static CertificateToken toCertificateToken(File certificateFile) {
        if (certificateFile != null) {
            byte[] certificateBytes = DSSUtils.toByteArray(certificateFile);
            String certificateBytesString = new String(certificateBytes);
            if (!isPem(certificateBytes) && Utils.isBase64Encoded(certificateBytesString)) {
                return DSSUtils.loadCertificateFromBase64EncodedString(certificateBytesString);
            }
            return DSSUtils.loadCertificate(certificateBytes);
        }
        return null;
    }

    // TODO : to remove after https://ec.europa.eu/digital-building-blocks/tracker/browse/DSS-3647 is resolved
    private static boolean isPem(byte[] string) {
        return Utils.startsWith(string, "-----".getBytes());
    }

    public static CertificateSource toCertificateSource(Collection<File> certificateFiles) {
        CertificateSource certSource = null;
        if (Utils.isCollectionNotEmpty(certificateFiles)) {
            certSource = new CommonCertificateSource();
            for (File file : certificateFiles) {
                CertificateToken certificateChainItem = toCertificateToken(file);
                if (certificateChainItem != null) {
                    certSource.addCertificate(certificateChainItem);
                }
            }
        }
        return certSource;
    }

}
