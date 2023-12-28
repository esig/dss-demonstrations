package eu.europa.esig.dss.web.service;

import eu.europa.esig.dss.enumerations.DigestAlgorithm;
import eu.europa.esig.dss.enumerations.TimestampType;
import eu.europa.esig.dss.model.DSSDocument;
import eu.europa.esig.dss.model.InMemoryDocument;
import eu.europa.esig.dss.model.TimestampBinary;
import eu.europa.esig.dss.spi.DSSUtils;
import eu.europa.esig.dss.spi.x509.tsp.TimestampToken;
import eu.europa.esig.dss.utils.Utils;
import eu.europa.esig.dss.web.DssDemoApplicationTests;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TSPConfigTest extends DssDemoApplicationTests {

	@Test
	public void test() throws Exception {
		DSSDocument document = new InMemoryDocument("Hello".getBytes());
		byte[] digest = DSSUtils.digest(DigestAlgorithm.SHA256, document);
		TimestampBinary timeStampResponse = getTspSource().getTimeStampResponse(DigestAlgorithm.SHA256, digest);
		assertNotNull(timeStampResponse);
		assertTrue(Utils.isArrayNotEmpty(timeStampResponse.getBytes()));
		TimestampToken timestampToken = new TimestampToken(timeStampResponse.getBytes(), TimestampType.CONTENT_TIMESTAMP);
		assertNotNull(timestampToken);
		assertTrue(timestampToken.matchData(document));
	}

}
