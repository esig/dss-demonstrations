package eu.europa.esig.dss.standalone.controller.validationresult;

import eu.europa.esig.dss.detailedreport.jaxb.XmlDetailedReport;
import eu.europa.esig.dss.diagnostic.jaxb.XmlDiagnosticData;
import eu.europa.esig.dss.model.DSSDocument;
import eu.europa.esig.dss.simplereport.jaxb.XmlSimpleReport;
import eu.europa.esig.dss.simplereport.jaxb.XmlToken;
import eu.europa.esig.dss.standalone.controller.AbstractController;
import eu.europa.esig.dss.standalone.source.PropertyReader;
import eu.europa.esig.dss.standalone.task.report.GenerateDetailedReportTask;
import eu.europa.esig.dss.standalone.task.report.GenerateDiagnosticDataTask;
import eu.europa.esig.dss.standalone.task.report.GenerateETSIValidationReportTask;
import eu.europa.esig.dss.standalone.task.report.GenerateReportTask;
import eu.europa.esig.dss.standalone.task.report.GenerateSimpleReportTask;
import eu.europa.esig.dss.utils.Utils;
import eu.europa.esig.dss.validation.reports.Reports;
import eu.europa.esig.validationreport.jaxb.ValidationReportType;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.List;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class ValidationResultController extends AbstractController {

    private static final Logger LOG = LoggerFactory.getLogger(ValidationResultController.class);

    @FXML
    public HBox noValidationResult;

    @FXML
    public HBox validationResultContainer;

    @FXML
    public ScrollPane validationResult;

    @FXML
    public ListView<String> signaturesList;

    @FXML
    public Label validationResultLabel;

    @FXML
    public Button simpleReportGenerate;

    @FXML
    public Button detailedReportGenerate;

    @FXML
    public Button diagnosticDataGenerate;

    @FXML
    public Button etsiVrGenerate;

    @FXML
    public Label projectVersion;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // see {@code #initialize(Reports)}
    }

    public void initialize(Reports reports) {
        Objects.requireNonNull(reports, "Reports cannot be null!");
        XmlSimpleReport simpleReport = reports.getSimpleReportJaxb();

        List<String> signatureIds = simpleReport.getSignatureOrTimestamp()
                .stream().map(XmlToken::getId).collect(Collectors.toList());
        noValidationResult.setVisible(Utils.isCollectionEmpty(signatureIds));

        if (Utils.isCollectionNotEmpty(signatureIds)) {

            ObservableList<String> observableList = FXCollections.observableArrayList();
            observableList.addAll(signatureIds);
            signaturesList.setItems(observableList);

            signaturesList.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {

                @Override
                public void changed(ObservableValue<? extends String> observableValue, String s, String t1) {
                    LOG.debug("Changed signature view to '{}'", t1);
                    for (XmlToken token : simpleReport.getSignatureOrTimestamp()) {
                        if (t1.equals(token.getId())) {
                            fillValidationResult(token);
                            break;
                        }
                    }
                }

            });

            signaturesList.getSelectionModel().select(0);

            // bind visibility
            noValidationResult.managedProperty().bind(noValidationResult.visibleProperty());

            // bind size
            Region content = (Region) validationResult.getContent();
            content.maxWidthProperty().bind(validationResult.widthProperty()
                    .subtract(validationResult.getPadding().getLeft() + validationResult.getPadding().getRight()));

            simpleReportGenerate.setOnAction(getReportCreationHandler(new GenerateSimpleReportTask(simpleReport)));

            XmlDetailedReport detailedReport = reports.getDetailedReportJaxb();
            detailedReportGenerate.setOnAction(getReportCreationHandler(new GenerateDetailedReportTask(detailedReport)));

            XmlDiagnosticData diagnosticData = reports.getDiagnosticDataJaxb();
            diagnosticDataGenerate.setOnAction(getReportCreationHandler(new GenerateDiagnosticDataTask(diagnosticData)));

            ValidationReportType validationReport = reports.getEtsiValidationReportJaxb();
            etsiVrGenerate.setOnAction(getReportCreationHandler(new GenerateETSIValidationReportTask(validationReport)));

            projectVersion.setText(projectVersion.getText() + PropertyReader.getProperty("project.version"));
        }
    }

    private void fillValidationResult(XmlToken token) {
        validationResultContainer.setVisible(true);

        TokenNodeController controller = TokenNodeController.loadController();
        Node signatureNode = controller.create(token);
        validationResult.setContent(signatureNode);
    }

    private EventHandler<ActionEvent> getReportCreationHandler(GenerateReportTask generateReportTask) {
        return new EventHandler<ActionEvent>() {

            private DSSDocument document;

            private ProgressIndicator progressIndicator;

            @Override
            public void handle(ActionEvent actionEvent) {
                Region button = (Region) actionEvent.getSource();
                Pane parent = (Pane) button.getParent();

                final Service<DSSDocument> service = new Service<>() {
                    @Override
                    protected Task<DSSDocument> createTask() {
                        return generateReportTask;
                    }
                };

                service.setOnRunning(new EventHandler<WorkerStateEvent>() {
                    @Override
                    public void handle(WorkerStateEvent event) {
                        parent.getChildren().remove(progressIndicator);
                        progressIndicator = new ProgressIndicator();
                        progressIndicator.setPrefHeight(20);
                        progressIndicator.setPrefWidth(20);
                        progressIndicator.setPadding(new Insets(0, 0, 0, 0));
                        progressIndicator.setStyle("-fx-border-color:black; -fx-border-width: 1; -fx-border-style: solid;");
                        parent.getChildren().add(progressIndicator);
                    }
                });

                service.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
                    @Override
                    public void handle(WorkerStateEvent event) {
                        parent.getChildren().remove(progressIndicator);
                        button.disableProperty().unbind();
                        button.setDisable(false);
                        if (document == null) {
                            document = service.getValue();
                        }
                        save(document);
                    }
                });

                service.setOnFailed(new EventHandler<WorkerStateEvent>() {
                    @Override
                    public void handle(WorkerStateEvent event) {
                        parent.getChildren().remove(progressIndicator);
                        button.disableProperty().unbind();
                        button.setDisable(false);
                        String errorMessage = "Oops an error occurred : " + service.getMessage();
                        LOG.error(errorMessage, service.getException());
                        Alert alert = new Alert(Alert.AlertType.ERROR, errorMessage, ButtonType.CLOSE);
                        alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
                        alert.showAndWait();
                    }
                });

                button.disableProperty().bind(service.runningProperty());
                service.start();
            }
        };
    }

}
