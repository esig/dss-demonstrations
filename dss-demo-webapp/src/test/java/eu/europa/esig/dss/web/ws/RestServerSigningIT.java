package eu.europa.esig.dss.web.ws;

import java.util.Arrays;

import org.apache.cxf.ext.logging.LoggingInInterceptor;
import org.apache.cxf.ext.logging.LoggingOutInterceptor;
import org.apache.cxf.jaxrs.client.JAXRSClientFactoryBean;

import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;

import eu.europa.esig.dss.token.RemoteSignatureTokenConnection;
import eu.europa.esig.dss.token.RestSignatureTokenConnection;
import eu.europa.esig.dss.web.config.CXFConfig;

public class RestServerSigningIT extends AbstractServerSigning {

	@Override
	RemoteSignatureTokenConnection getRemoteToken() {

		JAXRSClientFactoryBean factory = new JAXRSClientFactoryBean();

		factory.setAddress(getBaseCxf() + CXFConfig.REST_SERVER_SIGNING);
		factory.setServiceClass(RestSignatureTokenConnection.class);
		factory.setProviders(Arrays.asList(new JacksonJsonProvider()));

		LoggingInInterceptor loggingInInterceptor = new LoggingInInterceptor();
		factory.getInInterceptors().add(loggingInInterceptor);
		factory.getInFaultInterceptors().add(loggingInInterceptor);

		LoggingOutInterceptor loggingOutInterceptor = new LoggingOutInterceptor();
		factory.getOutInterceptors().add(loggingOutInterceptor);
		factory.getOutFaultInterceptors().add(loggingOutInterceptor);

		return factory.create(RestSignatureTokenConnection.class);

	}

}
