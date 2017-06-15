package eu.europa.esig.dss.web.business;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.bind.JAXBException;

import org.springframework.beans.factory.annotation.Value;

import eu.europa.esig.dss.client.http.proxy.ProxyConfig;
import eu.europa.esig.dss.client.http.proxy.ProxyConfigManager;
import eu.europa.esig.dss.client.http.proxy.ProxyProps;
import eu.europa.esig.dss.web.model.jaxb.JaxbProxyConfig;
import eu.europa.esig.dss.web.model.jaxb.JaxbProxyProps;

public class ProxyConfigFileManager implements ProxyConfigManager {

	@Value("${proxy.config.location}")
	private String proxyConfigLocation;
	
	private JaxbProxyConfig jaxbProxyConfig;
	
	@Override
	public void update(ProxyConfig proxyConfig) {
		if(proxyConfig != null) {
			JaxbProxyProps jaxbProxyPropsHttp = new JaxbProxyProps();
			jaxbProxyPropsHttp.setEnabled(proxyConfig.getHttpProps().isEnabled());
			jaxbProxyPropsHttp.setExcludedHost(proxyConfig.getHttpProps().getExcludedHost());
			jaxbProxyPropsHttp.setHost(proxyConfig.getHttpProps().getHost());
			jaxbProxyPropsHttp.setPassword(proxyConfig.getHttpProps().getPassword());
			jaxbProxyPropsHttp.setPort(proxyConfig.getHttpProps().getPort());
			jaxbProxyPropsHttp.setUser(proxyConfig.getHttpProps().getUser());
			jaxbProxyConfig.setHttpProps(jaxbProxyPropsHttp);
			
			JaxbProxyProps jaxbProxyPropsHttps = new JaxbProxyProps();
			jaxbProxyPropsHttps.setEnabled(proxyConfig.getHttpsProps().isEnabled());
			jaxbProxyPropsHttps.setExcludedHost(proxyConfig.getHttpsProps().getExcludedHost());
			jaxbProxyPropsHttps.setHost(proxyConfig.getHttpsProps().getHost());
			jaxbProxyPropsHttps.setPassword(proxyConfig.getHttpsProps().getPassword());
			jaxbProxyPropsHttps.setPort(proxyConfig.getHttpsProps().getPort());
			jaxbProxyPropsHttps.setUser(proxyConfig.getHttpsProps().getUser());
			jaxbProxyConfig.setHttpsProps(jaxbProxyPropsHttps);
		}
	}

	@Override
	public ProxyConfig get() {
		if(jaxbProxyConfig == null) {
			ClassLoader classloader = Thread.currentThread().getContextClassLoader();
			try (InputStream is = classloader.getResourceAsStream(proxyConfigLocation)) {
				jaxbProxyConfig = FileManager.readFromXml(JaxbProxyConfig.class, is); 
			} catch (IOException | JAXBException e) {
				throw new RuntimeException(e);
			}
		}
		
		ProxyConfig proxyConfig = new ProxyConfig();
		
		ProxyProps proxyPropsHttp = new ProxyProps();
		proxyPropsHttp.setEnabled(jaxbProxyConfig.getHttpProps().isEnabled());
		proxyPropsHttp.setExcludedHost(jaxbProxyConfig.getHttpProps().getExcludedHost());
		proxyPropsHttp.setHost(jaxbProxyConfig.getHttpProps().getHost());
		proxyPropsHttp.setPassword(jaxbProxyConfig.getHttpProps().getPassword());
		proxyPropsHttp.setPort(jaxbProxyConfig.getHttpProps().getPort());
		proxyPropsHttp.setUser(jaxbProxyConfig.getHttpProps().getUser());
		proxyConfig.setHttpProps(proxyPropsHttp);
		
		ProxyProps proxyPropsHttps = new ProxyProps();
		proxyPropsHttps.setEnabled(jaxbProxyConfig.getHttpsProps().isEnabled());
		proxyPropsHttps.setExcludedHost(jaxbProxyConfig.getHttpsProps().getExcludedHost());
		proxyPropsHttps.setHost(jaxbProxyConfig.getHttpsProps().getHost());
		proxyPropsHttps.setPassword(jaxbProxyConfig.getHttpsProps().getPassword());
		proxyPropsHttps.setPort(jaxbProxyConfig.getHttpsProps().getPort());
		proxyPropsHttps.setUser(jaxbProxyConfig.getHttpsProps().getUser());
		proxyConfig.setHttpsProps(proxyPropsHttps);
		
		return proxyConfig;
	}

}
