package eu.europa.esig.dss.standalone.task.report;

import eu.europa.esig.dss.model.DSSDocument;
import eu.europa.esig.dss.standalone.exception.ApplicationException;
import javafx.concurrent.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class GenerateReportTask extends Task<DSSDocument> {

    private static final Logger LOG = LoggerFactory.getLogger(GenerateReportTask.class);

    public void throwException(String message, Exception e) {
        String exceptionMessage = message + (e != null ? " : " + e.getMessage() : "");
        updateMessage(exceptionMessage);
        failed();
        LOG.error(message, e);
        throw new ApplicationException(exceptionMessage, e);
    }

}
