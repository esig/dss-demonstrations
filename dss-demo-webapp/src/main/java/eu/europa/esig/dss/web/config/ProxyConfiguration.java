package eu.europa.esig.dss.web.config;

import eu.europa.esig.dss.utils.Utils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import eu.europa.esig.dss.service.http.proxy.ProxyConfig;
import eu.europa.esig.dss.service.http.proxy.ProxyProperties;

import java.util.Collection;

@Configuration
public class ProxyConfiguration {

	@Value("${proxy.http.enabled:false}")
	private boolean httpEnabled;
	@Value("${proxy.http.host:}")
	private String httpHost;
	@Value("${proxy.http.port:-1}")
	private int httpPort;
	@Value("${proxy.http.scheme:}")
	private String httpScheme;
	@Value("${proxy.http.user:}")
	private String httpUser;
	@Value("${proxy.http.password:}")
	private String httpPassword;
	@Value("${proxy.http.exclude:}")
	private Collection<String> httpExcludedHosts;

	@Value("${proxy.https.enabled:false}")
	private boolean httpsEnabled;
	@Value("${proxy.https.host:}")
	private String httpsHost;
	@Value("${proxy.https.port:-1}")
	private int httpsPort;
	@Value("${proxy.https.scheme:}")
	private String httpsScheme;
	@Value("${proxy.https.user:}")
	private String httpsUser;
	@Value("${proxy.https.password:}")
	private String httpsPassword;
	@Value("${proxy.https.exclude:}")
	private Collection<String> httpsExcludedHosts;

	@Bean
	public ProxyConfig proxyConfig() {
		if (!httpEnabled && !httpsEnabled) {
			return null;
		}
		ProxyConfig config = new ProxyConfig();
		if (httpEnabled) {
			ProxyProperties httpProperties = new ProxyProperties();
			if (Utils.isStringNotEmpty(httpHost)) {
				httpProperties.setHost(httpHost);
			}
			if (httpPort != -1) {
				httpProperties.setPort(httpPort);
			}
			if (Utils.isStringNotEmpty(httpScheme)) {
				httpProperties.setScheme(httpScheme);
			}
			if (Utils.isStringNotEmpty(httpUser)) {
				httpProperties.setUser(httpUser);
			}
			if (Utils.isStringNotEmpty(httpPassword)) {
				httpProperties.setPassword(httpPassword);
			}
			if (Utils.isCollectionNotEmpty(httpExcludedHosts)) {
				httpProperties.setExcludedHosts(httpExcludedHosts);
			}
			config.setHttpProperties(httpProperties);
		}
		if (httpsEnabled) {
			ProxyProperties httpsProperties = new ProxyProperties();
			if (Utils.isStringNotEmpty(httpsHost)) {
				httpsProperties.setHost(httpsHost);
			}
			if (httpsPort != -1) {
				httpsProperties.setPort(httpsPort);
			}
			if (Utils.isStringNotEmpty(httpsScheme)) {
				httpsProperties.setScheme(httpsScheme);
			}
			if (Utils.isStringNotEmpty(httpsUser)) {
				httpsProperties.setUser(httpsUser);
			}
			if (Utils.isStringNotEmpty(httpsPassword)) {
				httpsProperties.setPassword(httpsPassword);
			}
			if (Utils.isCollectionNotEmpty(httpsExcludedHosts)) {
				httpsProperties.setExcludedHosts(httpsExcludedHosts);
			}
			config.setHttpsProperties(httpsProperties);
		}
		return config;
	}

}
