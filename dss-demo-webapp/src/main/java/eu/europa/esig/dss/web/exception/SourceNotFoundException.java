package eu.europa.esig.dss.web.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class SourceNotFoundException extends RuntimeException {

	private static final long serialVersionUID = 4207780280256354030L;

	public SourceNotFoundException(String message) {
		super(message);
	}

}
