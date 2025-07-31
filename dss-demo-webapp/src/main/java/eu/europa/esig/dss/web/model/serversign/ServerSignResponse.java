package eu.europa.esig.dss.web.model.serversign;

import java.io.Serializable;

public class ServerSignResponse implements Serializable {

    private static final long serialVersionUID = -4078721936595873396L;

    private ServerSignResponseBody response;

    public ServerSignResponse() {
        // empty
    }

    public ServerSignResponseBody getResponse() {
        return response;
    }

    public void setResponse(ServerSignResponseBody response) {
        this.response = response;
    }

}
