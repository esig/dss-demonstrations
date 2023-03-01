package eu.europa.esig.dss.standalone.source;

import eu.europa.esig.dss.service.http.proxy.ProxyConfig;
import eu.europa.esig.dss.service.http.proxy.ProxyProperties;
import eu.europa.esig.dss.utils.Utils;

import java.util.List;

public class ProxyConfiguration {

    private static ProxyConfig config = null;

    public static ProxyConfig proxyConfig() {
        if (config == null) {
            boolean httpEnabled = Utils.isTrue(PropertyReader.getBooleanProperty("proxy.http.enabled"));
            boolean httpsEnabled = Utils.isTrue(PropertyReader.getBooleanProperty("proxy.https.enabled"));

            if (!httpEnabled && !httpsEnabled) {
                return null;
            }
            config = new ProxyConfig();
            if (httpEnabled) {
                ProxyProperties httpProperties = new ProxyProperties();
                String httpHost = PropertyReader.getProperty("proxy.http.host");
                if (Utils.isStringNotEmpty(httpHost)) {
                    httpProperties.setHost(httpHost);
                }
                String httpScheme = PropertyReader.getProperty("proxy.http.scheme");
                if (Utils.isStringNotEmpty(httpScheme)) {
                    httpProperties.setScheme(httpScheme);
                }
                int httpPort = PropertyReader.getIntProperty("proxy.http.port");
                if (httpPort != -1) {
                    httpProperties.setPort(httpPort);
                }
                String httpUser = PropertyReader.getProperty("proxy.http.user");
                if (Utils.isStringNotEmpty(httpUser)) {
                    httpProperties.setUser(httpUser);
                }
                char[] httpPassword = PropertyReader.getCharArrayProperty("proxy.http.password");
                if (Utils.isArrayNotEmpty(httpPassword)) {
                    httpProperties.setPassword(httpPassword);
                }
                List<String> httpExcludedHosts = PropertyReader.getPropertyAsList("proxy.http.exclude");
                if (Utils.isCollectionNotEmpty(httpExcludedHosts)) {
                    httpProperties.setExcludedHosts(httpExcludedHosts);
                }
                config.setHttpProperties(httpProperties);
            }
            if (httpsEnabled) {
                ProxyProperties httpsProperties = new ProxyProperties();
                String httpsHost = PropertyReader.getProperty("proxy.https.host");
                if (Utils.isStringNotEmpty(httpsHost)) {
                    httpsProperties.setHost(httpsHost);
                }
                String httpsScheme = PropertyReader.getProperty("proxy.https.scheme");
                if (Utils.isStringNotEmpty(httpsScheme)) {
                    httpsProperties.setScheme(httpsScheme);
                }
                int httpsPort = PropertyReader.getIntProperty("proxy.https.port");
                if (httpsPort != -1) {
                    httpsProperties.setPort(httpsPort);
                }
                String httpsUser = PropertyReader.getProperty("proxy.https.user");
                if (Utils.isStringNotEmpty(httpsUser)) {
                    httpsProperties.setUser(httpsUser);
                }
                char[] httpsPassword = PropertyReader.getCharArrayProperty("proxy.https.password");
                if (Utils.isArrayNotEmpty(httpsPassword)) {
                    httpsProperties.setPassword(httpsPassword);
                }
                List<String> httpsExcludedHosts = PropertyReader.getPropertyAsList("proxy.https.exclude");
                if (Utils.isCollectionNotEmpty(httpsExcludedHosts)) {
                    httpsProperties.setExcludedHosts(httpsExcludedHosts);
                }
                config.setHttpsProperties(httpsProperties);
            }
        }
        return config;
    }

}
