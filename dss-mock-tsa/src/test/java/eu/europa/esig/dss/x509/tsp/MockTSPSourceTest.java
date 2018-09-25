package eu.europa.esig.dss.x509.tsp;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.IOException;
import java.security.KeyStore.PasswordProtection;

import org.bouncycastle.tsp.TimeStampToken;
import org.junit.Test;

import eu.europa.esig.dss.DSSUtils;
import eu.europa.esig.dss.DigestAlgorithm;
import eu.europa.esig.dss.token.KeyStoreSignatureTokenConnection;

public class MockTSPSourceTest {

	@Test
	public void test() throws IOException {

		MockTSPSource mock = new MockTSPSource();

		KeyStoreSignatureTokenConnection token = new KeyStoreSignatureTokenConnection(new File("src/test/resources/self-signed-tsa.p12"), "PKCS12",
				new PasswordProtection("ks-password".toCharArray()));
		mock.setToken(token);
		mock.setAlias("self-signed-tsa");

		byte[] digest = DSSUtils.digest(DigestAlgorithm.SHA256, "Hello".getBytes());
		TimeStampToken timeStampResponse = mock.getTimeStampResponse(DigestAlgorithm.SHA256, digest);

		assertNotNull(timeStampResponse);
		assertArrayEquals(digest, timeStampResponse.getTimeStampInfo().getMessageImprintDigest());

	}

}
