package eu.europa.esig.dss.standalone.controller.validationresult;

import eu.europa.esig.dss.enumerations.Indication;
import eu.europa.esig.dss.simplereport.jaxb.XmlCertificate;
import eu.europa.esig.dss.simplereport.jaxb.XmlCertificateChain;
import eu.europa.esig.dss.simplereport.jaxb.XmlDetails;
import eu.europa.esig.dss.simplereport.jaxb.XmlEvidenceRecord;
import eu.europa.esig.dss.simplereport.jaxb.XmlEvidenceRecords;
import eu.europa.esig.dss.simplereport.jaxb.XmlMessage;
import eu.europa.esig.dss.simplereport.jaxb.XmlSignature;
import eu.europa.esig.dss.simplereport.jaxb.XmlSignatureScope;
import eu.europa.esig.dss.simplereport.jaxb.XmlTimestamp;
import eu.europa.esig.dss.simplereport.jaxb.XmlTimestamps;
import eu.europa.esig.dss.simplereport.jaxb.XmlToken;
import eu.europa.esig.dss.simplereport.jaxb.XmlTrustAnchor;
import eu.europa.esig.dss.simplereport.jaxb.XmlTrustAnchors;
import eu.europa.esig.dss.spi.DSSUtils;
import eu.europa.esig.dss.standalone.controller.AbstractController;
import eu.europa.esig.dss.standalone.exception.ApplicationException;
import eu.europa.esig.dss.utils.Utils;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;

public class TokenNodeController extends AbstractController {

    private static final Logger LOG = LoggerFactory.getLogger(TokenNodeController.class);

    @FXML
    public HBox parentNode;

    @FXML
    public Label signatureIdLabel;

    @FXML
    public Label timestampIdLabel;

    @FXML
    public Label evidenceRecordIdLabel;

    @FXML
    public Label id;

    @FXML
    public HBox filenameContainer;

    @FXML
    public Label filename;

    @FXML
    public HBox qualificationLevelContainer;

    @FXML
    public Label qualificationLevel;

    @FXML
    public HBox qualificationDetailsContainer;

    @FXML
    public TextFlow qualificationDetails;

    @FXML
    public Label indication;

    @FXML
    public HBox adesValidationDetailsContainer;

    @FXML
    public TextFlow adesValidationDetails;

    @FXML
    public HBox signatureFormatContainer;

    @FXML
    public Label signatureFormat;

    @FXML
    public HBox certificateChainContainer;

    @FXML
    public TextFlow certificateChain;

    @FXML
    public HBox claimedSigningTimeContainer;

    @FXML
    public Label claimedSigningTime;

    @FXML
    public HBox bestSignatureTimeContainer;

    @FXML
    public Label bestSignatureTime;

    @FXML
    public HBox productionTimeContainer;

    @FXML
    public Label productionTime;

    @FXML
    public HBox lowestPOEContainer;

    @FXML
    public Label lowestPOE;

    @FXML
    public HBox signatureScopesContainer;

    @FXML
    public TextFlow signatureScopes;

    @FXML
    public HBox signatureTimestampsContainer;

    @FXML
    public VBox signatureTimestamps;

    @FXML
    public HBox signatureEvidenceRecordsContainer;

    @FXML
    public VBox signatureEvidenceRecords;

    public static TokenNodeController loadController() {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(TokenNodeController.class.getResource("/fxml/validationresult/token.fxml"));
            loader.load();

            return loader.getController();

        } catch (Exception e) {
            LOG.error(String.format("An error occurred: %s", e.getMessage()),  e);
            throw new ApplicationException(e);
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // bind visibility
        signatureIdLabel.managedProperty().bind(signatureIdLabel.visibleProperty());
        timestampIdLabel.managedProperty().bind(timestampIdLabel.visibleProperty());
        evidenceRecordIdLabel.managedProperty().bind(evidenceRecordIdLabel.visibleProperty());
        filenameContainer.managedProperty().bind(filenameContainer.visibleProperty());
        qualificationLevelContainer.managedProperty().bind(qualificationLevelContainer.visibleProperty());
        qualificationDetailsContainer.managedProperty().bind(qualificationDetailsContainer.visibleProperty());
        adesValidationDetailsContainer.managedProperty().bind(adesValidationDetailsContainer.visibleProperty());
        signatureFormatContainer.managedProperty().bind(signatureFormatContainer.visibleProperty());
        certificateChainContainer.managedProperty().bind(certificateChainContainer.visibleProperty());
        claimedSigningTimeContainer.managedProperty().bind(claimedSigningTimeContainer.visibleProperty());
        bestSignatureTimeContainer.managedProperty().bind(bestSignatureTimeContainer.visibleProperty());
        productionTimeContainer.managedProperty().bind(productionTimeContainer.visibleProperty());
        lowestPOEContainer.managedProperty().bind(lowestPOEContainer.visibleProperty());
        signatureScopesContainer.managedProperty().bind(signatureScopesContainer.visibleProperty());
        signatureTimestampsContainer.managedProperty().bind(signatureTimestampsContainer.visibleProperty());
        signatureEvidenceRecordsContainer.managedProperty().bind(signatureEvidenceRecordsContainer.visibleProperty());
    }

