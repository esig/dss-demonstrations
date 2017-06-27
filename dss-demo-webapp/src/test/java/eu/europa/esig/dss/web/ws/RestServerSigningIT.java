package eu.europa.esig.dss.web.ws;

import java.util.Arrays;

import org.apache.cxf.jaxrs.client.JAXRSClientFactory;

import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;

import eu.europa.esig.dss.token.RemoteSignatureTokenConnection;
import eu.europa.esig.dss.token.RestSignatureTokenConnection;
import eu.europa.esig.dss.web.config.CXFConfig;

public class RestServerSigningIT extends AbstractServerSigning {

	@Override
	RemoteSignatureTokenConnection getRemoteToken() {
		return JAXRSClientFactory.create(getBaseCxf() + CXFConfig.REST_SERVER_SIGNING, RestSignatureTokenConnection.class,
				Arrays.asList(new JacksonJsonProvider()));
	}

}
