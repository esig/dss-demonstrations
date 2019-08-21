package eu.europa.esig.dss.web.model;

import eu.europa.esig.dss.diagnostic.AbstractTokenProxy;
import eu.europa.esig.dss.diagnostic.CertificateWrapper;
import eu.europa.esig.dss.diagnostic.RevocationWrapper;
import eu.europa.esig.dss.diagnostic.TimestampWrapper;

public class TokenDTO {

	private final String id;
	private final String name;

	public TokenDTO(AbstractTokenProxy wrapper) {
		this.id = wrapper.getId();
		if (wrapper instanceof CertificateWrapper) {
			CertificateWrapper cert = (CertificateWrapper) wrapper;
			this.name = cert.getReadableCertificateName();
		} else if (wrapper instanceof TimestampWrapper) {
			TimestampWrapper tst = (TimestampWrapper) wrapper;
			this.name = tst.getType().name();
		} else if (wrapper instanceof RevocationWrapper) {
			RevocationWrapper rd = (RevocationWrapper) wrapper;
			this.name = rd.getRevocationType().name().replace("Token", "") + "_" + rd.getOrigin();
		} else {
			throw new IllegalArgumentException("Unsupported argument " + wrapper.getClass().getSimpleName());
		}
	}

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}

}