    public Node create(XmlToken token) {
        id.setText(token.getId());

        filename.setText(token.getFilename());
        filenameContainer.setVisible(token.getFilename() != null);

        qualificationDetails.getChildren().addAll(getDetails(token.getQualificationDetails()));
        qualificationDetailsContainer.setVisible(isNotEmpty(token.getQualificationDetails()));

        indication.setText(getIndicationResultString(token));
        indication.setStyle(getIndicationStyle(token.getIndication()));

        adesValidationDetails.getChildren().addAll(getDetails(token.getAdESValidationDetails()));
        adesValidationDetailsContainer.setVisible(isNotEmpty(token.getAdESValidationDetails()));

        certificateChain.getChildren().addAll(getCertificateChain(token.getCertificateChain()));
        certificateChainContainer.setVisible(isNotEmpty(token.getCertificateChain()));

        signatureIdLabel.setVisible(token instanceof XmlSignature);
        signatureFormatContainer.setVisible(token instanceof XmlSignature);
        claimedSigningTimeContainer.setVisible(token instanceof XmlSignature);
        bestSignatureTimeContainer.setVisible(token instanceof XmlSignature);
        signatureFormatContainer.setVisible(token instanceof XmlSignature);
        signatureTimestampsContainer.setVisible(token instanceof XmlSignature);
        signatureEvidenceRecordsContainer.setVisible(token instanceof XmlSignature);
        timestampIdLabel.setVisible(token instanceof XmlTimestamp);
        productionTimeContainer.setVisible(token instanceof XmlTimestamp);
        evidenceRecordIdLabel.setVisible(token instanceof XmlEvidenceRecord);
        lowestPOEContainer.setVisible(token instanceof XmlEvidenceRecord);

        if (token instanceof XmlSignature) {
            XmlSignature signature = (XmlSignature) token;

            if (signature.getSignatureLevel() != null) {
                qualificationLevel.setText(signature.getSignatureLevel().getValue().getReadable());
            }
            qualificationLevelContainer.setVisible(signature.getSignatureLevel() != null);

            signatureFormat.setText(signature.getSignatureFormat().toString());
            claimedSigningTime.setText(DSSUtils.formatDateToRFC(signature.getSigningTime()));
            bestSignatureTime.setText(DSSUtils.formatDateToRFC(signature.getBestSignatureTime()));
            signatureScopes.getChildren().addAll(getSignatureScope(signature.getSignatureScope()));
            signatureScopesContainer.setVisible(Utils.isCollectionNotEmpty(signature.getSignatureScope()));

            XmlTimestamps timestamps = signature.getTimestamps();
            if (timestamps != null && Utils.isCollectionNotEmpty(timestamps.getTimestamp())) {
                signatureTimestamps.getChildren().addAll(getSignatureTimestamps(timestamps));
                signatureTimestampsContainer.setVisible(true);
            }

            XmlEvidenceRecords evidenceRecords = signature.getEvidenceRecords();
            if (evidenceRecords != null && Utils.isCollectionNotEmpty(evidenceRecords.getEvidenceRecord())) {
                signatureEvidenceRecords.getChildren().addAll(getSignatureEvidenceRecords(evidenceRecords));
                signatureEvidenceRecordsContainer.setVisible(true);
            }

        } else if (token instanceof XmlTimestamp) {
            XmlTimestamp timestamp = (XmlTimestamp) token;

            if (timestamp.getTimestampLevel() != null) {
                qualificationLevel.setText(timestamp.getTimestampLevel().getValue().getReadable());
            }
            qualificationLevelContainer.setVisible(timestamp.getTimestampLevel() != null);

            productionTime.setText(DSSUtils.formatDateToRFC(timestamp.getProductionTime()));
            signatureScopes.getChildren().addAll(getSignatureScope(timestamp.getTimestampScope()));

            signatureScopesContainer.setVisible(Utils.isCollectionNotEmpty(timestamp.getTimestampScope()));

        } else if (token instanceof XmlEvidenceRecord) {
            XmlEvidenceRecord evidenceRecord = (XmlEvidenceRecord) token;

            qualificationLevelContainer.setVisible(false);

            lowestPOE.setText(DSSUtils.formatDateToRFC(evidenceRecord.getPOETime()));
            signatureScopes.getChildren().addAll(getSignatureScope(evidenceRecord.getEvidenceRecordScope()));
            signatureScopesContainer.setVisible(Utils.isCollectionNotEmpty(evidenceRecord.getEvidenceRecordScope()));

            XmlTimestamps timestamps = evidenceRecord.getTimestamps();
            if (timestamps != null && Utils.isCollectionNotEmpty(timestamps.getTimestamp())) {
                signatureTimestamps.getChildren().addAll(getSignatureTimestamps(timestamps));
                signatureTimestampsContainer.setVisible(true);
            }

        } else {
            throw new UnsupportedOperationException(String.format("XmlToken class '%s' is not supported!", token.getClass().toString()));
        }

        return parentNode;
    }

