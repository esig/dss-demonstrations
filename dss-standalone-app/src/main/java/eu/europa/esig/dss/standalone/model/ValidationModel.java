package eu.europa.esig.dss.standalone.model;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

import java.io.File;
import java.util.Collection;

public class ValidationModel {

    private ObjectProperty<File> fileToValidate = new SimpleObjectProperty<>();

    private ObjectProperty<Collection<File>> originalDocuments = new SimpleObjectProperty<>();

    private ObjectProperty<File> validationPolicy = new SimpleObjectProperty<>();

    private ObjectProperty<File> signingCertificate = new SimpleObjectProperty<>();

    private ObjectProperty<Collection<File>> adjunctCertificates = new SimpleObjectProperty<>();

    private ObjectProperty<Collection<File>> evidenceRecords = new SimpleObjectProperty<>();

    private ObjectProperty<Boolean> userFriendlyIdentifiers = new SimpleObjectProperty<>(true);

    private ObjectProperty<Boolean> semantics = new SimpleObjectProperty<>(false);

    public File getFileToValidate() {
        return fileToValidate.get();
    }

    public ObjectProperty<File> fileToValidateProperty() {
        return fileToValidate;
    }

    public void setFileToValidate(File fileToValidate) {
        this.fileToValidate.set(fileToValidate);
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

    public File getValidationPolicy() {
        return validationPolicy.get();
    }

    public ObjectProperty<File> fileValidationPolicyProperty() {
        return validationPolicy;
    }

    public void setValidationPolicy(File validationPolicy) {
        this.validationPolicy.set(validationPolicy);
    }

    public File getSigningCertificate() {
        return signingCertificate.get();
    }

    public ObjectProperty<File> signingCertificateProperty() {
        return signingCertificate;
    }

    public void setSigningCertificate(File signingCertificate) {
        this.signingCertificate.set(signingCertificate);
    }

    public Collection<File> getAdjunctCertificates() {
        return adjunctCertificates.get();
    }

    public ObjectProperty<Collection<File>> adjunctCertificatesProperty() {
        return adjunctCertificates;
    }

    public void setAdjunctCertificates(Collection<File> adjunctCertificates) {
        this.adjunctCertificates.set(adjunctCertificates);
    }

    public Collection<File> getEvidenceRecords() {
        return evidenceRecords.get();
    }

    public ObjectProperty<Collection<File>> evidenceRecordsProperty() {
        return evidenceRecords;
    }

    public void setEvidenceRecords(Collection<File> evidenceRecords) {
        this.evidenceRecords.set(evidenceRecords);
    }

    public Boolean isUserFriendlyIdentifiers() {
        return userFriendlyIdentifiers.get();
    }

    public ObjectProperty<Boolean> userFriendlyIdentifiersProperty() {
        return userFriendlyIdentifiers;
    }

    public void setSigningCertificate(Boolean userFriendlyIdentifiers) {
        this.userFriendlyIdentifiers.set(userFriendlyIdentifiers);
    }

    public Boolean isSemantics() {
        return semantics.get();
    }

    public ObjectProperty<Boolean> semanticsProperty() {
        return semantics;
    }

    public void setSemantics(Boolean semantics) {
        this.semantics.set(semantics);
    }
    
}
