package eu.europa.esig.dss.web.ws;

import eu.europa.esig.dss.enumerations.DigestAlgorithm;
import eu.europa.esig.dss.enumerations.SignatureAlgorithm;
import eu.europa.esig.dss.model.x509.CertificateToken;
import eu.europa.esig.dss.spi.DSSUtils;
import eu.europa.esig.dss.utils.Utils;
import eu.europa.esig.dss.web.config.CXFConfig;
import eu.europa.esig.dss.ws.dto.DigestDTO;
import eu.europa.esig.dss.ws.dto.SignatureValueDTO;
import eu.europa.esig.dss.ws.server.signing.dto.RemoteKeyEntry;
import eu.europa.esig.dss.ws.server.signing.soap.client.SoapSignatureTokenConnection;
import org.apache.cxf.ext.logging.LoggingInInterceptor;
import org.apache.cxf.ext.logging.LoggingOutInterceptor;
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.charset.Charset;
import java.security.GeneralSecurityException;
import java.security.Signature;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SoapServerSigningIT extends AbstractIT {

	private SoapSignatureTokenConnection remoteToken;

	@BeforeEach
	public void init() {
		JaxWsProxyFactoryBean factory = new JaxWsProxyFactoryBean();
		factory.setServiceClass(SoapSignatureTokenConnection.class);

		Map<String, Object> props = new HashMap<>();
		props.put("mtom-enabled", Boolean.TRUE);
		factory.setProperties(props);

		factory.setAddress(getBaseCxf() + CXFConfig.SOAP_SERVER_SIGNING);

		LoggingInInterceptor loggingInInterceptor = new LoggingInInterceptor();
		factory.getInInterceptors().add(loggingInInterceptor);
		factory.getInFaultInterceptors().add(loggingInInterceptor);

		LoggingOutInterceptor loggingOutInterceptor = new LoggingOutInterceptor();
		factory.getOutInterceptors().add(loggingOutInterceptor);
		factory.getOutFaultInterceptors().add(loggingOutInterceptor);

		remoteToken = factory.create(SoapSignatureTokenConnection.class);
	}

	@Test
	public void testRemoteSignature() throws Exception {
		List<RemoteKeyEntry> keys = remoteToken.getKeys();
		assertTrue(Utils.isCollectionNotEmpty(keys));

		RemoteKeyEntry remoteKeyEntry = keys.get(0);
		String alias = remoteKeyEntry.getAlias();

		byte[] toBeSigned = "Hello world!".getBytes(Charset.defaultCharset());
		byte[] digest = DSSUtils.digest(DigestAlgorithm.SHA256, toBeSigned);
		digest = DSSUtils.encodeRSADigest(DigestAlgorithm.SHA256, digest);
		DigestDTO digestDTO = new DigestDTO(DigestAlgorithm.SHA256, digest);
		SignatureValueDTO signatureValue = remoteToken.signDigest(digestDTO, alias);
		assertNotNull(signatureValue);
		assertNotNull(signatureValue.getAlgorithm());
		assertNotNull(signatureValue.getValue());

		SignatureValueDTO signatureValue2 = remoteToken.signDigest(digestDTO, alias);
		SignatureValueDTO signatureValue3 = remoteToken.signDigest(digestDTO, alias);
		assertEquals(signatureValue.getAlgorithm(), signatureValue2.getAlgorithm());
		assertEquals(signatureValue2.getAlgorithm(), signatureValue3.getAlgorithm());
		assertArrayEquals(signatureValue.getValue(), signatureValue2.getValue());
		assertArrayEquals(signatureValue2.getValue(), signatureValue3.getValue());

		try {
			Signature sig = Signature.getInstance(signatureValue.getAlgorithm().getJCEId());
			CertificateToken certificateToken = DSSUtils.loadCertificate(remoteKeyEntry.getCertificate().getEncodedCertificate());
			sig.initVerify(certificateToken.getPublicKey());
			sig.update(toBeSigned);
			assertTrue(sig.verify(signatureValue.getValue()));
		} catch (GeneralSecurityException e) {
			Assertions.fail(e.getMessage());
		}

		SignatureValueDTO signatureValue4 = remoteToken.signDigest(digestDTO, SignatureAlgorithm.RSA_SHA256, alias);
		assertEquals(signatureValue.getAlgorithm(), signatureValue4.getAlgorithm());
		assertArrayEquals(signatureValue.getValue(), signatureValue4.getValue());

		RemoteKeyEntry key = remoteToken.getKey(alias);
		assertNotNull(key);
		assertNotNull(key.getCertificate());
		assertEquals(alias, key.getAlias());

		RemoteKeyEntry key2 = remoteToken.getKey("bla");
		assertNull(key2);
	}

	@Test
	public void testRemoteSignDigest() throws Exception {
		List<RemoteKeyEntry> keys = remoteToken.getKeys();
		assertTrue(Utils.isCollectionNotEmpty(keys));

		RemoteKeyEntry remoteKeyEntry = keys.get(0);
		String alias = remoteKeyEntry.getAlias();

		byte[] toBeSigned = "Hello world!".getBytes(Charset.defaultCharset());
		byte[] digest = DSSUtils.digest(DigestAlgorithm.SHA256, toBeSigned);
		digest = DSSUtils.encodeRSADigest(DigestAlgorithm.SHA256, digest);
		DigestDTO digestDTO = new DigestDTO(DigestAlgorithm.SHA256, digest);
		SignatureValueDTO signatureValue = remoteToken.signDigest(digestDTO, alias);
		assertNotNull(signatureValue);
		assertNotNull(signatureValue.getAlgorithm());
		assertNotNull(signatureValue.getValue());

		SignatureValueDTO signatureValue2 = remoteToken.signDigest(digestDTO, alias);
		SignatureValueDTO signatureValue3 = remoteToken.signDigest(digestDTO, alias);
		assertEquals(signatureValue.getAlgorithm(), signatureValue2.getAlgorithm());
		assertEquals(signatureValue2.getAlgorithm(), signatureValue3.getAlgorithm());
		assertArrayEquals(signatureValue.getValue(), signatureValue2.getValue());
		assertArrayEquals(signatureValue2.getValue(), signatureValue3.getValue());

		try {
			Signature sig = Signature.getInstance(signatureValue.getAlgorithm().getJCEId());
			CertificateToken certificateToken = DSSUtils.loadCertificate(remoteKeyEntry.getCertificate().getEncodedCertificate());
			sig.initVerify(certificateToken.getPublicKey());
			sig.update(toBeSigned);
			assertTrue(sig.verify(signatureValue.getValue()));
		} catch (GeneralSecurityException e) {
			Assertions.fail(e.getMessage());
		}

		SignatureValueDTO signatureValue4 = remoteToken.signDigest(digestDTO, SignatureAlgorithm.RSA_SHA256, alias);
		assertEquals(signatureValue.getAlgorithm(), signatureValue4.getAlgorithm());
		assertArrayEquals(signatureValue.getValue(), signatureValue4.getValue());

		RemoteKeyEntry key = remoteToken.getKey(alias);
		assertNotNull(key);
		assertNotNull(key.getCertificate());
		assertEquals(alias, key.getAlias());

		RemoteKeyEntry key2 = remoteToken.getKey("bla");
		assertNull(key2);
	}

}
