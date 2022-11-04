package eu.europa.esig.dss.web.exception;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

public class ExceptionRestMapper implements ExceptionMapper<Exception> {

	private static final Logger LOG = LoggerFactory.getLogger(ExceptionRestMapper.class);

	@Override
	public Response toResponse(Exception exception) {
		if (exception instanceof JsonProcessingException) {
			if (LOG.isDebugEnabled()) {
				// print with error location for debug purposes
				LOG.debug("An error occurred during the REST response : {}", exception.getMessage());
			}
			((JsonProcessingException) exception).clearLocation();
		}
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .type(MediaType.APPLICATION_JSON_TYPE)
                .entity(exception.getMessage())
                .build();
	}

}
