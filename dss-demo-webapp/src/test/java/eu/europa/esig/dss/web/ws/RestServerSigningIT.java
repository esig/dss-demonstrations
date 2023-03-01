package eu.europa.esig.dss.web.ws;


import eu.europa.esig.dss.enumerations.DigestAlgorithm;
import eu.europa.esig.dss.enumerations.MaskGenerationFunction;
import eu.europa.esig.dss.enumerations.SignatureAlgorithm;
import eu.europa.esig.dss.model.x509.CertificateToken;
import eu.europa.esig.dss.spi.DSSUtils;
import eu.europa.esig.dss.utils.Utils;
import eu.europa.esig.dss.web.config.CXFConfig;
import eu.europa.esig.dss.ws.dto.DigestDTO;
import eu.europa.esig.dss.ws.dto.SignatureValueDTO;
import eu.europa.esig.dss.ws.dto.ToBeSignedDTO;
import eu.europa.esig.dss.ws.server.signing.dto.RemoteKeyEntry;
import eu.europa.esig.dss.ws.server.signing.rest.client.RestSignatureTokenConnection;
import org.apache.cxf.ext.logging.LoggingInInterceptor;
import org.apache.cxf.ext.logging.LoggingOutInterceptor;
import org.apache.cxf.jaxrs.client.JAXRSClientFactoryBean;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.charset.Charset;
import java.security.GeneralSecurityException;
import java.security.Signature;
import java.security.spec.MGF1ParameterSpec;
import java.security.spec.PSSParameterSpec;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class RestServerSigningIT extends AbstractRestIT {
	
	private RestSignatureTokenConnection remoteToken;
	
	@BeforeEach
	public void init() {
		JAXRSClientFactoryBean factory = new JAXRSClientFactoryBean();

		factory.setAddress(getBaseCxf() + CXFConfig.REST_SERVER_SIGNING);
		factory.setServiceClass(RestSignatureTokenConnection.class);
		factory.setProviders(Arrays.asList(jacksonJsonProvider()));

		LoggingInInterceptor loggingInInterceptor = new LoggingInInterceptor();
		factory.getInInterceptors().add(loggingInInterceptor);
		factory.getInFaultInterceptors().add(loggingInInterceptor);

		LoggingOutInterceptor loggingOutInterceptor = new LoggingOutInterceptor();
		factory.getOutInterceptors().add(loggingOutInterceptor);
		factory.getOutFaultInterceptors().add(loggingOutInterceptor);

		remoteToken = factory.create(RestSignatureTokenConnection.class);
	}

	@Test
	public void testRemoteSignature() throws Exception {
		List<RemoteKeyEntry> keys = remoteToken.getKeys();
		assertTrue(Utils.isCollectionNotEmpty(keys));

		RemoteKeyEntry remoteKeyEntry = keys.get(0);
		String alias = remoteKeyEntry.getAlias();

		ToBeSignedDTO toBeSigned = new ToBeSignedDTO(DSSUtils.digest(DigestAlgorithm.SHA256, "Hello world!".getBytes(Charset.defaultCharset())));
		SignatureValueDTO signatureValue = remoteToken.sign(toBeSigned, DigestAlgorithm.SHA256, alias);
		assertNotNull(signatureValue);
		assertNotNull(signatureValue.getAlgorithm());
		assertNotNull(signatureValue.getValue());

		SignatureValueDTO signatureValue2 = remoteToken.sign(toBeSigned, DigestAlgorithm.SHA256, alias);
		SignatureValueDTO signatureValue3 = remoteToken.sign(toBeSigned, DigestAlgorithm.SHA256, alias);
		assertEquals(signatureValue.getAlgorithm(), signatureValue2.getAlgorithm());
		assertEquals(signatureValue2.getAlgorithm(), signatureValue3.getAlgorithm());
		assertArrayEquals(signatureValue.getValue(), signatureValue2.getValue());
		assertArrayEquals(signatureValue2.getValue(), signatureValue3.getValue());

		try {
			Signature sig = Signature.getInstance(signatureValue.getAlgorithm().getJCEId());
			CertificateToken certificateToken = DSSUtils.loadCertificate(remoteKeyEntry.getCertificate().getEncodedCertificate());
			sig.initVerify(certificateToken.getPublicKey());
			sig.update(toBeSigned.getBytes());
			assertTrue(sig.verify(signatureValue.getValue()));
		} catch (GeneralSecurityException e) {
			Assertions.fail(e.getMessage());
		}

		SignatureValueDTO signatureValue4 = remoteToken.sign(toBeSigned, SignatureAlgorithm.RSA_SHA256, alias);
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
	public void testRemoteSignatureWithMask() throws Exception {
		List<RemoteKeyEntry> keys = remoteToken.getKeys();
		assertTrue(Utils.isCollectionNotEmpty(keys));

		RemoteKeyEntry remoteKeyEntry = keys.get(0);
		String alias = remoteKeyEntry.getAlias();

		ToBeSignedDTO toBeSigned = new ToBeSignedDTO(DSSUtils.digest(DigestAlgorithm.SHA256, "Hello world!".getBytes(Charset.defaultCharset())));
		SignatureValueDTO signatureValue = remoteToken.sign(toBeSigned, DigestAlgorithm.SHA256, MaskGenerationFunction.MGF1, alias);
		assertNotNull(signatureValue);
		assertNotNull(signatureValue.getAlgorithm());
		assertNotNull(signatureValue.getValue());

		SignatureValueDTO signatureValue2 = remoteToken.sign(toBeSigned, DigestAlgorithm.SHA256, MaskGenerationFunction.MGF1, alias);
		assertEquals(signatureValue.getAlgorithm(), signatureValue2.getAlgorithm());
		assertFalse(Arrays.equals(signatureValue.getValue(), signatureValue2.getValue()));

		try {
			Signature sig = Signature.getInstance(signatureValue.getAlgorithm().getJCEId());
			CertificateToken certificateToken = DSSUtils.loadCertificate(remoteKeyEntry.getCertificate().getEncodedCertificate());
			sig.initVerify(certificateToken.getPublicKey());
			sig.setParameter(new PSSParameterSpec(DigestAlgorithm.SHA256.getJavaName(), "MGF1",
					new MGF1ParameterSpec(DigestAlgorithm.SHA256.getJavaName()), DigestAlgorithm.SHA256.getSaltLength(), 1));
			sig.update(toBeSigned.getBytes());
			assertTrue(sig.verify(signatureValue.getValue()));
		} catch (GeneralSecurityException e) {
			Assertions.fail(e.getMessage());
		}

		SignatureValueDTO signatureValue3 = remoteToken.sign(toBeSigned, SignatureAlgorithm.RSA_SSA_PSS_SHA256_MGF1, alias);
		assertEquals(signatureValue.getAlgorithm(), signatureValue3.getAlgorithm());
		assertFalse(Arrays.equals(signatureValue.getValue(), signatureValue3.getValue()));

		try {
			Signature sig = Signature.getInstance(signatureValue3.getAlgorithm().getJCEId());
			CertificateToken certificateToken = DSSUtils.loadCertificate(remoteKeyEntry.getCertificate().getEncodedCertificate());
			sig.initVerify(certificateToken.getPublicKey());
			sig.setParameter(new PSSParameterSpec(DigestAlgorithm.SHA256.getJavaName(), "MGF1",
					new MGF1ParameterSpec(DigestAlgorithm.SHA256.getJavaName()), DigestAlgorithm.SHA256.getSaltLength(), 1));
			sig.update(toBeSigned.getBytes());
			assertTrue(sig.verify(signatureValue3.getValue()));
		} catch (GeneralSecurityException e) {
			Assertions.fail(e.getMessage());
		}

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

	@Test
	public void testRemoteSignDigestWithMask() throws Exception {
		List<RemoteKeyEntry> keys = remoteToken.getKeys();
		assertTrue(Utils.isCollectionNotEmpty(keys));

		RemoteKeyEntry remoteKeyEntry = keys.get(0);
		String alias = remoteKeyEntry.getAlias();

		byte[] toBeSigned = "Hello world!".getBytes(Charset.defaultCharset());
		byte[] digest = DSSUtils.digest(DigestAlgorithm.SHA256, toBeSigned);
		DigestDTO digestDTO = new DigestDTO(DigestAlgorithm.SHA256, digest);
		SignatureValueDTO signatureValue = remoteToken.signDigest(digestDTO, MaskGenerationFunction.MGF1, alias);
		assertNotNull(signatureValue);
		assertNotNull(signatureValue.getAlgorithm());
		assertNotNull(signatureValue.getValue());

		SignatureValueDTO signatureValue2 = remoteToken.signDigest(digestDTO, MaskGenerationFunction.MGF1, alias);
		assertEquals(signatureValue.getAlgorithm(), signatureValue2.getAlgorithm());
		assertFalse(Arrays.equals(signatureValue.getValue(), signatureValue2.getValue()));

		try {
			Signature sig = Signature.getInstance(signatureValue.getAlgorithm().getJCEId());
			CertificateToken certificateToken = DSSUtils.loadCertificate(remoteKeyEntry.getCertificate().getEncodedCertificate());
			sig.initVerify(certificateToken.getPublicKey());
			sig.setParameter(new PSSParameterSpec(DigestAlgorithm.SHA256.getJavaName(), "MGF1",
					new MGF1ParameterSpec(DigestAlgorithm.SHA256.getJavaName()), DigestAlgorithm.SHA256.getSaltLength(), 1));
			sig.update(toBeSigned);
			assertTrue(sig.verify(signatureValue.getValue()));
		} catch (GeneralSecurityException e) {
			Assertions.fail(e.getMessage());
		}

		SignatureValueDTO signatureValue3 = remoteToken.signDigest(digestDTO, SignatureAlgorithm.RSA_SSA_PSS_SHA256_MGF1, alias);
		assertEquals(signatureValue.getAlgorithm(), signatureValue3.getAlgorithm());
		assertFalse(Arrays.equals(signatureValue.getValue(), signatureValue3.getValue()));

		try {
			Signature sig = Signature.getInstance(signatureValue3.getAlgorithm().getJCEId());
			CertificateToken certificateToken = DSSUtils.loadCertificate(remoteKeyEntry.getCertificate().getEncodedCertificate());
			sig.initVerify(certificateToken.getPublicKey());
			sig.setParameter(new PSSParameterSpec(DigestAlgorithm.SHA256.getJavaName(), "MGF1",
					new MGF1ParameterSpec(DigestAlgorithm.SHA256.getJavaName()), DigestAlgorithm.SHA256.getSaltLength(), 1));
			sig.update(toBeSigned);
			assertTrue(sig.verify(signatureValue3.getValue()));
		} catch (GeneralSecurityException e) {
			Assertions.fail(e.getMessage());
		}

		RemoteKeyEntry key = remoteToken.getKey(alias);
		assertNotNull(key);
		assertNotNull(key.getCertificate());
		assertEquals(alias, key.getAlias());

		RemoteKeyEntry key2 = remoteToken.getKey("bla");
		assertNull(key2);
	}

}
