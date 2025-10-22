package eu.europa.esig.dss.web.model.serversign;

import java.io.Serializable;

public abstract class ServerSignResponseBody implements Serializable {

    private static final long serialVersionUID = 6547235492411771868L;

    private TokenId tokenId;

    protected ServerSignResponseBody() {
        // empty
    }

    public TokenId getTokenId() {
        return tokenId;
    }

    public void setTokenId(TokenId tokenId) {
        this.tokenId = tokenId;
    }

}
