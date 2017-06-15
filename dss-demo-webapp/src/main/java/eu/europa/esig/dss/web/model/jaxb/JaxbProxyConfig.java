package eu.europa.esig.dss.web.model.jaxb;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class JaxbProxyConfig {

	private JaxbProxyProps httpProps;
	
	private JaxbProxyProps httpsProps;

	public JaxbProxyProps getHttpProps() {
		return httpProps;
	}

	public void setHttpProps(JaxbProxyProps httpProps) {
		this.httpProps = httpProps;
	}

	public JaxbProxyProps getHttpsProps() {
		return httpsProps;
	}

	public void setHttpsProps(JaxbProxyProps httpsProps) {
		this.httpsProps = httpsProps;
	}
	
}
