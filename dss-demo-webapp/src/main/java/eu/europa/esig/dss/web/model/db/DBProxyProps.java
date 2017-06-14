package eu.europa.esig.dss.web.model.db;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity(name = "PROXY_PROPS")
public class DBProxyProps {

	@Id
	private String id;
	
	private boolean enabled;
	private String excludedHost;
	private String host;
	private String password;
	private String user;
	private Long port;
	
	public boolean isEnabled() {
		return enabled;
	}
	
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
	
	public String getExcludedHost() {
		return excludedHost;
	}
	
	public void setExcludedHost(String excludedHost) {
		this.excludedHost = excludedHost;
	}
	
	public String getHost() {
		return host;
	}
	
	public void setHost(String host) {
		this.host = host;
	}
	
	public String getPassword() {
		return password;
	}
	
	public void setPassword(String password) {
		this.password = password;
	}
	
	public String getUser() {
		return user;
	}
	
	public void setUser(String user) {
		this.user = user;
	}
	
	public Long getPort() {
		return port;
	}
	
	public void setPort(Long port) {
		this.port = port;
	}
}
