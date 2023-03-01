package eu.europa.esig.dss.standalone.model;

import eu.europa.esig.dss.enumerations.ASiCContainerType;
import eu.europa.esig.dss.enumerations.SignatureForm;
import eu.europa.esig.dss.enumerations.SignatureLevel;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

import java.io.File;
import java.util.Collection;

public class ExtensionModel {

    private ObjectProperty<File> fileToExtend = new SimpleObjectProperty<>();

    private ObjectProperty<Collection<File>> originalDocuments = new SimpleObjectProperty<>();

    private ObjectProperty<ASiCContainerType> asicContainerType = new SimpleObjectProperty<>();

    private ObjectProperty<SignatureForm> signatureForm = new SimpleObjectProperty<>();

    private ObjectProperty<SignatureLevel> signatureLevel = new SimpleObjectProperty<>();

    public File getFileToExtend() {
        return fileToExtend.get();
    }

    public ObjectProperty<File> fileToExtendProperty() {
        return fileToExtend;
    }

    public void setFileToExtend(File fileToExtend) {
        this.fileToExtend.set(fileToExtend);
    }

    public Collection<File> getOriginalDocuments() {
        return originalDocuments.get();
    }

    public ObjectProperty<Collection<File>> originalDocumentsProperty() {
        return originalDocuments;
    }

    public void setOriginalDocuments(Collection<File> originalDocuments) {
        this.originalDocuments.set(originalDocuments);
    }

    public ASiCContainerType getAsicContainerType() {
        return asicContainerType.get();
    }

    public ObjectProperty<ASiCContainerType> asicContainerTypeProperty() {
        return asicContainerType;
    }

    public void setAsicContainerType(ASiCContainerType asicContainerType) {
        this.asicContainerType.set(asicContainerType);
    }

    public SignatureForm getSignatureForm() {
        return signatureForm.get();
    }

    public ObjectProperty<SignatureForm> signatureFormProperty() {
        return signatureForm;
    }

    public void setSignatureForm(SignatureForm signatureForm) {
        this.signatureForm.set(signatureForm);
    }

    public SignatureLevel getSignatureLevel() {
        return signatureLevel.get();
    }

    public ObjectProperty<SignatureLevel> signatureLevelProperty() {
        return signatureLevel;
    }

    public void setSignatureLevel(SignatureLevel signatureLevel) {
        this.signatureLevel.set(signatureLevel);
    }
}
