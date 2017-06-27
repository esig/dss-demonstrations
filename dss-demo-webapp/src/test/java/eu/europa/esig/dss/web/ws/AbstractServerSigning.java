package eu.europa.esig.dss.web.ws;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.nio.charset.Charset;
import java.util.List;

import org.junit.Test;

import eu.europa.esig.dss.DSSUtils;
import eu.europa.esig.dss.DigestAlgorithm;
import eu.europa.esig.dss.RemoteKeyEntry;
import eu.europa.esig.dss.SignatureValue;
import eu.europa.esig.dss.ToBeSigned;
import eu.europa.esig.dss.token.RemoteSignatureTokenConnection;
import eu.europa.esig.dss.utils.Utils;

public abstract class AbstractServerSigning extends AbstractIT {

	abstract RemoteSignatureTokenConnection getRemoteToken();

	@Test
	public void testRemoteSignature() throws Exception {
		List<RemoteKeyEntry> keys = getRemoteToken().getKeys();
		assertTrue(Utils.isCollectionNotEmpty(keys));

		String alias = keys.get(0).getAlias();

		ToBeSigned toBeSigned = new ToBeSigned(DSSUtils.digest(DigestAlgorithm.SHA256, "Hello world!".getBytes(Charset.defaultCharset())));
		SignatureValue signatureValue = getRemoteToken().sign(toBeSigned, DigestAlgorithm.SHA256, alias);
		assertNotNull(signatureValue);
		assertNotNull(signatureValue.getAlgorithm());
		assertNotNull(signatureValue.getValue());

		SignatureValue signatureValue2 = getRemoteToken().sign(toBeSigned, DigestAlgorithm.SHA256, alias);
		SignatureValue signatureValue3 = getRemoteToken().sign(toBeSigned, DigestAlgorithm.SHA256, alias);
		assertEquals(signatureValue2.getAlgorithm(), signatureValue3.getAlgorithm());
		assertEquals(Utils.toBase64(signatureValue2.getValue()), Utils.toBase64(signatureValue3.getValue()));

		RemoteKeyEntry key = getRemoteToken().getKey(alias);
		assertNotNull(key);
		assertNotNull(key.getCertificate());
		assertEquals(alias, key.getAlias());

		RemoteKeyEntry key2 = getRemoteToken().getKey("bla");
		assertNull(key2);
	}

}
