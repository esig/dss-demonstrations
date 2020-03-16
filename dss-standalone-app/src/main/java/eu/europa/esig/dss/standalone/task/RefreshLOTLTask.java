package eu.europa.esig.dss.standalone.task;

import eu.europa.esig.dss.tsl.job.TLValidationJob;
import javafx.concurrent.Task;

public class RefreshLOTLTask extends Task<Void> {

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
