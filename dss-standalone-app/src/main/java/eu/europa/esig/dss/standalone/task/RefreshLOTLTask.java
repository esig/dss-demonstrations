package eu.europa.esig.dss.standalone.task;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.europa.esig.dss.tsl.job.TLValidationJob;
import javafx.concurrent.Task;

public class RefreshLOTLTask extends Task<Void> {

	private static final Logger LOG = LoggerFactory.getLogger(RefreshLOTLTask.class);

	private TLValidationJob job;
	
	public RefreshLOTLTask(TLValidationJob job) {
		this.job = job;
	}
	
	@Override
	protected Void call() throws Exception {
		job.onlineRefresh();
		return null;
	}

}
