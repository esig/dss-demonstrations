package eu.europa.esig.dss.standalone.task.report;

import eu.europa.esig.dss.detailedreport.jaxb.XmlDetailedReport;
import eu.europa.esig.dss.enumerations.MimeTypeEnum;
import eu.europa.esig.dss.model.DSSDocument;
import eu.europa.esig.dss.model.InMemoryDocument;
import eu.europa.esig.dss.standalone.service.FOPService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.util.Objects;

public class GenerateDetailedReportTask extends GenerateReportTask {

    private static final Logger LOG = LoggerFactory.getLogger(GenerateDetailedReportTask.class);

    private final XmlDetailedReport detailedReport;

    public GenerateDetailedReportTask(XmlDetailedReport detailedReport) {
        Objects.requireNonNull(detailedReport, "Detailed Report cannot be null!");
        this.detailedReport = detailedReport;
    }

    @Override
    protected DSSDocument call() {
        LOG.debug("Generating Detailed Report...");
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            FOPService.getInstance().generateDetailedReport(detailedReport, baos);
            return new InMemoryDocument(baos.toByteArray(), "Detailed Report.pdf", MimeTypeEnum.PDF);

        } catch (Exception e) {
            throwException("Unable to generate Detailed Report", e);
            return null;
        }
    }

}
