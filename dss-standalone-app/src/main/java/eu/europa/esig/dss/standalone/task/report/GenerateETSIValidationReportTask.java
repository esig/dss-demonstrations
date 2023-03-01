package eu.europa.esig.dss.standalone.task.report;

import eu.europa.esig.dss.enumerations.MimeTypeEnum;
import eu.europa.esig.dss.model.DSSDocument;
import eu.europa.esig.dss.model.InMemoryDocument;
import eu.europa.esig.validationreport.ValidationReportFacade;
import eu.europa.esig.validationreport.jaxb.ValidationReportType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.Objects;

public class GenerateETSIValidationReportTask extends GenerateReportTask {

    private static final Logger LOG = LoggerFactory.getLogger(GenerateETSIValidationReportTask.class);

    private final ValidationReportType validationReport;

    public GenerateETSIValidationReportTask(ValidationReportType validationReport) {
        Objects.requireNonNull(validationReport, "Validation report cannot be null!");
        this.validationReport = validationReport;
    }

    @Override
    protected DSSDocument call() {
        LOG.debug("Generating ETSI Validation report...");
        try {
            String marshalled = ValidationReportFacade.newFacade().marshall(validationReport);
            return new InMemoryDocument(marshalled.getBytes(StandardCharsets.UTF_8), "Validation report.xml", MimeTypeEnum.XML);

        } catch (Exception e) {
            throwException("Unable to generate ETSI Validation report", e);
            return null;
        }
    }

}
