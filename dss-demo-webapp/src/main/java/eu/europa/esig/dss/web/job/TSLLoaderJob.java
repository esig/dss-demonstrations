package eu.europa.esig.dss.web.job;

import eu.europa.esig.dss.tsl.job.TLValidationJob;
import eu.europa.esig.dss.utils.Utils;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class TSLLoaderJob {

	@Value("${cron.tl.loader.enable}")
	private boolean enable;

	@Value("${bc.rsa.max_mr_tests:}")
	private String bcRsaValidation;

	@Autowired
	private TLValidationJob job;

	@PostConstruct
	public void init() {
		if (Utils.isStringNotEmpty(bcRsaValidation)) {
			System.setProperty("org.bouncycastle.rsa.max_mr_tests", bcRsaValidation);
		}
		job.offlineRefresh();
	}

	@Scheduled(initialDelayString = "${cron.initial.delay.tl.loader}", fixedDelayString = "${cron.delay.tl.loader}")
	public void refresh() {
		if (enable) {
			job.onlineRefresh();
		}
	}

}
