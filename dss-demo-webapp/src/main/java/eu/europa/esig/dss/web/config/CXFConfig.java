package eu.europa.esig.dss.web.config;

import javax.annotation.PostConstruct;
import javax.xml.ws.Endpoint;
import javax.xml.ws.soap.SOAPBinding;

import org.apache.cxf.Bus;
import org.apache.cxf.endpoint.Server;
import org.apache.cxf.ext.logging.LoggingInInterceptor;
import org.apache.cxf.ext.logging.LoggingOutInterceptor;
import org.apache.cxf.jaxb.JAXBDataBinding;
import org.apache.cxf.jaxrs.JAXRSServerFactoryBean;
import org.apache.cxf.jaxws.EndpointImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;

import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;

import eu.europa.esig.dss.RemoteDocument;
import eu.europa.esig.dss.RemoteSignatureParameters;
import eu.europa.esig.dss.signature.DateAdapter;
import eu.europa.esig.dss.signature.RemoteDocumentSignatureService;
import eu.europa.esig.dss.signature.RemoteMultipleDocumentsSignatureService;
import eu.europa.esig.dss.signature.RestDocumentSignatureService;
import eu.europa.esig.dss.signature.RestDocumentSignatureServiceImpl;
import eu.europa.esig.dss.signature.RestMultipleDocumentSignatureService;
import eu.europa.esig.dss.signature.RestMultipleDocumentSignatureServiceImpl;
import eu.europa.esig.dss.signature.SoapDocumentSignatureService;
import eu.europa.esig.dss.signature.SoapDocumentSignatureServiceImpl;
import eu.europa.esig.dss.signature.SoapMultipleDocumentsSignatureService;
import eu.europa.esig.dss.signature.SoapMultipleDocumentsSignatureServiceImpl;
import eu.europa.esig.dss.token.RemoteSignatureTokenConnection;
import eu.europa.esig.dss.token.RestSignatureTokenConnection;
import eu.europa.esig.dss.token.RestSignatureTokenConnectionImpl;
import eu.europa.esig.dss.token.SoapSignatureTokenConnection;
import eu.europa.esig.dss.token.SoapSignatureTokenConnectionImpl;
import eu.europa.esig.dss.validation.RemoteDocumentValidationService;
import eu.europa.esig.dss.validation.RestDocumentValidationService;
import eu.europa.esig.dss.validation.RestDocumentValidationServiceImpl;
import eu.europa.esig.dss.validation.SoapDocumentValidationService;
import eu.europa.esig.dss.validation.SoapDocumentValidationServiceImpl;

@Configuration
@ImportResource({ "classpath:META-INF/cxf/cxf.xml" })
public class CXFConfig {

	public static final String SOAP_SIGNATURE_ONE_DOCUMENT = "/soap/signature/one-document";
	public static final String SOAP_SIGNATURE_MULTIPLE_DOCUMENTS = "/soap/signature/multiple-documents";
	public static final String SOAP_VALIDATION = "/soap/validation";
	public static final String SOAP_SERVER_SIGNING = "/soap/server-signing";

	public static final String REST_SIGNATURE_ONE_DOCUMENT = "/rest/signature/one-document";
	public static final String REST_SIGNATURE_MULTIPLE_DOCUMENTS = "/rest/signature/multiple-documents";
	public static final String REST_VALIDATION = "/rest/validation";
	public static final String REST_SERVER_SIGNING = "/rest/server-signing";

	@Value("${cxf.debug}")
	private boolean cxfDebug;

	@Value("${cxf.mtom.enabled}")
	private boolean mtomEnabled;

	@Autowired
	private Bus bus;

	@Autowired
	private RemoteDocumentSignatureService<RemoteDocument, RemoteSignatureParameters> remoteSignatureService;

	@Autowired
	private RemoteMultipleDocumentsSignatureService<RemoteDocument, RemoteSignatureParameters> remoteMultipleDocumentsSignatureService;

	@Autowired
	private RemoteDocumentValidationService remoteValidationService;

	@Autowired
	private RemoteSignatureTokenConnection serverToken;

	@PostConstruct
	private void addLoggers() {
		if (cxfDebug) {
			LoggingInInterceptor loggingInInterceptor = new LoggingInInterceptor();
			bus.getInInterceptors().add(loggingInInterceptor);
			bus.getInFaultInterceptors().add(loggingInInterceptor);

			LoggingOutInterceptor loggingOutInterceptor = new LoggingOutInterceptor();
			bus.getOutInterceptors().add(loggingOutInterceptor);
			bus.getOutFaultInterceptors().add(loggingOutInterceptor);
		}
	}

	// --------------- SOAP

	@Bean
	public SoapDocumentSignatureService soapDocumentSignatureService() {
		SoapDocumentSignatureServiceImpl service = new SoapDocumentSignatureServiceImpl();
		service.setService(remoteSignatureService);
		return service;
	}

	@Bean
	public SoapMultipleDocumentsSignatureService soapMultipleDocumentsSignatureService() {
		SoapMultipleDocumentsSignatureServiceImpl service = new SoapMultipleDocumentsSignatureServiceImpl();
		service.setService(remoteMultipleDocumentsSignatureService);
		return service;
	}

