package eu.europa.esig.dss.standalone.task.report;

import eu.europa.esig.dss.diagnostic.DiagnosticDataFacade;
import eu.europa.esig.dss.diagnostic.jaxb.XmlDiagnosticData;
import eu.europa.esig.dss.enumerations.MimeTypeEnum;
import eu.europa.esig.dss.model.DSSDocument;
import eu.europa.esig.dss.model.InMemoryDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.Objects;

public class GenerateDiagnosticDataTask extends GenerateReportTask {

    private static final Logger LOG = LoggerFactory.getLogger(GenerateDiagnosticDataTask.class);

    private final XmlDiagnosticData diagnosticData;

    public GenerateDiagnosticDataTask(XmlDiagnosticData diagnosticData) {
        Objects.requireNonNull(diagnosticData, "Diagnostic Data cannot be null!");
        this.diagnosticData = diagnosticData;
    }

    @Override
    protected DSSDocument call() {
        LOG.debug("Generating Diagnostic Data...");
        try {
            String marshalled = DiagnosticDataFacade.newFacade().marshall(diagnosticData);
            return new InMemoryDocument(marshalled.getBytes(StandardCharsets.UTF_8), "Diagnostic Data.xml", MimeTypeEnum.XML);

        } catch (Exception e) {
            throwException("Unable to generate Diagnostic Data", e);
            return null;
        }
    }

}
