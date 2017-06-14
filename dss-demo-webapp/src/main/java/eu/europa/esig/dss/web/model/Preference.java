package eu.europa.esig.dss.web.model;

import javax.persistence.Entity;
import javax.persistence.Id;

import eu.europa.esig.dss.client.http.proxy.ProxyPreference;

@Entity
public class Preference implements ProxyPreference {

	@Id
	private String key;
	
	private String value;
	
	@Override
	public String getKey() {
		return key;
	}

	@Override
	public void setKey(String key) {
		this.key = key;
	}

	@Override
	public String getValue() {
		return value;
	}

	@Override
	public void setValue(String value) {
		this.value = value;
	}

}