    protected boolean isNotEmpty(XmlDetails xmlDetails) {
        return xmlDetails != null && (Utils.isCollectionNotEmpty(xmlDetails.getError()) ||
                Utils.isCollectionNotEmpty(xmlDetails.getWarning()) || Utils.isCollectionNotEmpty(xmlDetails.getInfo()));
    }

    protected boolean isNotEmpty(XmlCertificateChain xmlCertificateChain) {
        return xmlCertificateChain != null && (Utils.isCollectionNotEmpty(xmlCertificateChain.getCertificate()));
    }

    protected List<Node> getDetails(XmlDetails xmlDetails) {
        if (isNotEmpty(xmlDetails)) {
            List<Node> messages = new ArrayList<>();
            messages.addAll(getMessages(xmlDetails.getError(), Color.RED));
            messages.addAll(getMessages(xmlDetails.getWarning(), Color.ORANGE));
            messages.addAll(getMessages(xmlDetails.getInfo(), Color.BLUE));

            List<Node> result = new ArrayList<>();
            Iterator<Node> iterator = messages.iterator();
            while (iterator.hasNext()) {
                result.add(iterator.next());
                if (iterator.hasNext()) {
                    result.add(new Text(System.lineSeparator()));
                }
            }
            return result;

        } else {
            return Collections.emptyList();
        }
    }

    private List<Node> getMessages(List<XmlMessage> messages, Color color) {
        List<Node> result = new ArrayList<>();
        for (XmlMessage message : messages) {
            Text text = new Text(message.getValue());
            text.setFill(color);
            result.add(text);
        }
        return result;
    }

