package eu.europa.esig.dss.web.ws;

import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import eu.europa.esig.dss.token.RemoteSignatureTokenConnection;
import eu.europa.esig.dss.token.RestSignatureTokenConnection;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "/test-server-signing-rest-context.xml")
public class RestServerSigningIT extends AbstractServerSigning {

	@Autowired
	private RestSignatureTokenConnection remoteToken;

	@Override
	RemoteSignatureTokenConnection getRemoteToken() {
		return remoteToken;
	}

}
