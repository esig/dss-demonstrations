package eu.europa.esig.dss.web.exception;

import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.http.HttpStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class BadRequestException extends RuntimeException {

	private static final long serialVersionUID = -6337931956048056130L;

	public BadRequestException(String message) {
		super(message);
	}

}