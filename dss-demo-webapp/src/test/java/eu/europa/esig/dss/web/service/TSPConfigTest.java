package eu.europa.esig.dss.web.service;

import static org.junit.Assert.assertNotNull;

import org.bouncycastle.tsp.TimeStampToken;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import eu.europa.esig.dss.DSSUtils;
import eu.europa.esig.dss.DigestAlgorithm;
import eu.europa.esig.dss.web.config.DSSBeanConfig;
import eu.europa.esig.dss.x509.tsp.TSPSource;

@WebAppConfiguration
@ContextConfiguration(classes = { DSSBeanConfig.class })
@RunWith(SpringJUnit4ClassRunner.class)
public class TSPConfigTest {

	@Autowired
	private TSPSource tspSource;

	@Test
	public void test() {
		TimeStampToken timeStampResponse = tspSource.getTimeStampResponse(DigestAlgorithm.SHA256, DSSUtils.digest(DigestAlgorithm.SHA256, "Hello".getBytes()));
		assertNotNull(timeStampResponse);
	}
}
