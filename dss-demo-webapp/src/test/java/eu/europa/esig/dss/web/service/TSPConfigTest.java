package eu.europa.esig.dss.web.service;

import eu.europa.esig.dss.enumerations.DigestAlgorithm;
import eu.europa.esig.dss.enumerations.TimestampType;
import eu.europa.esig.dss.model.DSSDocument;
import eu.europa.esig.dss.model.InMemoryDocument;
import eu.europa.esig.dss.model.TimestampBinary;
import eu.europa.esig.dss.spi.DSSUtils;
import eu.europa.esig.dss.spi.x509.tsp.TSPSource;
import eu.europa.esig.dss.spi.x509.tsp.TimestampToken;
import eu.europa.esig.dss.utils.Utils;
import eu.europa.esig.dss.web.config.DSSBeanConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@WebAppConfiguration
@ContextConfiguration(classes = { DSSBeanConfig.class })
@ExtendWith(SpringExtension.class)
public class TSPConfigTest {

	@Autowired
	private TSPSource tspSource;

	@Test
	public void test() throws Exception {
		DSSDocument document = new InMemoryDocument("Hello".getBytes());
		byte[] digest = DSSUtils.digest(DigestAlgorithm.SHA256, document);
		TimestampBinary timeStampResponse = tspSource.getTimeStampResponse(DigestAlgorithm.SHA256, digest);
		assertNotNull(timeStampResponse);
		assertTrue(Utils.isArrayNotEmpty(timeStampResponse.getBytes()));
		TimestampToken timestampToken = new TimestampToken(timeStampResponse.getBytes(), TimestampType.CONTENT_TIMESTAMP);
		assertNotNull(timestampToken);
		assertTrue(timestampToken.matchData(document));
	}

}
