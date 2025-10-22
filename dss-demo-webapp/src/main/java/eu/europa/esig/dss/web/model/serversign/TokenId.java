package eu.europa.esig.dss.web.model.serversign;

import java.io.Serializable;
import java.util.UUID;

public class TokenId implements Serializable {

    private static final long serialVersionUID = -3308156629332632613L;

    private String id;

    public TokenId() {
        id = UUID.randomUUID().toString();
    }

    public TokenId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

}
