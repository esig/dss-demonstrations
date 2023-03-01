package eu.europa.esig.dss.standalone.task.report;

import eu.europa.esig.dss.enumerations.MimeTypeEnum;
import eu.europa.esig.dss.model.DSSDocument;
import eu.europa.esig.dss.model.InMemoryDocument;
import eu.europa.esig.dss.simplereport.jaxb.XmlSimpleReport;
import eu.europa.esig.dss.standalone.service.FOPService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.util.Objects;

public class GenerateSimpleReportTask extends GenerateReportTask {

    private static final Logger LOG = LoggerFactory.getLogger(GenerateSimpleReportTask.class);

    private final XmlSimpleReport simpleReport;

    public GenerateSimpleReportTask(XmlSimpleReport simpleReport) {
        Objects.requireNonNull(simpleReport, "Simple Report cannot be null!");
        this.simpleReport = simpleReport;
    }

    @Override
    protected DSSDocument call() {
        LOG.debug("Generating Simple Report...");
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            FOPService.getInstance().generateSimpleReport(simpleReport, baos);
            return new InMemoryDocument(baos.toByteArray(), "Simple Report.pdf", MimeTypeEnum.PDF);

        } catch (Exception e) {
            throwException("Unable to generate Simple Report", e);
            return null;
        }
    }

}
