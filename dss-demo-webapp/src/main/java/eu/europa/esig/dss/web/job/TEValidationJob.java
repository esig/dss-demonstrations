package eu.europa.esig.dss.web.job;

import eu.europa.esig.dss.lote.job.LoTEValidationJob;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service()
public class TEValidationJob {

    @Value("${cron.lote.loader.enable}")
    private boolean enable;

    @Autowired
    private LoTEValidationJob loloteJob;

    @PostConstruct
    public void init() {
        loloteJob.offlineRefresh();
    }

    @Scheduled(initialDelayString = "${cron.lote.loader.initial.delay}", fixedDelayString = "${cron.lote.loader.update.delay}")
    public void refresh() {
        if (enable) {
            loloteJob.onlineRefresh();
        }
    }

}
