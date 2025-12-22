package eu.europa.esig.dss.standalone.controller;

import eu.europa.esig.dss.enumerations.SignatureProfile;
import eu.europa.esig.dss.model.DSSDocument;
import eu.europa.esig.dss.standalone.fx.CollectionFilesToStringConverter;
import eu.europa.esig.dss.standalone.fx.DSSFileChooser;
import eu.europa.esig.dss.standalone.fx.DSSFileChooserLoader;
import eu.europa.esig.dss.standalone.fx.FileToStringConverter;
import eu.europa.esig.dss.standalone.model.ExtensionModel;
import eu.europa.esig.dss.standalone.source.PropertyReader;
import eu.europa.esig.dss.standalone.source.TLValidationJobExecutor;
import eu.europa.esig.dss.standalone.task.ExtensionTask;
import eu.europa.esig.dss.utils.Utils;
import javafx.beans.binding.BooleanBinding;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class ExtensionController extends AbstractController {

    private static final Logger LOG = LoggerFactory.getLogger(ExtensionController.class);

    @FXML
    public Button signedFileSelectButton;

    @FXML
    public Button originalFilesSelectButton;

    @FXML
    public ComboBox<SignatureProfile> comboProfile;

    @FXML
    public Label warningMockTSALabel;

    @FXML
    public Button extendButton;

    @FXML
    public HBox refreshBox;

    @FXML
    public Button refreshLOTL;

    @FXML
    public Label nbCertificates;

    @FXML
    public Label warningLabel;

    private ExtensionModel model;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        model = new ExtensionModel();

        signedFileSelectButton.setOnAction(new EventHandler<>() {
            @Override
            public void handle(ActionEvent event) {
                DSSFileChooser fileChooser = DSSFileChooserLoader.getInstance().createFileChooser("File to extend");
                File fileToExtend = fileChooser.showOpenDialog(stage);
                model.setFileToExtend(fileToExtend);
            }
        });
        signedFileSelectButton.textProperty().bindBidirectional(model.fileToExtendProperty(), new FileToStringConverter());

        originalFilesSelectButton.setOnAction(new EventHandler<>() {
            @Override
            public void handle(ActionEvent event) {
                DSSFileChooser fileChooser = DSSFileChooserLoader.getInstance().createFileChooser("Original file(s)");
                List<File> files = fileChooser.showOpenMultipleDialog(stage);
                model.setOriginalDocuments(files);
            }
        });
        originalFilesSelectButton.textProperty().bindBidirectional(model.originalDocumentsProperty(), new CollectionFilesToStringConverter());

        comboProfile.valueProperty().bindBidirectional(model.signatureProfileProperty());

        comboProfile.setDisable(false);
        comboProfile.getItems().addAll(SignatureProfile.BASELINE_T, SignatureProfile.BASELINE_LT, SignatureProfile.BASELINE_LTA);
        comboProfile.setValue(SignatureProfile.BASELINE_T);

        BooleanBinding disableExtendButton = model.fileToExtendProperty().isNull()
                .or(model.signatureProfileProperty().isNull());

        extendButton.disableProperty().bind(disableExtendButton);

        extendButton.setOnAction(new EventHandler<>() {
            @Override
            public void handle(ActionEvent event) {
                final Service<DSSDocument> service = new Service<>() {
                    @Override
                    protected Task<DSSDocument> createTask() {
                        return new ExtensionTask(model, TLValidationJobExecutor.getInstance().getCertificateSources());
                    }
                };
                service.setOnSucceeded(new EventHandler<>() {
                    @Override
                    public void handle(WorkerStateEvent event) {
                        save(service.getValue());
                        extendButton.disableProperty().bind(disableExtendButton);
                    }
                });
                service.setOnFailed(new EventHandler<>() {
                    @Override
                    public void handle(WorkerStateEvent event) {
                        String errorMessage = "Oops an error occurred : " + service.getMessage();
                        LOG.error(errorMessage, service.getException());
                        Alert alert = new Alert(Alert.AlertType.ERROR, errorMessage, ButtonType.CLOSE);
                        alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
                        alert.showAndWait();
                        extendButton.disableProperty().bind(disableExtendButton);
                    }
                });

                extendButton.disableProperty().bind(service.runningProperty());
                service.start();
            }
        });

        warningMockTSALabel.setVisible(Utils.isTrue(PropertyReader.getBooleanProperty("timestamp.mock")));
    }

}
