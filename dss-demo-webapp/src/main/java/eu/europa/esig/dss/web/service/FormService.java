package eu.europa.esig.dss.web.service;

import org.springframework.stereotype.Component;

import eu.europa.esig.dss.client.http.proxy.ProxyConfig;
import eu.europa.esig.dss.client.http.proxy.ProxyProps;
import eu.europa.esig.dss.web.model.ProxyConfigForm;

@Component
public class FormService {

	public ProxyConfigForm getForm(ProxyConfig proxyConfig) {
		ProxyConfigForm form = new ProxyConfigForm();
		
		form.setHttpEnabled(proxyConfig.getHttpProps().isEnabled());
		form.setHttpExcludedHost(proxyConfig.getHttpProps().getExcludedHost());
		form.setHttpHost(proxyConfig.getHttpProps().getHost());
		form.setHttpPassword(proxyConfig.getHttpProps().getPassword());
		form.setHttpPort(proxyConfig.getHttpProps().getPort());
		form.setHttpUser(proxyConfig.getHttpProps().getUser());
		
		form.setHttpsEnabled(proxyConfig.getHttpsProps().isEnabled());
		form.setHttpsExcludedHost(proxyConfig.getHttpsProps().getExcludedHost());
		form.setHttpsHost(proxyConfig.getHttpsProps().getHost());
		form.setHttpsPassword(proxyConfig.getHttpsProps().getPassword());
		form.setHttpsPort(proxyConfig.getHttpsProps().getPort());
		form.setHttpsUser(proxyConfig.getHttpsProps().getUser());
		
		return form;
	}
	
	public ProxyConfig getProxyConfig(ProxyConfigForm proxyConfigForm) {
		ProxyConfig proxyConfig = new ProxyConfig();
		
		ProxyProps httpProps = new ProxyProps();
		httpProps.setEnabled(proxyConfigForm.isHttpEnabled());
		httpProps.setExcludedHost(proxyConfigForm.getHttpExcludedHost());
		httpProps.setHost(proxyConfigForm.getHttpHost());
		httpProps.setPassword(proxyConfigForm.getHttpPassword());
		httpProps.setPort(proxyConfigForm.getHttpPort());
		httpProps.setUser(proxyConfigForm.getHttpUser());
		proxyConfig.setHttpProps(httpProps);
		
		ProxyProps httpsProps = new ProxyProps();
		httpsProps.setEnabled(proxyConfigForm.isHttpsEnabled());
		httpsProps.setExcludedHost(proxyConfigForm.getHttpsExcludedHost());
		httpsProps.setHost(proxyConfigForm.getHttpsHost());
		httpsProps.setPassword(proxyConfigForm.getHttpsPassword());
		httpsProps.setPort(proxyConfigForm.getHttpsPort());
		httpsProps.setUser(proxyConfigForm.getHttpsUser());
		proxyConfig.setHttpsProps(httpsProps);
		
		return proxyConfig;
	}
}
