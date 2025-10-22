package eu.europa.esig.dss.standalone.controller;

import eu.europa.esig.dss.standalone.controller.validationresult.ValidationResultController;
import eu.europa.esig.dss.standalone.exception.ApplicationException;
import eu.europa.esig.dss.standalone.fx.CollectionFilesToStringConverter;
import eu.europa.esig.dss.standalone.fx.DSSFileChooser;
import eu.europa.esig.dss.standalone.fx.DSSFileChooserLoader;
import eu.europa.esig.dss.standalone.fx.FileToStringConverter;
import eu.europa.esig.dss.standalone.model.ValidationModel;
import eu.europa.esig.dss.standalone.source.TLValidationJobExecutor;
import eu.europa.esig.dss.standalone.task.ValidationTask;
import eu.europa.esig.dss.utils.Utils;
import eu.europa.esig.dss.validation.reports.Reports;
import javafx.beans.binding.BooleanBinding;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class ValidationController extends AbstractController {

    private static final Logger LOG = LoggerFactory.getLogger(ValidationController.class);

    @FXML
    public Button signedFileSelectButton;

    @FXML
    public Button originalFilesSelectButton;

    @FXML
    public Button validationPolicySelectButton;

    @FXML
    public Button cryptographicSuiteSelectButton;

    @FXML
    public HBox validationBox;

    @FXML
    public Button validateButton;

    @FXML
    public Button signingCertificateSelectButton;

    @FXML
    public Button adjunctCertificatesSelectButton;

    @FXML
    public Button evidenceRecordsSelectButton;

    @FXML
    public CheckBox userFriendlyIdentifiersSelectButton;

    @FXML
    public CheckBox semanticsSelectButton;

    @FXML
    private ValidationModel model;

    private ProgressIndicator progressValidateDocument;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        model = new ValidationModel();

        signedFileSelectButton.setOnAction(new EventHandler<>() {
            @Override
            public void handle(ActionEvent event) {
                DSSFileChooser fileChooser = DSSFileChooserLoader.getInstance().createFileChooser("File to validate");
                File fileToValidate = fileChooser.showOpenDialog(stage);
                model.setFileToValidate(fileToValidate);
            }
        });
        signedFileSelectButton.textProperty().bindBidirectional(model.fileToValidateProperty(), new FileToStringConverter());

        originalFilesSelectButton.setOnAction(new EventHandler<>() {
            @Override
            public void handle(ActionEvent event) {
                DSSFileChooser fileChooser = DSSFileChooserLoader.getInstance().createFileChooser("Original file(s)");
                List<File> files = fileChooser.showOpenMultipleDialog(stage);
                model.setOriginalDocuments(files);
            }
        });
        originalFilesSelectButton.textProperty().bindBidirectional(model.originalDocumentsProperty(), new CollectionFilesToStringConverter());

        validationPolicySelectButton.setOnAction(new EventHandler<>() {
            @Override
            public void handle(ActionEvent event) {
                DSSFileChooser fileChooser = DSSFileChooserLoader.getInstance().createFileChooser(
                        "Validation policy file", "Validation policy (*.xml)", "*.xml");
                File validationPolicy = fileChooser.showOpenDialog(stage);
                model.setValidationPolicy(validationPolicy);
            }
        });
        validationPolicySelectButton.textProperty().bindBidirectional(model.fileValidationPolicyProperty(), new FileToStringConverter());

        cryptographicSuiteSelectButton.setOnAction(new EventHandler<>() {
            @Override
            public void handle(ActionEvent event) {
                DSSFileChooser fileChooser = DSSFileChooserLoader.getInstance().createFileChooser(
                        "Cryptographic suite file", "Validation policy (*.xml)", "*.xml");
                File cryptographicSuite = fileChooser.showOpenDialog(stage);
                model.setCryptographicSuite(cryptographicSuite);
            }
        });
        cryptographicSuiteSelectButton.textProperty().bindBidirectional(model.fileCryptographicSuiteProperty(), new FileToStringConverter());

        signingCertificateSelectButton.setOnAction(new EventHandler<>() {
            @Override
            public void handle(ActionEvent event) {
                DSSFileChooser fileChooser = DSSFileChooserLoader.getInstance().createFileChooser(
                        "Signing certificate file", "Certificate (*.cer, *.der, *.pem, *.crt)",
                        "*.cer", "*.der", "*.pem", "*.crt");
                File signingCertificate = fileChooser.showOpenDialog(stage);
                model.setSigningCertificate(signingCertificate);
            }
        });
        signingCertificateSelectButton.textProperty().bindBidirectional(model.signingCertificateProperty(), new FileToStringConverter());

        adjunctCertificatesSelectButton.setOnAction(new EventHandler<>() {
            @Override
            public void handle(ActionEvent event) {
                DSSFileChooser fileChooser = DSSFileChooserLoader.getInstance().createFileChooser(
                        "Adjunct certificates", "Certificates (*.cer, *.der, *.pem, *.crt)",
                        "*.cer", "*.der", "*.pem", "*.crt");
                List<File> files = fileChooser.showOpenMultipleDialog(stage);
                model.setAdjunctCertificates(files);
            }
        });
        adjunctCertificatesSelectButton.textProperty().bindBidirectional(model.adjunctCertificatesProperty(), new CollectionFilesToStringConverter());

        evidenceRecordsSelectButton.setOnAction(new EventHandler<>() {
            @Override
            public void handle(ActionEvent event) {
                DSSFileChooser fileChooser = DSSFileChooserLoader.getInstance().createFileChooser("Evidence record(s)");
                List<File> files = fileChooser.showOpenMultipleDialog(stage);
                model.setEvidenceRecords(files);
            }
        });
        evidenceRecordsSelectButton.textProperty().bindBidirectional(model.evidenceRecordsProperty(), new CollectionFilesToStringConverter());

        userFriendlyIdentifiersSelectButton.selectedProperty().bindBidirectional(model.userFriendlyIdentifiersProperty());

        semanticsSelectButton.selectedProperty().bindBidirectional(model.semanticsProperty());

        BooleanBinding disableValidateButton = model.fileToValidateProperty().isNull();

        validateButton.disableProperty().bind(disableValidateButton);

        validateButton.setOnAction(new EventHandler<>() {
            @Override
            public void handle(ActionEvent event) {
                final Service<Reports> service = new Service<>() {
                    @Override
                    protected Task<Reports> createTask() {
                        return new ValidationTask(model, TLValidationJobExecutor.getInstance().getCertificateSources());
                    }
                };

                service.setOnRunning(new EventHandler<>() {
                    @Override
                    public void handle(WorkerStateEvent event) {
                        addLoader();
                    }
                });

                service.setOnSucceeded(new EventHandler<>() {
                    @Override
                    public void handle(WorkerStateEvent event) {
                        removeLoader();
                        try {
                            Reports value = service.getValue();

                            FXMLLoader loader = new FXMLLoader();
                            loader.setLocation(getClass().getResource("/fxml/validationresult/validationResult.fxml"));

                            Stage validationStage = new Stage();
                            validationStage.setTitle(String.format("Validation result \"%s\"", value.getSimpleReport().getDocumentFilename()));
                            validationStage.getIcons().add(new Image("/dss-logo.png"));
                            validationStage.setResizable(true);

                            Scene scene;
                            if (Utils.isCollectionNotEmpty(value.getSimpleReportJaxb().getSignatureOrTimestampOrEvidenceRecord())) {
                                scene = new Scene(loader.load(), 1000, 550);
                            } else {
                                scene = new Scene(loader.load(), 150, 75);
                            }
                            scene.getStylesheets().add("/styles/style.css");
                            validationStage.setScene(scene);

                            validationStage.setX(validationBox.getScene().getWindow().getX() + 30);
                            validationStage.setY(validationBox.getScene().getWindow().getY() + 30);

                            ValidationResultController controller = loader.getController();
                            controller.setStage(validationStage);
                            controller.initialize(value);

                            validationStage.show();

                            LOG.info("Successfully validated document with name '{}'", value.getSimpleReport().getDocumentFilename());

                        } catch (Exception e) {
                            LOG.error(String.format("An error occurred: %s", e.getMessage()),  e);
                            throw new ApplicationException(e);
                        }
                    }
                });
                service.setOnFailed(new EventHandler<>() {
                    @Override
                    public void handle(WorkerStateEvent event) {
                        removeLoader();
                        String errorMessage = "Oops an error occurred : " + service.getMessage();
                        LOG.error(errorMessage, service.getException());
                        Alert alert = new Alert(Alert.AlertType.ERROR, errorMessage, ButtonType.CLOSE);
                        alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
                        alert.showAndWait();
                        validateButton.disableProperty().bind(disableValidateButton);
                    }
                });

                validateButton.disableProperty().bind(service.runningProperty());
                service.start();
            }
        });
    }

    private void removeLoader() {
        validationBox.getChildren().remove(progressValidateDocument);
    }

    private void addLoader() {
        removeLoader();
        progressValidateDocument = new ProgressIndicator();
        validationBox.getChildren().add(progressValidateDocument);
    }

}
