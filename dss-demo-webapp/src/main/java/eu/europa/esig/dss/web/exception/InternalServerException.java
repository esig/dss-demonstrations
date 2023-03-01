package eu.europa.esig.dss.web.exception;

import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.http.HttpStatus;

@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public class InternalServerException extends RuntimeException {

	private static final long serialVersionUID = -6337931956048056130L;

	public InternalServerException(String message) {
		super(message);
	}

	public InternalServerException(String message, Exception e) {
		super(message, e);
	}

}