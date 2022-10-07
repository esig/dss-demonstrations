package eu.europa.esig.dss.standalone.fx;

import eu.europa.esig.dss.utils.Utils;
import javafx.util.StringConverter;

import java.io.File;
import java.util.Collection;

public class CollectionFilesToStringConverter extends StringConverter<Collection<File>> {

    @Override
    public String toString(Collection<File> files) {
        if (Utils.isCollectionEmpty(files)) {
            return getPrimaryString();
        } else if (Utils.collectionSize(files) == 1) {
            return new FileToStringConverter().toString(files.iterator().next());
        } else {
            return String.format("%s files", Utils.collectionSize(files));
        }
    }

    protected String getPrimaryString() {
        return "0 files";
    }

    @Override
    public Collection<File> fromString(String string) {
        return null;
    }

}
