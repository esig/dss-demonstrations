package eu.europa.esig.dss.web.exception;

import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExceptionRestMapper implements ExceptionMapper<Exception> {

	private static final Logger LOG = LoggerFactory.getLogger(ExceptionRestMapper.class);

	@Override
	public Response toResponse(Exception exception) {
		if (LOG.isDebugEnabled()) {
			// print with error location for debug purposes
			LOG.debug("An error occurred during the REST response : {}", exception.getMessage());
		}
		if (exception instanceof JsonProcessingException) {
			// clear location to avoid information disclosure
			((JsonProcessingException) exception).clearLocation();
		}
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .type(MediaType.APPLICATION_JSON_TYPE)
                .entity(exception.getMessage())
                .build();
	}

}
