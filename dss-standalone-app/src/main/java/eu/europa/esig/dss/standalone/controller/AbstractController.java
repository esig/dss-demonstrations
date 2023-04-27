package eu.europa.esig.dss.standalone.controller;

import eu.europa.esig.dss.enumerations.MimeType;
import eu.europa.esig.dss.model.DSSDocument;
import eu.europa.esig.dss.standalone.source.SystemPropertyReader;
import eu.europa.esig.dss.utils.Utils;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileOutputStream;

public abstract class AbstractController implements Initializable {

    private static final SimpleObjectProperty<File> lastKnownSavingDirectory = new SimpleObjectProperty<>(
            new File(SystemPropertyReader.getUserHome()));

    protected Stage stage;

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    protected void save(DSSDocument document) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialFileName(document.getName());
        fileChooser.initialDirectoryProperty().bindBidirectional(lastKnownSavingDirectory);

        MimeType mimeType = document.getMimeType();
        String extension = Utils.getFileNameExtension(document.getName());
        if (Utils.isStringEmpty(extension)) {
            extension = mimeType.getExtension();
        }

        String filterPattern = extension != null ? "*." + extension : "*";
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter(mimeType.getMimeTypeString(), filterPattern);
        fileChooser.getExtensionFilters().add(extFilter);
        File fileToSave = fileChooser.showSaveDialog(stage);

        if (fileToSave != null) {
            lastKnownSavingDirectory.setValue(fileToSave.getParentFile());

            try (FileOutputStream fos = new FileOutputStream(fileToSave)) {
                Utils.copy(document.openStream(), fos);
            } catch (Exception e) {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Unable to save file : " + e.getMessage(), ButtonType.CLOSE);
                alert.showAndWait();
            }
        }
    }

}
