package eu.europa.esig.dss.web.ws;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;

import org.apache.cxf.ext.logging.LoggingInInterceptor;
import org.apache.cxf.ext.logging.LoggingOutInterceptor;
import org.apache.cxf.jaxrs.client.JAXRSClientFactoryBean;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.europa.esig.dss.enumerations.DigestAlgorithm;
import eu.europa.esig.dss.spi.DSSUtils;
import eu.europa.esig.dss.utils.Utils;
import eu.europa.esig.dss.web.config.CXFConfig;
import eu.europa.esig.dss.ws.dto.SignatureValueDTO;
import eu.europa.esig.dss.ws.dto.ToBeSignedDTO;
import eu.europa.esig.dss.ws.server.signing.dto.RemoteKeyEntry;
import eu.europa.esig.dss.ws.server.signing.rest.client.RestSignatureTokenConnection;

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

		String alias = keys.get(0).getAlias();

		ToBeSignedDTO toBeSigned = new ToBeSignedDTO(DSSUtils.digest(DigestAlgorithm.SHA256, "Hello world!".getBytes(Charset.defaultCharset())));
		SignatureValueDTO signatureValue = remoteToken.sign(toBeSigned, DigestAlgorithm.SHA256, alias);
		assertNotNull(signatureValue);
		assertNotNull(signatureValue.getAlgorithm());
		assertNotNull(signatureValue.getValue());

		SignatureValueDTO signatureValue2 = remoteToken.sign(toBeSigned, DigestAlgorithm.SHA256, alias);
		SignatureValueDTO signatureValue3 = remoteToken.sign(toBeSigned, DigestAlgorithm.SHA256, alias);
		assertEquals(signatureValue2.getAlgorithm(), signatureValue3.getAlgorithm());
		assertEquals(Utils.toBase64(signatureValue2.getValue()), Utils.toBase64(signatureValue3.getValue()));

		RemoteKeyEntry key = remoteToken.getKey(alias);
		assertNotNull(key);
		assertNotNull(key.getCertificate());
		assertEquals(alias, key.getAlias());

		RemoteKeyEntry key2 = remoteToken.getKey("bla");
		assertNull(key2);
	}

}
