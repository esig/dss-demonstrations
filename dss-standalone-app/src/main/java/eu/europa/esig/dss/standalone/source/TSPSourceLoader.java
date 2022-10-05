package eu.europa.esig.dss.standalone.source;

import eu.europa.esig.dss.service.http.commons.HostConnection;
import eu.europa.esig.dss.service.http.commons.TimestampDataLoader;
import eu.europa.esig.dss.service.http.commons.UserCredentials;
import eu.europa.esig.dss.service.tsp.OnlineTSPSource;
import eu.europa.esig.dss.spi.x509.tsp.TSPSource;
import eu.europa.esig.dss.standalone.service.RemoteDocumentSignatureServiceBuilder;
import eu.europa.esig.dss.token.KeyStoreSignatureTokenConnection;
import eu.europa.esig.dss.utils.Utils;
import eu.europa.esig.dss.x509.tsp.MockTSPSource;
import org.apache.hc.client5.http.ssl.TrustAllStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;

public class TSPSourceLoader {

    private static final Logger LOG = LoggerFactory.getLogger(TSPSourceLoader.class);

    private static TSPSource tspSource;

    public static TSPSource getTspSource() {
        if (tspSource == null) {
            tspSource = loadTspSource();
        }
        return tspSource;
    }

    private static TSPSource loadTspSource() {
        if (Utils.isTrue(PropertyReader.getBooleanProperty("timestamp.mock"))) {
            MockTSPSource tspSource = new MockTSPSource();
            try (InputStream is = RemoteDocumentSignatureServiceBuilder.class.getResourceAsStream("/self-signed-tsa.p12")) {
                tspSource.setToken(new KeyStoreSignatureTokenConnection(is, "PKCS12", new KeyStore.PasswordProtection("ks-password".toCharArray())));
            } catch (IOException e) {
                LOG.warn("Cannot load the KeyStore");
            }
            tspSource.setAlias("self-signed-tsa");
            return tspSource;

        } else {
            OnlineTSPSource tspSource = new OnlineTSPSource(PropertyReader.getProperty("timestamp.url"));
            TimestampDataLoader dataLoader = new TimestampDataLoader();
            dataLoader.setTrustStrategy(TrustAllStrategy.INSTANCE);

            String host = PropertyReader.getProperty("timestamp.host");
            if (Utils.isStringNotEmpty(host)) {
                HostConnection hostConnection = new HostConnection();
                hostConnection.setHost(PropertyReader.getProperty("timestamp.host"));
                hostConnection.setPort(PropertyReader.getIntProperty("timestamp.port"));
                hostConnection.setProtocol(PropertyReader.getProperty("timestamp.protocol"));

                UserCredentials userCredentials = new UserCredentials();
                userCredentials.setUsername(PropertyReader.getProperty("timestamp.username"));
                userCredentials.setPassword(PropertyReader.getProperty("timestamp.password"));

                dataLoader.addAuthentication(hostConnection, userCredentials);
            }

            tspSource.setDataLoader(dataLoader);
            return tspSource;
        }
    }

}
