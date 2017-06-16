package eu.europa.esig.dss.web.model;

import eu.europa.esig.dss.client.http.proxy.ProxyConfig;
import eu.europa.esig.dss.client.http.proxy.ProxyProps;

public class ProxyConfigForm {

	private boolean httpEnabled;
	
	private String httpExcludedHost;
	
	private String httpHost;
	
	private String httpPassword;
	
	private String httpUser;
	
	private Long httpPort;
	
	private boolean httpsEnabled;
	
	private String httpsExcludedHost;
	
	private String httpsHost;
	
	private String httpsPassword;
	
	private String httpsUser;
	
	private Long httpsPort;

	public boolean isHttpEnabled() {
		return httpEnabled;
	}

	public void setHttpEnabled(boolean httpEnabled) {
		this.httpEnabled = httpEnabled;
	}

	public String getHttpExcludedHost() {
		return httpExcludedHost;
	}

	public void setHttpExcludedHost(String httpExcludedHost) {
		this.httpExcludedHost = httpExcludedHost;
	}

	public String getHttpHost() {
		return httpHost;
	}

	public void setHttpHost(String httpHost) {
		this.httpHost = httpHost;
	}

	public String getHttpPassword() {
		return httpPassword;
	}

	public void setHttpPassword(String httpPassword) {
		this.httpPassword = httpPassword;
	}

	public String getHttpUser() {
		return httpUser;
	}

	public void setHttpUser(String httpUser) {
		this.httpUser = httpUser;
	}

	public Long getHttpPort() {
		return httpPort;
	}

	public void setHttpPort(Long httpPort) {
		this.httpPort = httpPort;
	}

	public boolean isHttpsEnabled() {
		return httpsEnabled;
	}

	public void setHttpsEnabled(boolean httpsEnabled) {
		this.httpsEnabled = httpsEnabled;
	}

	public String getHttpsExcludedHost() {
		return httpsExcludedHost;
	}

	public void setHttpsExcludedHost(String httpsExcludedHost) {
		this.httpsExcludedHost = httpsExcludedHost;
	}

	public String getHttpsHost() {
		return httpsHost;
	}

	public void setHttpsHost(String httpsHost) {
		this.httpsHost = httpsHost;
	}

	public String getHttpsPassword() {
		return httpsPassword;
	}

	public void setHttpsPassword(String httpsPassword) {
		this.httpsPassword = httpsPassword;
	}

	public String getHttpsUser() {
		return httpsUser;
	}

	public void setHttpsUser(String httpsUser) {
		this.httpsUser = httpsUser;
	}

	public Long getHttpsPort() {
		return httpsPort;
	}

	public void setHttpsPort(Long httpsPort) {
		this.httpsPort = httpsPort;
	}
	
}
