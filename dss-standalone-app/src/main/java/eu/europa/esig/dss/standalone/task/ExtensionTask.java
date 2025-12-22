package eu.europa.esig.dss.standalone.task;

import eu.europa.esig.dss.model.DSSDocument;
import eu.europa.esig.dss.model.FileDocument;
import eu.europa.esig.dss.spi.tsl.TrustedListsCertificateSource;
import eu.europa.esig.dss.standalone.exception.ApplicationException;
import eu.europa.esig.dss.standalone.model.ExtensionModel;
import eu.europa.esig.dss.standalone.service.RemoteDocumentSignatureServiceBuilder;
import eu.europa.esig.dss.utils.Utils;
import eu.europa.esig.dss.ws.converter.RemoteDocumentConverter;
import eu.europa.esig.dss.ws.dto.RemoteDocument;
import eu.europa.esig.dss.ws.signature.common.RemoteDocumentSignatureService;
import eu.europa.esig.dss.ws.signature.dto.parameters.RemoteSignatureParameters;
import javafx.concurrent.Task;

import java.util.List;
import java.util.stream.Collectors;

public class ExtensionTask extends Task<DSSDocument> {

    private final ExtensionModel model;
    private final RemoteDocumentSignatureService service;

    public ExtensionTask(ExtensionModel model, TrustedListsCertificateSource tslCertificateSource) {
        this.model = model;

        RemoteDocumentSignatureServiceBuilder signatureServiceBuilder = new RemoteDocumentSignatureServiceBuilder();
        signatureServiceBuilder.setTslCertificateSource(tslCertificateSource);
        this.service = signatureServiceBuilder.build();
    }

    @Override
    protected DSSDocument call() {
        try {
            FileDocument fileToExtend = new FileDocument(model.getFileToExtend());
            RemoteDocument toExtendDocument = RemoteDocumentConverter.toRemoteDocument(fileToExtend);

            RemoteSignatureParameters parameters = buildParameters();
            RemoteDocument extendedDocument = service.extendDocument(toExtendDocument, model.getSignatureProfile(), parameters);
            return RemoteDocumentConverter.toDSSDocument(extendedDocument);

        } catch (Exception e) {
            throwException("Unable to extend the document", e);
            return null;
        }
    }

    private RemoteSignatureParameters buildParameters() {
        RemoteSignatureParameters parameters = new RemoteSignatureParameters();
        if (Utils.isCollectionNotEmpty(model.getOriginalDocuments())) {
            List<DSSDocument> fileDocuments = model.getOriginalDocuments().stream().map(FileDocument::new).collect(Collectors.toList());
            parameters.setDetachedContents(RemoteDocumentConverter.toRemoteDocuments(fileDocuments));
        }
        return parameters;
    }

    private void throwException(String message, Exception e) {
        String exceptionMessage = message + (e != null ? " : " + e.getMessage() : "");
        updateMessage(exceptionMessage);
        failed();
        throw new ApplicationException(exceptionMessage, e);
    }

}
