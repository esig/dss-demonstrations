package eu.europa.esig.dss.standalone.controller;

import eu.europa.esig.dss.enumerations.MimeType;
import eu.europa.esig.dss.model.DSSDocument;
import eu.europa.esig.dss.utils.Utils;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileOutputStream;

public abstract class AbstractController implements Initializable {

    protected Stage stage;

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    protected void save(DSSDocument signedDocument) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialFileName(signedDocument.getName());
        MimeType mimeType = signedDocument.getMimeType();

        String extension = mimeType.getExtension();
        String filterPattern = extension != null ? "*." + extension : "*";
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter(mimeType.getMimeTypeString(), filterPattern);
        fileChooser.getExtensionFilters().add(extFilter);
        File fileToSave = fileChooser.showSaveDialog(stage);

        if (fileToSave != null) {
            try (FileOutputStream fos = new FileOutputStream(fileToSave)) {
                Utils.copy(signedDocument.openStream(), fos);
            } catch (Exception e) {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Unable to save file : " + e.getMessage(), ButtonType.CLOSE);
                alert.showAndWait();
            }
        }
    }

}