    protected String getIndicationResultString(XmlToken token) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(token.getIndication().toString());
        if (token.getSubIndication() != null) {
            stringBuilder.append(" - ");
            stringBuilder.append(token.getSubIndication().toString());
        }
        return stringBuilder.toString();
    }

    protected String getIndicationStyle(Indication indication) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("-fx-font-weight: bold; ");
        switch (indication) {
            case TOTAL_PASSED:
            case PASSED:
                stringBuilder.append("-fx-text-fill: green;");
                break;
            case INDETERMINATE:
                stringBuilder.append("-fx-text-fill: orange;");
                break;
            case TOTAL_FAILED:
            case FAILED:
                stringBuilder.append("-fx-text-fill: red;");
                break;
            default:
                throw new UnsupportedOperationException(String.format("Indication '%s' is not supported!", indication.name()));
        }
        return stringBuilder.toString();
    }

    protected List<Node> getCertificateChain(XmlCertificateChain xmlCertificateChain) {
        List<Node> result = new ArrayList<>();
        if (xmlCertificateChain != null && Utils.isCollectionNotEmpty(xmlCertificateChain.getCertificate())) {
            for (int i = 0; i < xmlCertificateChain.getCertificate().size(); i++) {
                XmlCertificate xmlCertificate = xmlCertificateChain.getCertificate().get(i);
                Text text = new Text(xmlCertificate.getQualifiedName());
                result.add(text);
                if (i == 0) {
                    text.setStyle("-fx-font-weight: bold");
                }
                if (!isTrustedPath(xmlCertificate, xmlCertificateChain)) {
                    text.setFill(Color.GRAY);
                }
                if (xmlCertificate.isTrusted()) {
                    XmlTrustAnchors trustAnchors = xmlCertificate.getTrustAnchors();
                    if (trustAnchors != null && Utils.isCollectionNotEmpty(trustAnchors.getTrustAnchor())) {
                        for (XmlTrustAnchor xmlTrustAnchor : trustAnchors.getTrustAnchor()) {
                            result.add(getIconArrow());
                            result.add(new Text(xmlTrustAnchor.getCountryCode()));
                            result.add(getIconArrow());
                            result.add(new Text(xmlTrustAnchor.getTrustServiceProvider()));
                        }
                    } else {
                        result.add(new Text(" (Trust Anchor)"));
                    }
                }
                if (i + 1 != xmlCertificateChain.getCertificate().size()) {
                    result.add(new Text(System.lineSeparator()));
                }
            }
        } else {
            result.add(new Text("-"));
        }
        return result;
    }

    private Node getIconArrow() {
        Text text = new Text();
        text.setText(" -> ");
        return text;
    }

    private boolean isTrustedPath(XmlCertificate xmlCertificate, XmlCertificateChain xmlCertificateChain) {
        boolean trustAnchorFound = false;
        for (int i = xmlCertificateChain.getCertificate().size() - 1; i > -1; i--) {
            XmlCertificate chainItem = xmlCertificateChain.getCertificate().get(i);
            trustAnchorFound = trustAnchorFound || chainItem.isTrusted();
            if (xmlCertificate == chainItem) {
                return trustAnchorFound;
            }
        }
        return trustAnchorFound;
    }

    protected List<Node> getSignatureScope(List<XmlSignatureScope> signatureScopes) {
        List<Node> result = new ArrayList<>();

        if (Utils.isCollectionNotEmpty(signatureScopes)) {
            Iterator<XmlSignatureScope> it = signatureScopes.iterator();
            while (it.hasNext()) {
                XmlSignatureScope signatureScope = it.next();
                Text text = new Text(String.format("%s - %s", signatureScope.getScope(), signatureScope.getValue()));
                result.add(text);
                if (it.hasNext()) {
                    result.add(new Text(System.lineSeparator()));
                }
            }
        } else {
            result.add(new Text("-"));
        }
        return result;
    }

    private List<Node> getSignatureTimestamps(XmlTimestamps xmlTimestamps) {
        List<Node> result = new ArrayList<>();
        if (xmlTimestamps != null && Utils.isCollectionNotEmpty(xmlTimestamps.getTimestamp())) {
            Iterator<XmlTimestamp> it = xmlTimestamps.getTimestamp().iterator();
            while (it.hasNext()) {
                try {
                    XmlTimestamp xmlTimestamp = it.next();
                    Node timestampNode = TokenNodeController.loadController().create(xmlTimestamp);
                    result.add(timestampNode);

                } catch (Exception e) {
                    LOG.error(String.format("An error occurred: %s", e.getMessage()),  e);
                    throw new ApplicationException(e);
                }
                if (it.hasNext()) {
                    result.add(getHrNode());
                }
            }
        }
        return result;
    }

    private List<Node> getSignatureEvidenceRecords(XmlEvidenceRecords xmlEvidenceRecords) {
        List<Node> result = new ArrayList<>();
        if (xmlEvidenceRecords != null && Utils.isCollectionNotEmpty(xmlEvidenceRecords.getEvidenceRecord())) {
            Iterator<XmlEvidenceRecord> it = xmlEvidenceRecords.getEvidenceRecord().iterator();
            while (it.hasNext()) {
                try {
                    XmlEvidenceRecord xmlEvidenceRecord = it.next();
                    Node evidenceRecordNode = TokenNodeController.loadController().create(xmlEvidenceRecord);
                    result.add(evidenceRecordNode);

                } catch (Exception e) {
                    LOG.error(String.format("An error occurred: %s", e.getMessage()),  e);
                    throw new ApplicationException(e);
                }
                if (it.hasNext()) {
                    result.add(getHrNode());
                }
            }
        }
        return result;
    }

    private Node getHrNode() {
        Region hr = new Region();
        hr.getStyleClass().add("hr");
        return hr;
    }

}
