package eu.europa.esig.dss.web.ws;

import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import eu.europa.esig.dss.token.RemoteSignatureTokenConnection;
import eu.europa.esig.dss.token.SoapSignatureTokenConnection;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "/test-server-signing-soap-context.xml")
public class SoapServerSigningIT extends AbstractServerSigning {

	@Autowired
	private SoapSignatureTokenConnection remoteToken;

	@Override
	RemoteSignatureTokenConnection getRemoteToken() {
		return remoteToken;
	}

}
