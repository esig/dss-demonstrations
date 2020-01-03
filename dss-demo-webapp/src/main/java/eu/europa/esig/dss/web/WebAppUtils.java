package eu.europa.esig.dss.web;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;

import eu.europa.esig.dss.model.DSSDocument;
import eu.europa.esig.dss.model.InMemoryDocument;
import eu.europa.esig.dss.validation.timestamp.TimestampToken;
import eu.europa.esig.dss.ws.dto.TimestampDTO;
import eu.europa.esig.dss.ws.signature.common.TimestampTokenConverter;

public final class WebAppUtils {

	private static final Logger LOG = LoggerFactory.getLogger(WebAppUtils.class);

	private WebAppUtils() {
	}

	public static DSSDocument toDSSDocument(MultipartFile multipartFile) {
		try {
			if ((multipartFile != null) && !multipartFile.isEmpty()) {
				return new InMemoryDocument(multipartFile.getBytes(), multipartFile.getOriginalFilename());
			}
		} catch (IOException e) {
			LOG.error("Cannot read file : " + e.getMessage(), e);
		}
		return null;
	}

	public static List<DSSDocument> toDSSDocuments(List<MultipartFile> documentsToSign) {
		List<DSSDocument> dssDocuments = new ArrayList<DSSDocument>();
		for (MultipartFile multipartFile : documentsToSign) {
			DSSDocument dssDocument = toDSSDocument(multipartFile);
			if (dssDocument != null) {
				dssDocuments.add(dssDocument);
			}
		}
		return dssDocuments;
	}

	public static TimestampDTO fromTimestampToken(TimestampToken token) {
		return TimestampTokenConverter.toTimestampDTO(token);
	}

	public static TimestampToken toTimestampToken(TimestampDTO dto) {
		return TimestampTokenConverter.toTimestampToken(dto);
	}

}
