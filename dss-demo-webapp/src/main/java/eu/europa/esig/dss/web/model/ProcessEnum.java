package eu.europa.esig.dss.web.model;

public enum ProcessEnum {
	
	SIGNATURE(true, false),

	SIGNATURE_SERVER_SIGN(true, true),
	
	EXTENSION(false, false),
	
	DIGEST_SIGN(true, false),

	DIGEST_SIGN_SERVER_SIGN(true, true),
	
    COUNTER_SIGN(true, false),

	COUNTER_SIGN_SERVER_SIGN(true, true);

	private final boolean sign;

	private final boolean serverSign;

	ProcessEnum(final boolean sign, final boolean serverSign) {
		this.sign = sign;
		this.serverSign = serverSign;
	}

	public boolean isSign() {
		return sign;
	}

	public boolean isServerSign() {
		return serverSign;
	}

}
