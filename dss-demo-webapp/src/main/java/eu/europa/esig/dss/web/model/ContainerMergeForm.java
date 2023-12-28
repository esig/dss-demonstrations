package eu.europa.esig.dss.web.model;

import eu.europa.esig.dss.asic.common.ASiCUtils;
import eu.europa.esig.dss.web.WebAppUtils;
import eu.europa.esig.dss.web.validation.AssertMultipartFile;
import jakarta.validation.constraints.AssertTrue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class ContainerMergeForm {

    private static final Logger LOG = LoggerFactory.getLogger(ContainerMergeForm.class);

    @AssertMultipartFile
    private List<MultipartFile> documentsToMerge;

    public List<MultipartFile> getDocumentsToMerge() {
        return documentsToMerge;
    }

    public void setDocumentsToMerge(List<MultipartFile> documentsToMerge) {
        this.documentsToMerge = documentsToMerge;
    }

    @AssertTrue(message = "{error.to.merge.containers.mandatory}")
    public boolean isDocumentsToMerge() {
        return WebAppUtils.isCollectionNotEmpty(documentsToMerge);
    }

    @AssertTrue(message = "{error.to.merge.containers.valid.zip}")
    public boolean isDocumentsToMergeValid() {
        if (documentsToMerge != null) {
            for (MultipartFile file : documentsToMerge) {
                if (!file.isEmpty()) {
                    try (InputStream is = file.getInputStream()) {
                        if (!ASiCUtils.isZip(is)) {
                            return false;
                        }
                    } catch (IOException e) {
                        LOG.warn("Unable to read the file with name '{}'. Reason : {}", file.getName(), e.getMessage(), e);
                    }
                }
            }
        }
        return true;
    }

}
