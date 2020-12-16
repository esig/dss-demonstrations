package eu.europa.esig.dss.web.model;

import javax.validation.constraints.AssertTrue;

import eu.europa.esig.dss.spi.client.http.Protocol;
import eu.europa.esig.dss.utils.Utils;

public class QwacValidationForm {
	
	// the URL to be validated
	private String url;
	
    private boolean includeCertificateTokens;
    
    private boolean includeRevocationTokens;

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public boolean isIncludeCertificateTokens() {
		return includeCertificateTokens;
	}

	public void setIncludeCertificateTokens(boolean includeCertificateTokens) {
		this.includeCertificateTokens = includeCertificateTokens;
	}

	public boolean isIncludeRevocationTokens() {
		return includeRevocationTokens;
	}

	public void setIncludeRevocationTokens(boolean includeRevocationTokens) {
		this.includeRevocationTokens = includeRevocationTokens;
	}

	@AssertTrue(message = "{error.url.invalid}")
	public boolean isUrlValid() {
		return Protocol.isHttpUrl(Utils.trim(url));
	}

}
