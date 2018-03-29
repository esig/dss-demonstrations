package eu.europa.esig.dss.web;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import eu.europa.esig.dss.DSSUtils;
import eu.europa.esig.dss.validation.CertificateQualification;
import eu.europa.esig.dss.validation.CertificateValidator;
import eu.europa.esig.dss.validation.CertificateVerifier;
import eu.europa.esig.dss.validation.reports.CertificateReports;
import eu.europa.esig.dss.validation.reports.wrapper.DiagnosticData;
import eu.europa.esig.dss.web.config.DSSBeanConfig;
import eu.europa.esig.dss.x509.CertificateToken;

@WebAppConfiguration
@ContextConfiguration(classes = { DSSBeanConfig.class })
@RunWith(SpringJUnit4ClassRunner.class)
public class MultiThreadsCertificateValidatorStressApp {

	private static final Logger LOG = LoggerFactory.getLogger(MultiThreadsCertificateValidatorStressApp.class);

	@Autowired
	private CertificateVerifier certificateVerifier;

	@Test
	public void test() throws InterruptedException, ExecutionException {

		ExecutorService executor = Executors.newFixedThreadPool(100);

		List<Future<CertificateReports>> futures = new ArrayList<Future<CertificateReports>>();

		int nbReq = 1000;
		long startNanoTime = System.nanoTime();

		for (int i = 0; i < nbReq; i++) {
			futures.add(executor.submit(new TestConcurrent(DSSUtils.loadCertificate(new File("src/test/resources/CZ.cer")))));
		}

		for (Future<CertificateReports> future : futures) {
			CertificateReports certificateReports = future.get();
			assertNotNull(certificateReports);
			DiagnosticData diagnosticData = certificateReports.getDiagnosticData();
			assertNotNull(diagnosticData);
			assertEquals(CertificateQualification.QCERT_FOR_ESIG_QSCD, certificateReports.getSimpleReport().getQualificationAtCertificateIssuance());
		}

		long endNanoTime = System.nanoTime();
		long totalTime = endNanoTime - startNanoTime;

		LOG.info("Time : {} ns", totalTime);
		LOG.info("Time/req: {} ns", totalTime / nbReq);

		executor.shutdown();

	}

	class TestConcurrent implements Callable<CertificateReports> {

		private final CertificateToken certificate;

		public TestConcurrent(CertificateToken certificate) {
			this.certificate = certificate;
		}

		@Override
		public CertificateReports call() throws Exception {
			CertificateValidator cv = CertificateValidator.fromCertificate(certificate);
			cv.setCertificateVerifier(certificateVerifier);
			return cv.validate();
		}

	}

}
