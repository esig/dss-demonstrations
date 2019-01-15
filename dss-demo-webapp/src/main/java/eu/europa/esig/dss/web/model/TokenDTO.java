package eu.europa.esig.dss.web.model;

import javax.security.auth.x500.X500Principal;

import eu.europa.esig.dss.DSSASN1Utils;
import eu.europa.esig.dss.validation.reports.wrapper.CertificateWrapper;
import eu.europa.esig.dss.validation.reports.wrapper.RevocationWrapper;
import eu.europa.esig.dss.validation.reports.wrapper.TimestampWrapper;

public class TokenDTO {
	
	private String id;
	private String name;
	
	public TokenDTO(CertificateWrapper cert) {
		this.id = cert.getId();
		this.name = getHumanReadableName(cert);
	}
	
	public TokenDTO(TimestampWrapper tst) {
		this.id = tst.getId();
		this.name = tst.getType();
	}
	
	public TokenDTO(RevocationWrapper rd) {
		this.id = rd.getId();
		this.name = rd.getSource().replace("Token", "") + "_" + rd.getOrigin();
	}
	
	public String getHumanReadableName(CertificateWrapper cert) {
		X500Principal x500 = new X500Principal(cert.getSubjectDistinguishedName().get(0).getValue());
		return DSSASN1Utils.getHumanReadableName(x500);
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
