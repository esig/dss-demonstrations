package eu.europa.esig.dss.standalone.source;

import eu.europa.esig.dss.service.http.commons.CommonsDataLoader;
import eu.europa.esig.dss.spi.client.http.DataLoader;
import eu.europa.esig.dss.utils.Utils;

public class DataLoaderConfigLoader {

    public static DataLoader getDataLoader() {
        return getDataLoader(new CommonsDataLoader());
    }

    public static <C extends CommonsDataLoader> C getDataLoader(C dataLoader) {
        configure(dataLoader);
        return dataLoader;
    }

    private static <C extends CommonsDataLoader> void configure(C dataLoader) {
        dataLoader.setTimeoutConnection(PropertyReader.getIntProperty("dataloader.connection.timeout"));
        dataLoader.setTimeoutConnectionRequest(PropertyReader.getIntProperty("dataloader.connection.request.timeout"));
        dataLoader.setRedirectsEnabled(Utils.isTrue(PropertyReader.getBooleanProperty("dataloader.redirect.enabled")));
        dataLoader.setProxyConfig(ProxyConfiguration.proxyConfig());
    }

}
