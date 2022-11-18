package eu.europa.esig.dss.standalone.controller;

import eu.europa.esig.dss.enumerations.ASiCContainerType;
import eu.europa.esig.dss.enumerations.SignatureForm;
import eu.europa.esig.dss.enumerations.SignatureLevel;
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
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
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
import javafx.scene.control.RadioButton;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
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
    public RadioButton asicsRadio;

    @FXML
    public RadioButton asiceRadio;

    @FXML
    private ToggleGroup toggleAsicContainerType;

    @FXML
    public RadioButton xadesRadio;

    @FXML
    public RadioButton cadesRadio;

    @FXML
    public RadioButton padesRadio;

    @FXML
    public RadioButton jadesRadio;

    @FXML
    private ToggleGroup toogleSigFormat;

    @FXML
    public ComboBox<SignatureLevel> comboLevel;

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

        asicsRadio.setUserData(ASiCContainerType.ASiC_S);
        asiceRadio.setUserData(ASiCContainerType.ASiC_E);
        toggleAsicContainerType.selectedToggleProperty().addListener(new ChangeListener<>() {
            @Override
            public void changed(ObservableValue<? extends Toggle> observable, Toggle oldValue, Toggle newValue) {
                if (newValue != null) {
                    ASiCContainerType newContainerType = (ASiCContainerType) newValue.getUserData();
                    updateSignatureFormForASiC(newContainerType);
                } else {
                    updateSignatureFormForASiC(null);
                }
            }
        });

        cadesRadio.setUserData(SignatureForm.CAdES);
        xadesRadio.setUserData(SignatureForm.XAdES);
        padesRadio.setUserData(SignatureForm.PAdES);
        jadesRadio.setUserData(SignatureForm.JAdES);
        toogleSigFormat.selectedToggleProperty().addListener(new ChangeListener<>() {
            @Override
            public void changed(ObservableValue<? extends Toggle> observable, Toggle oldValue, Toggle newValue) {
                if (newValue != null) {
                    SignatureForm newSigForm = (SignatureForm) newValue.getUserData();
                    updateSignatureForm(newSigForm);
                } else {
                    updateSignatureForm(null);
                }
            }
        });

        comboLevel.valueProperty().bindBidirectional(model.signatureLevelProperty());

        BooleanBinding disableExtendButton = model.fileToExtendProperty().isNull()
                .or(model.signatureFormProperty().isNull()).or(model.signatureLevelProperty().isNull());

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

    protected void updateSignatureFormForASiC(ASiCContainerType newValue) {
        model.setAsicContainerType(newValue);

        reinitSignatureFormats();

        if (newValue != null) { // ASiC
            cadesRadio.setDisable(false);
            xadesRadio.setDisable(false);
        } else {
            cadesRadio.setDisable(false);
            padesRadio.setDisable(false);
            xadesRadio.setDisable(false);
            jadesRadio.setDisable(false);
        }
    }

    private void reinitSignatureFormats() {
        cadesRadio.setDisable(true);
        padesRadio.setDisable(true);
        xadesRadio.setDisable(true);
        jadesRadio.setDisable(true);

        cadesRadio.setSelected(false);
        padesRadio.setSelected(false);
        xadesRadio.setSelected(false);
        jadesRadio.setSelected(false);
    }

    private void updateSignatureForm(SignatureForm signatureForm) {
        model.setSignatureForm(signatureForm);

        comboLevel.setDisable(false);
        comboLevel.getItems().removeAll(comboLevel.getItems());

        if (signatureForm != null) {
            switch (signatureForm) {
                case CAdES:
                    comboLevel.getItems().addAll(SignatureLevel.CAdES_BASELINE_T, SignatureLevel.CAdES_BASELINE_LT, SignatureLevel.CAdES_BASELINE_LTA);
                    comboLevel.setValue(SignatureLevel.CAdES_BASELINE_T);
                    break;
                case PAdES:
                    comboLevel.getItems().addAll(SignatureLevel.PAdES_BASELINE_T, SignatureLevel.PAdES_BASELINE_LT, SignatureLevel.PAdES_BASELINE_LTA);
                    comboLevel.setValue(SignatureLevel.PAdES_BASELINE_T);
                    break;
                case XAdES:
                    comboLevel.getItems().addAll(SignatureLevel.XAdES_BASELINE_T, SignatureLevel.XAdES_BASELINE_LT, SignatureLevel.XAdES_BASELINE_LTA);
                    comboLevel.setValue(SignatureLevel.XAdES_BASELINE_T);
                    break;
                case JAdES:
                    comboLevel.getItems().addAll(SignatureLevel.JAdES_BASELINE_T, SignatureLevel.JAdES_BASELINE_LT, SignatureLevel.JAdES_BASELINE_LTA);
                    comboLevel.setValue(SignatureLevel.JAdES_BASELINE_T);
                default:
                    break;
            }
        }
    }

}
