package eu.europa.esig.dss.standalone.fx;

import javafx.beans.property.SimpleObjectProperty;

import javax.swing.filechooser.FileSystemView;
import java.io.File;

public class DSSFileChooserLoader {

    private static DSSFileChooserLoader instance;

    private final SimpleObjectProperty<File> lastKnownDirectoryProperty = new SimpleObjectProperty<>(
            FileSystemView.getFileSystemView().getDefaultDirectory());

    private DSSFileChooserLoader() {
    }

    public static DSSFileChooserLoader getInstance() {
        if (instance == null) {
            instance = new DSSFileChooserLoader();
        }
        return instance;
    }

    public DSSFileChooser createFileChooser(String title) {
        return createFileChooser(title, null);
    }

    public DSSFileChooser createFileChooser(String title, String fileExtensionFilter, String... fileExtensions) {
        DSSFileChooser fileChooser = new DSSFileChooser(title, fileExtensionFilter, fileExtensions);
        fileChooser.bindDirectoryProperty(lastKnownDirectoryProperty);
        return fileChooser;
    }

}
