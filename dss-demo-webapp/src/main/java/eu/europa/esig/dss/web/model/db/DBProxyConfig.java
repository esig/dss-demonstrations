package eu.europa.esig.dss.web.model.db;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.PrimaryKeyJoinColumn;

@Entity(name="PROXY_CONFIG")
public class DBProxyConfig {
	
	@Id
	private String id;

	@OneToOne
	private DBProxyProps httpProps;
	
	@OneToOne
	private DBProxyProps httpsProps;
	
	public DBProxyProps getHttpProps() {
		return httpProps;
	}
	public void setHttpProps(DBProxyProps httpProps) {
		this.httpProps = httpProps;
	}
	public DBProxyProps getHttpsProps() {
		return httpsProps;
	}
	public void setHttpsProps(DBProxyProps httpsProps) {
		this.httpsProps = httpsProps;
	}
}
