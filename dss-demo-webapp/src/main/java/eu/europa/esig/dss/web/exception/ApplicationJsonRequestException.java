package eu.europa.esig.dss.web.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public class ApplicationJsonRequestException extends RuntimeException {

	private static final long serialVersionUID = -1881926104709953107L;

	public ApplicationJsonRequestException(String message) {
		super(message);
	}
	
}