	@Bean
	public SoapDocumentValidationService soapValidationService() {
		SoapDocumentValidationServiceImpl service = new SoapDocumentValidationServiceImpl();
		service.setValidationService(remoteValidationService);
		return service;
	}

	@Bean
	public SoapSignatureTokenConnection soapServerSigningService() {
		SoapSignatureTokenConnectionImpl signatureToken = new SoapSignatureTokenConnectionImpl();
		signatureToken.setToken(serverToken);
		return signatureToken;
	}

	@Bean
	public Endpoint createSoapSignatureEndpoint() {
		EndpointImpl endpoint = new EndpointImpl(bus, soapDocumentSignatureService());
		endpoint.publish(SOAP_SIGNATURE_ONE_DOCUMENT);
		addXmlAdapterDate(endpoint);
		enableMTOM(endpoint);
		return endpoint;
	}

	@Bean
	public Endpoint createSoapMultipleDocumentsSignatureEndpoint() {
		EndpointImpl endpoint = new EndpointImpl(bus, soapMultipleDocumentsSignatureService());
		endpoint.publish(SOAP_SIGNATURE_MULTIPLE_DOCUMENTS);
		addXmlAdapterDate(endpoint);
		enableMTOM(endpoint);
		return endpoint;
	}

	@Bean
	public Endpoint createSoapValidationEndpoint() {
		EndpointImpl endpoint = new EndpointImpl(bus, soapValidationService());
		endpoint.publish(SOAP_VALIDATION);
		enableMTOM(endpoint);
		return endpoint;
	}

	@Bean
	public Endpoint createSoapServerSigningEndpoint() {
		EndpointImpl endpoint = new EndpointImpl(bus, soapServerSigningService());
		endpoint.publish(SOAP_SERVER_SIGNING);
		enableMTOM(endpoint);
		return endpoint;
	}

	private void addXmlAdapterDate(EndpointImpl endpoint) {
		JAXBDataBinding jaxbDataBinding = new JAXBDataBinding();
		jaxbDataBinding.getConfiguredXmlAdapters().add(new DateAdapter());
		endpoint.setDataBinding(jaxbDataBinding);
	}

	private void enableMTOM(EndpointImpl endpoint) {
		SOAPBinding binding = (SOAPBinding) endpoint.getBinding();
		binding.setMTOMEnabled(mtomEnabled);
	}

	// --------------- REST

	@Bean
	public RestDocumentSignatureService restSignatureService() {
		RestDocumentSignatureServiceImpl service = new RestDocumentSignatureServiceImpl();
		service.setService(remoteSignatureService);
		return service;
	}

	@Bean
	public RestMultipleDocumentSignatureService restMultipleDocumentsSignatureService() {
		RestMultipleDocumentSignatureServiceImpl service = new RestMultipleDocumentSignatureServiceImpl();
		service.setService(remoteMultipleDocumentsSignatureService);
		return service;
	}

	@Bean
	public RestDocumentValidationService restValidationService() {
		RestDocumentValidationServiceImpl service = new RestDocumentValidationServiceImpl();
		service.setValidationService(remoteValidationService);
		return service;
	}

	@Bean
	public RestSignatureTokenConnection restServerSigningService() {
		RestSignatureTokenConnectionImpl signatureToken = new RestSignatureTokenConnectionImpl();
		signatureToken.setToken(serverToken);
		return signatureToken;
	}

	@Bean
	public Server createServerValidationRestService() {
		JAXRSServerFactoryBean sfb = new JAXRSServerFactoryBean();
		sfb.setServiceBean(restValidationService());
		sfb.setAddress(REST_VALIDATION);
		sfb.setProvider(jacksonJsonProvider());
		return sfb.create();
	}

	@Bean
	public Server createServerSigningRestService() {
		JAXRSServerFactoryBean sfb = new JAXRSServerFactoryBean();
		sfb.setServiceBean(restServerSigningService());
		sfb.setAddress(REST_SERVER_SIGNING);
		sfb.setProvider(jacksonJsonProvider());
		return sfb.create();
	}

	@Bean
	public Server createOneDocumentSignatureRestService() {
		JAXRSServerFactoryBean sfb = new JAXRSServerFactoryBean();
		sfb.setServiceBean(restSignatureService());
		sfb.setAddress(REST_SIGNATURE_ONE_DOCUMENT);
		sfb.setProvider(jacksonJsonProvider());
		return sfb.create();
	}

	@Bean
	public Server createMultipleDocumentRestService() {
		JAXRSServerFactoryBean sfb = new JAXRSServerFactoryBean();
		sfb.setServiceBean(restMultipleDocumentsSignatureService());
		sfb.setAddress(REST_SIGNATURE_MULTIPLE_DOCUMENTS);
		sfb.setProvider(jacksonJsonProvider());
		return sfb.create();
	}

	@Bean
	public JacksonJsonProvider jacksonJsonProvider() {
		return new JacksonJsonProvider();
	}

}
