package eu.europa.esig.dss.web.model.serversign;

import eu.europa.esig.dss.enumerations.DigestAlgorithm;
import eu.europa.esig.dss.model.ToBeSigned;

import java.io.Serializable;

public class SignRequest implements Serializable {

    private static final long serialVersionUID = -7341157440337337623L;

    private DigestAlgorithm digestAlgorithm;

    private String keyId;

    private ToBeSigned toBeSigned;

    private TokenId tokenId;

    public SignRequest() {
        // empty
    }

    public DigestAlgorithm getDigestAlgorithm() {
        return digestAlgorithm;
    }

    public void setDigestAlgorithm(DigestAlgorithm digestAlgorithm) {
        this.digestAlgorithm = digestAlgorithm;
    }

    public String getKeyId() {
        return keyId;
    }

    public void setKeyId(String keyId) {
        this.keyId = keyId;
    }

    public ToBeSigned getToBeSigned() {
        return toBeSigned;
    }

    public void setToBeSigned(ToBeSigned toBeSigned) {
        this.toBeSigned = toBeSigned;
    }

    public TokenId getTokenId() {
        return tokenId;
    }

    public void setTokenId(TokenId tokenId) {
        this.tokenId = tokenId;
    }

}
