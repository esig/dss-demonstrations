package eu.europa.esig.dss.standalone.model;

import eu.europa.esig.dss.enumerations.SignatureProfile;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

import java.io.File;
import java.util.Collection;

public class ExtensionModel {

    private ObjectProperty<File> fileToExtend = new SimpleObjectProperty<>();

    private ObjectProperty<Collection<File>> originalDocuments = new SimpleObjectProperty<>();

    private ObjectProperty<SignatureProfile> signatureProfile = new SimpleObjectProperty<>();

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

    public SignatureProfile getSignatureProfile() {
        return signatureProfile.get();
    }

    public ObjectProperty<SignatureProfile> signatureProfileProperty() {
        return signatureProfile;
    }

    public void setSignatureProfile(SignatureProfile signatureProfile) {
        this.signatureProfile.set(signatureProfile);
    }

}
