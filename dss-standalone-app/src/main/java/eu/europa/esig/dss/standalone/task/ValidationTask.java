package eu.europa.esig.dss.standalone.task;

import eu.europa.esig.dss.model.DSSDocument;
import eu.europa.esig.dss.model.FileDocument;
import eu.europa.esig.dss.model.identifier.OriginalIdentifierProvider;
import eu.europa.esig.dss.model.policy.ValidationPolicy;
import eu.europa.esig.dss.spi.policy.SignaturePolicyProvider;
import eu.europa.esig.dss.spi.tsl.TrustedListsCertificateSource;
import eu.europa.esig.dss.spi.validation.CertificateVerifier;
import eu.europa.esig.dss.spi.x509.CertificateSource;
import eu.europa.esig.dss.spi.x509.CommonCertificateSource;
import eu.europa.esig.dss.standalone.exception.ApplicationException;
import eu.europa.esig.dss.standalone.model.ValidationModel;
import eu.europa.esig.dss.standalone.source.CertificateVerifierBuilder;
import eu.europa.esig.dss.standalone.source.DataLoaderConfigLoader;
import eu.europa.esig.dss.standalone.source.PropertyReader;
import eu.europa.esig.dss.standalone.utils.StandaloneAppUtils;
import eu.europa.esig.dss.utils.Utils;
import eu.europa.esig.dss.validation.DocumentValidator;
import eu.europa.esig.dss.validation.SignedDocumentValidator;
import eu.europa.esig.dss.validation.identifier.UserFriendlyIdentifierProvider;
import eu.europa.esig.dss.validation.policy.ValidationPolicyLoader;
import eu.europa.esig.dss.validation.reports.Reports;
import javafx.concurrent.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.List;
import java.util.stream.Collectors;

public class ValidationTask extends Task<Reports> {

    private static final Logger LOG = LoggerFactory.getLogger(ValidationTask.class);

    private final ValidationModel model;

    private final TrustedListsCertificateSource tslCertificateSource;

    public ValidationTask(ValidationModel model, TrustedListsCertificateSource tslCertificateSource) {
        this.model = model;
        this.tslCertificateSource = tslCertificateSource;
    }

    @Override
    protected Reports call() {
        try {
            FileDocument fileToValidate = new FileDocument(model.getFileToValidate());

            DocumentValidator documentValidator = SignedDocumentValidator.fromDocument(fileToValidate);

            if (model.getOriginalDocuments() != null) {
                List<DSSDocument> fileDocuments = model.getOriginalDocuments().stream().map(FileDocument::new)
                        .collect(Collectors.toList());
                documentValidator.setDetachedContents(fileDocuments);
            }

            if (model.getEvidenceRecords() != null) {
                List<DSSDocument> fileDocuments = model.getEvidenceRecords().stream().map(FileDocument::new)
                        .collect(Collectors.toList());
                documentValidator.setDetachedEvidenceRecordDocuments(fileDocuments);
            }

            if (model.getSigningCertificate() != null) {
                CommonCertificateSource signingCertificateSource = new CommonCertificateSource();
                signingCertificateSource.addCertificate(StandaloneAppUtils.toCertificateToken(model.getSigningCertificate()));
                documentValidator.setSigningCertificateSource(signingCertificateSource);
            }

            documentValidator.setTokenIdentifierProvider(model.isUserFriendlyIdentifiers() ?
                    new UserFriendlyIdentifierProvider() : new OriginalIdentifierProvider());

            documentValidator.setIncludeSemantics(model.isSemantics());

            CertificateVerifierBuilder certificateVerifierBuilder = new CertificateVerifierBuilder()
                    .setTslCertificateSource(tslCertificateSource);

            CertificateSource adjunctCertificateSource = StandaloneAppUtils.toCertificateSource(model.getAdjunctCertificates());
            if (adjunctCertificateSource != null) {
                certificateVerifierBuilder.setAdjunctCertificateSource(adjunctCertificateSource);
            }
            CertificateVerifier certificateVerifier = certificateVerifierBuilder.build();
            documentValidator.setCertificateVerifier(certificateVerifier);

            SignaturePolicyProvider signaturePolicyProvider = new SignaturePolicyProvider();
            signaturePolicyProvider.setDataLoader(DataLoaderConfigLoader.getDataLoader());
            documentValidator.setSignaturePolicyProvider(signaturePolicyProvider);

            ValidationPolicyLoader validationPolicyLoader;
            if (model.getValidationPolicy() != null) {
                validationPolicyLoader = ValidationPolicyLoader.fromValidationPolicy(model.getValidationPolicy());
            } else {
                validationPolicyLoader = fromDefaultValidationPolicy();
            }
            if (model.getCryptographicSuite() != null) {
                validationPolicyLoader = validationPolicyLoader.withCryptographicSuite(model.getCryptographicSuite());
            }

            ValidationPolicy validationPolicy = validationPolicyLoader.create();
            return documentValidator.validateDocument(validationPolicy);

        } catch (Exception e) {
            throwException("Unable to validate the document", e);
            return null;
        }
    }

    private ValidationPolicyLoader fromDefaultValidationPolicy() {
        String policyPath = PropertyReader.getProperty("default.validation.policy");
        if (Utils.isStringEmpty(policyPath)) {
            throw new IllegalArgumentException("default.validation.policy is not defined!");
        }
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(policyPath)) {
            return ValidationPolicyLoader.fromValidationPolicy(is);

        } catch (Exception e) {
            throwException("Unable to load validation policy", e);
            return null;
        }
    }

    private void throwException(String message, Exception e) {
        String exceptionMessage = message + (e != null ? " : " + e.getMessage() : "");
        updateMessage(exceptionMessage);
        failed();
        LOG.error(message, e);
        throw new ApplicationException(exceptionMessage, e);
    }

}
