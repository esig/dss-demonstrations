package eu.europa.esig.dss.web.model;

import eu.europa.esig.dss.enumerations.ASiCContainerType;
import eu.europa.esig.dss.utils.Utils;
import eu.europa.esig.dss.web.validation.AssertMultipartFile;
import jakarta.validation.constraints.AssertTrue;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public class TimestampForm {

	@AssertMultipartFile
	private List<MultipartFile> originalFiles;

	/* PAdES or ASiC-S or ASiC-E */
	private ASiCContainerType containerType;

	public List<MultipartFile> getOriginalFiles() {
		return originalFiles;
	}

	public void setOriginalFiles(List<MultipartFile> originalFiles) {
		this.originalFiles = originalFiles;
	}

	public ASiCContainerType getContainerType() {
		return containerType;
	}

	public void setContainerType(ASiCContainerType containerType) {
		this.containerType = containerType;
	}

	@AssertTrue(message = "{error.original.files.mandatory}")
	public boolean isOriginalFiles() {
		boolean valid = Utils.isCollectionNotEmpty(originalFiles);
		if (valid) {
			boolean atLeastOneNotEmptyFile = false;
			for (MultipartFile multipartFile : originalFiles) {
				if (multipartFile != null && !multipartFile.isEmpty()) {
					atLeastOneNotEmptyFile = true;
					break;
				}
			}
			valid = atLeastOneNotEmptyFile;
		}
		return valid;
	}

}
