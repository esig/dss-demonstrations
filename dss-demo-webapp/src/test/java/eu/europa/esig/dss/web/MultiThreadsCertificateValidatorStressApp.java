package eu.europa.esig.dss.web;

import eu.europa.esig.dss.diagnostic.DiagnosticData;
import eu.europa.esig.dss.enumerations.CertificateQualification;
import eu.europa.esig.dss.model.x509.CertificateToken;
import eu.europa.esig.dss.spi.DSSUtils;
import eu.europa.esig.dss.validation.CertificateValidator;
import eu.europa.esig.dss.validation.reports.CertificateReports;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class MultiThreadsCertificateValidatorStressApp extends DssDemoApplicationTests {

	private static final Logger LOG = LoggerFactory.getLogger(MultiThreadsCertificateValidatorStressApp.class);


	@Test
	public void test() throws InterruptedException, ExecutionException {

		ExecutorService executor = Executors.newFixedThreadPool(100);

		List<Future<CertificateReports>> futures = new ArrayList<>();

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
		public CertificateReports call() {
			CertificateValidator cv = CertificateValidator.fromCertificate(certificate);
			cv.setCertificateVerifier(getCertificateVerifier());
			return cv.validate();
		}

	}

}
