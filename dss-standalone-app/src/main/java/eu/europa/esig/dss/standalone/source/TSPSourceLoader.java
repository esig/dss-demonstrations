package eu.europa.esig.dss.standalone.source;

import eu.europa.esig.dss.service.http.commons.HostConnection;
import eu.europa.esig.dss.service.http.commons.TimestampDataLoader;
import eu.europa.esig.dss.service.http.commons.UserCredentials;
import eu.europa.esig.dss.service.tsp.OnlineTSPSource;
import eu.europa.esig.dss.spi.x509.tsp.KeyEntityTSPSource;
import eu.europa.esig.dss.spi.x509.tsp.TSPSource;
import eu.europa.esig.dss.utils.Utils;
import org.apache.hc.client5.http.ssl.TrustAllStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
            final String ksFilePath = PropertyReader.getProperty("timestamp.mock.keystore.file");
            final String ksType = PropertyReader.getProperty("timestamp.mock.keystore.type");
            final char[] ksPassword = PropertyReader.getCharArrayProperty("timestamp.mock.keystore.password");
            final String alias = PropertyReader.getProperty("timestamp.mock.keystore.alias");
            final String tsaPolicy = PropertyReader.getProperty("timestamp.mock.policy.oid");

            KeyStore keyStore;
            try (InputStream is = TSPSourceLoader.class.getResourceAsStream(ksFilePath)) {
                keyStore = KeyStore.getInstance(ksType);
                keyStore.load(is, ksPassword);
            } catch (Exception e) {
                LOG.error("Cannot load the KeyStore TSPSource! Reason : {}", e.getMessage(), e);
                return null;
            }

            KeyEntityTSPSource keyEntityTSPSource = new KeyEntityTSPSource(keyStore, alias, ksPassword);
            keyEntityTSPSource.setTsaPolicy(tsaPolicy);
            return keyEntityTSPSource;

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
                userCredentials.setPassword(PropertyReader.getCharArrayProperty("timestamp.password"));

                dataLoader.addAuthentication(hostConnection, userCredentials);
            }

            tspSource.setDataLoader(dataLoader);
            return tspSource;
        }
    }

}
