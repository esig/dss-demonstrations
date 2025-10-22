package eu.europa.esig.dss.standalone.controller;

import eu.europa.esig.dss.standalone.source.PropertyReader;
import eu.europa.esig.dss.standalone.source.TLValidationJobExecutor;
import eu.europa.esig.dss.standalone.task.RefreshLOTLTask;
import eu.europa.esig.dss.tsl.job.TLValidationJob;
import eu.europa.esig.dss.utils.Utils;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TabPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.util.ResourceBundle;

public class MainController extends AbstractController {

    public static final String NUMBER_OF_TRUSTED_CERTIFICATES = "Number of Trusted Certificates : ";

    @FXML
    public TabPane tabPane;

    @FXML
    private VBox signatureTab;

    @FXML
    private SignatureController signatureTabController;

    @FXML
    private VBox extensionTab;

    @FXML
    private ExtensionController extensionTabController;

    @FXML
    private VBox validationTab;

    @FXML
    private ValidationController validationTabController;

    @FXML
    private Button refreshLOTL;

    @FXML
    private HBox refreshBox;

    @FXML
    private Label nbCertificates;

    @FXML
    private Label warningLabel;

    private ProgressIndicator progressRefreshLOTL;

    private TLValidationJobExecutor jobBuilder;

    private TLValidationJob tlValidationJob;

    static {
        // Fix a freeze in Windows 10, JDK 8 and touchscreen
        System.setProperty("glass.accessible.force", "false");

        String bcRsaValidation = PropertyReader.getProperty("bc.rsa.max_mr_tests");
        if (Utils.isStringNotEmpty(bcRsaValidation)) {
            System.setProperty("org.bouncycastle.rsa.max_mr_tests", bcRsaValidation);
        }
        String bcAllowWrongOidEncoding = PropertyReader.getProperty("bc.allow.wrong.oid.enc");
        if (Utils.isStringNotEmpty(bcAllowWrongOidEncoding)) {
            System.setProperty("org.bouncycastle.asn1.allow_wrong_oid_enc", bcAllowWrongOidEncoding);
        }

        String xmlsecManifestMaxRefsCount = PropertyReader.getProperty("xmlsec.manifest.max.references");
        if (Utils.isStringNotEmpty(xmlsecManifestMaxRefsCount)) {
            System.setProperty("org.apache.xml.security.maxReferences", xmlsecManifestMaxRefsCount);
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        signatureTabController.setStage(stage);
        extensionTabController.setStage(stage);
        validationTabController.setStage(stage);

        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tabPane.getSelectionModel().selectFirst();

        //Create JobBuilder && TLValidationJob
        jobBuilder = TLValidationJobExecutor.getInstance();
        tlValidationJob = jobBuilder.job();
        tlValidationJob.offlineRefresh();
        warningLabel.setVisible(false);
        updateLabelText();

        refreshLOTL.setOnAction(event -> {
            final RefreshLOTLTask task = new RefreshLOTLTask(tlValidationJob);
            task.setOnRunning(new EventHandler<>() {
                @Override
                public void handle(WorkerStateEvent event) {
                    warningLabel.setVisible(false);
                    addLoader();
                }
            });

            task.setOnSucceeded(new EventHandler<>() {
                @Override
                public void handle(WorkerStateEvent event) {
                    removeLoader();
                    updateLabelText();
                }
            });

            task.setOnFailed(new EventHandler<>() {
                @Override
                public void handle(WorkerStateEvent event) {
                    removeLoader();
                    warningLabel.setVisible(true);
                }
            });

            //start Task
            Thread readValThread = new Thread(task);
            readValThread.setDaemon(true);
            readValThread.start();
        });
    }

    private void updateLabelText() {
        nbCertificates.setText(MainController.NUMBER_OF_TRUSTED_CERTIFICATES + jobBuilder.getCertificateSources().getNumberOfCertificates());
    }

    private void removeLoader() {
        refreshBox.getChildren().remove(progressRefreshLOTL);
    }

    private void addLoader() {
        removeLoader();
        progressRefreshLOTL = new ProgressIndicator();
        refreshBox.getChildren().add(progressRefreshLOTL);
    }

}
