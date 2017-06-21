package eu.europa.esig.dss.web.config;

import java.io.IOException;

import javax.sql.DataSource;
import javax.xml.ws.Endpoint;

import org.apache.cxf.Bus;
import org.apache.cxf.endpoint.Server;
import org.apache.cxf.interceptor.InterceptorProvider;
import org.apache.cxf.interceptor.LoggingInInterceptor;
import org.apache.cxf.interceptor.LoggingOutInterceptor;
import org.apache.cxf.jaxrs.JAXRSServerFactoryBean;
import org.apache.cxf.jaxws.EndpointImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.io.ClassPathResource;

import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;

import eu.europa.esig.dss.asic.signature.ASiCWithCAdESService;
import eu.europa.esig.dss.asic.signature.ASiCWithXAdESService;
import eu.europa.esig.dss.cades.signature.CAdESService;
import eu.europa.esig.dss.client.crl.JdbcCacheCRLSource;
import eu.europa.esig.dss.client.crl.OnlineCRLSource;
import eu.europa.esig.dss.client.http.DataLoader;
import eu.europa.esig.dss.client.http.commons.CommonsDataLoader;
import eu.europa.esig.dss.client.http.commons.FileCacheDataLoader;
import eu.europa.esig.dss.client.http.commons.OCSPDataLoader;
import eu.europa.esig.dss.client.http.commons.TimestampDataLoader;
import eu.europa.esig.dss.client.http.proxy.ProxyConfigManager;
import eu.europa.esig.dss.client.http.proxy.ProxyManager;
import eu.europa.esig.dss.client.ocsp.OnlineOCSPSource;
import eu.europa.esig.dss.client.tsp.OnlineTSPSource;
import eu.europa.esig.dss.pades.signature.PAdESService;
import eu.europa.esig.dss.signature.RemoteDocumentSignatureServiceImpl;
import eu.europa.esig.dss.signature.RemoteMultipleDocumentsSignatureServiceImpl;
import eu.europa.esig.dss.signature.RestDocumentSignatureService;
import eu.europa.esig.dss.signature.RestDocumentSignatureServiceImpl;
import eu.europa.esig.dss.signature.RestMultipleDocumentSignatureService;
import eu.europa.esig.dss.signature.RestMultipleDocumentSignatureServiceImpl;
import eu.europa.esig.dss.signature.SoapDocumentSignatureServiceImpl;
import eu.europa.esig.dss.signature.SoapMultipleDocumentsSignatureServiceImpl;
import eu.europa.esig.dss.token.Pkcs12SignatureToken;
import eu.europa.esig.dss.token.RemoteSignatureTokenConnection;
import eu.europa.esig.dss.token.RemoteSignatureTokenConnectionImpl;
import eu.europa.esig.dss.token.RestSignatureTokenConnection;
import eu.europa.esig.dss.token.RestSignatureTokenConnectionImpl;
import eu.europa.esig.dss.token.SoapSignatureTokenConnectionImpl;
import eu.europa.esig.dss.tsl.TrustedListsCertificateSource;
import eu.europa.esig.dss.tsl.service.TSLRepository;
import eu.europa.esig.dss.tsl.service.TSLValidationJob;
import eu.europa.esig.dss.validation.CertificateVerifier;
import eu.europa.esig.dss.validation.CommonCertificateVerifier;
import eu.europa.esig.dss.validation.RemoteDocumentValidationService;
import eu.europa.esig.dss.validation.RestDocumentValidationService;
import eu.europa.esig.dss.validation.RestDocumentValidationServiceImpl;
import eu.europa.esig.dss.validation.SoapDocumentValidationServiceImpl;
import eu.europa.esig.dss.web.business.ProxyConfigDbManager;
import eu.europa.esig.dss.x509.KeyStoreCertificateSource;
import eu.europa.esig.dss.x509.tsp.TSPSource;
import eu.europa.esig.dss.xades.signature.XAdESService;

@Configuration
@PropertySource("classpath:dss.properties")
@ComponentScan(basePackages = { "eu.europa.esig.dss" })
@ImportResource({ "classpath:META-INF/cxf/cxf.xml" })
public class DSSBeanFactory {

	private final static String TSA_BELGIUM_URL = "http://tsa.belgium.be/connect";

	@Value("${default.validation.policy}")
	private String defaultValidationPolicy;

	@Value("${dss.keystore.type}")
	private String ksType;

	@Value("${dss.keystore.filename}")
	private String ksStream;

	@Value("${dss.keystore.password}")
	private String ksPassword;

	@Value("${dss.oj.url}")
	private String ojUrl;
	
	@Value("${cxf.debug}")
	private boolean cxfDebug;

	@Autowired
	private DataSource dataSource;
	
	@Autowired
	private Bus bus;

	@Bean
	public ProxyConfigManager proxyConfigManager() {
		// return new ProxyConfigFileManager();
		return new ProxyConfigDbManager();
	}

	@Bean
	public ProxyManager proxyPreferenceManager() {
		ProxyManager proxyManager = new ProxyManager();
		proxyManager.setProxyConfigManager(proxyConfigManager());
		return proxyManager;
	}

	@Bean
	public CommonsDataLoader dataLoader() {
		CommonsDataLoader dataLoader = new CommonsDataLoader();
		dataLoader.setProxyPreferenceManager(proxyPreferenceManager());
		return dataLoader;
	}

	@Bean
	public TimestampDataLoader timestampDataLoader() {
		TimestampDataLoader timestampDataLoader = new TimestampDataLoader();
		timestampDataLoader.setProxyPreferenceManager(proxyPreferenceManager());
		return timestampDataLoader;
	}

	@Bean
	public OCSPDataLoader ocspDataLoader() {
		OCSPDataLoader ocspDataLoader = new OCSPDataLoader();
		ocspDataLoader.setProxyPreferenceManager(proxyPreferenceManager());
		return ocspDataLoader;
	}

	@Bean
	public FileCacheDataLoader fileCacheDataLoader(ProxyManager proxyPreferenceManager) {
		FileCacheDataLoader fileCacheDataLoader = new FileCacheDataLoader();
		fileCacheDataLoader.setProxyPreferenceManager(proxyPreferenceManager);
		// Per default uses "java.io.tmpdir" property
		// fileCacheDataLoader.setFileCacheDirectory(new File("/tmp"));
		return fileCacheDataLoader;
	}

	@Bean
	public OnlineCRLSource cacheCrlSource() {
		OnlineCRLSource onlineCRLSource = new OnlineCRLSource();
		onlineCRLSource.setDataLoader(dataLoader());
		return onlineCRLSource;
	}

	@Bean
	public JdbcCacheCRLSource crlSource() throws Exception {
		JdbcCacheCRLSource jdbcCacheCRLSource = new JdbcCacheCRLSource();
		jdbcCacheCRLSource.setDataSource(dataSource);
		jdbcCacheCRLSource.setCachedSource(cacheCrlSource());
		return jdbcCacheCRLSource;
	}

	@Bean
	public OnlineOCSPSource ocspSource() {
		OnlineOCSPSource onlineOCSPSource = new OnlineOCSPSource();
		onlineOCSPSource.setDataLoader(ocspDataLoader());
		return onlineOCSPSource;
	}

	@Bean
	public TrustedListsCertificateSource trustedListSource() {
		return new TrustedListsCertificateSource();
	}

	@Bean
	public TSPSource tspSource() {
		OnlineTSPSource onlineTSPSource = new OnlineTSPSource();
		onlineTSPSource.setDataLoader(timestampDataLoader());
		onlineTSPSource.setTspServer(TSA_BELGIUM_URL);
		return onlineTSPSource;
	}

	@Bean
	public CertificateVerifier certificateVerifier() throws Exception {
		CommonCertificateVerifier certificateVerifier = new CommonCertificateVerifier();
		certificateVerifier.setTrustedCertSource(trustedListSource());
		certificateVerifier.setCrlSource(crlSource());
		certificateVerifier.setOcspSource(ocspSource());
		certificateVerifier.setDataLoader(dataLoader());
		return certificateVerifier;
	}

	@Bean
	public ClassPathResource defaultPolicy() {
		return new ClassPathResource(defaultValidationPolicy);
	}

	@Bean
	public CAdESService cadesService() throws Exception {
		CAdESService service = new CAdESService(certificateVerifier());
		service.setTspSource(tspSource());
		return service;
	}

	@Bean
	public XAdESService xadesService() throws Exception {
		XAdESService service = new XAdESService(certificateVerifier());
		service.setTspSource(tspSource());
		return service;
	}

	@Bean
	public PAdESService padesService() throws Exception {
		PAdESService service = new PAdESService(certificateVerifier());
		service.setTspSource(tspSource());
		return service;
	}

	@Bean
	public ASiCWithCAdESService asicWithCadesService() throws Exception {
		ASiCWithCAdESService service = new ASiCWithCAdESService(certificateVerifier());
		service.setTspSource(tspSource());
		return service;
	}

	@Bean
	public ASiCWithXAdESService asicWithXadesService() throws Exception {
		ASiCWithXAdESService service = new ASiCWithXAdESService(certificateVerifier());
		service.setTspSource(tspSource());
		return service;
	}

	@Bean
	public RemoteDocumentSignatureServiceImpl remoteSignatureService() throws Exception {
		RemoteDocumentSignatureServiceImpl service = new RemoteDocumentSignatureServiceImpl();
		service.setAsicWithCAdESService(asicWithCadesService());
		service.setAsicWithXAdESService(asicWithXadesService());
		service.setCadesService(cadesService());
		service.setXadesService(xadesService());
		service.setPadesService(padesService());
		return service;
	}

	@Bean
	public RemoteMultipleDocumentsSignatureServiceImpl remoteMultipleDocumentsSignatureService() throws Exception {
		RemoteMultipleDocumentsSignatureServiceImpl service = new RemoteMultipleDocumentsSignatureServiceImpl();
		service.setAsicWithCAdESService(asicWithCadesService());
		service.setAsicWithXAdESService(asicWithXadesService());
		service.setXadesService(xadesService());
		return service;
	}

	@Bean
	public RemoteDocumentValidationService remoteValidationService() throws Exception {
		RemoteDocumentValidationService service = new RemoteDocumentValidationService();
		service.setVerifier(certificateVerifier());
		return service;
	}

	@Bean
	public Pkcs12SignatureToken serverSidePKCS12() throws IOException {
		return new Pkcs12SignatureToken(new ClassPathResource("user_a_rsa.p12").getInputStream(), "password");
	}

	@Bean
	public RemoteSignatureTokenConnection serverToken() throws IOException {
		RemoteSignatureTokenConnectionImpl remoteSignatureTokenConnectionImpl = new RemoteSignatureTokenConnectionImpl();
		remoteSignatureTokenConnectionImpl.setToken(serverSidePKCS12());
		return remoteSignatureTokenConnectionImpl;
	}

	@Bean
	public TSLRepository tslRepository(TrustedListsCertificateSource trustedListSource) {
		TSLRepository tslRepository = new TSLRepository();
		tslRepository.setTrustedListsCertificateSource(trustedListSource);
		return tslRepository;
	}

	@Bean
	public KeyStoreCertificateSource ojContentKeyStore() throws IOException {
		return new KeyStoreCertificateSource(getClass().getResourceAsStream(ksStream), ksType, ksPassword);
	}

	@Bean
	public TSLValidationJob tslValidationJob(DataLoader dataLoader, TSLRepository tslRepository,
			KeyStoreCertificateSource ojContentKeyStore) {
		TSLValidationJob validationJob = new TSLValidationJob();
		validationJob.setDataLoader(dataLoader);
		validationJob.setRepository(tslRepository);
		validationJob.setLotlUrl("https://ec.europa.eu/information_society/policy/esignature/trusted-list/tl-mp.xml");
		validationJob.setLotlRootSchemeInfoUri(
				"https://ec.europa.eu/information_society/policy/esignature/trusted-list/tl.html");
		validationJob.setLotlCode("EU");
		validationJob.setOjUrl(ojUrl);
		validationJob.setOjContentKeyStore(ojContentKeyStore);
		validationJob.setCheckLOTLSignature(true);
		validationJob.setCheckTSLSignatures(true);
		return validationJob;
	}
	
	@Bean 
	public JacksonJsonProvider jacksonJsonProvider() {
		return new JacksonJsonProvider();
	}

	@Bean
	public Endpoint soapSignatureService(RemoteDocumentSignatureServiceImpl remoteSignatureService) {
		SoapDocumentSignatureServiceImpl service = new SoapDocumentSignatureServiceImpl();
		service.setService(remoteSignatureService);

		EndpointImpl endpoint = new EndpointImpl(bus, service);
		endpoint.publish("/soap/signature/one-document");
		addLoggers(endpoint);
		return endpoint;
	}

	@Bean
	public Endpoint soapMultipleDocumentsSignatureService(
			RemoteMultipleDocumentsSignatureServiceImpl remoteMultipleDocumentsSignatureService) {
		SoapMultipleDocumentsSignatureServiceImpl service = new SoapMultipleDocumentsSignatureServiceImpl();
		service.setService(remoteMultipleDocumentsSignatureService);

		EndpointImpl endpoint = new EndpointImpl(bus, service);
		endpoint.publish("/soap/signature/multiple-documents");
		addLoggers(endpoint);
		return endpoint;
	}
	
	@Bean
	public Endpoint soapValidationService(RemoteDocumentValidationService remoteValidationService) {
		SoapDocumentValidationServiceImpl service = new SoapDocumentValidationServiceImpl();
		service.setValidationService(remoteValidationService);
		EndpointImpl endpoint = new EndpointImpl(bus, service);
		endpoint.publish("/soap/ValidationService");
		addLoggers(endpoint);
		return endpoint;
	}

	@Bean
	public Endpoint soapServerSigningService(RemoteSignatureTokenConnection serverToken) {
		SoapSignatureTokenConnectionImpl signatureToken = new SoapSignatureTokenConnectionImpl();
		signatureToken.setToken(serverToken);
		EndpointImpl endpoint = new EndpointImpl(bus, signatureToken);
		endpoint.publish("/soap/ServerSigningService");
		addLoggers(endpoint);
		return endpoint;
	}
	
	private void addLoggers(InterceptorProvider endpoint) {
		if(cxfDebug) {
			endpoint.getInInterceptors().add(new LoggingInInterceptor());
			endpoint.getOutInterceptors().add(new LoggingOutInterceptor());
		}
	}

	@Bean
	public RestDocumentSignatureService restSignatureService() throws Exception {
		RestDocumentSignatureServiceImpl service = new RestDocumentSignatureServiceImpl();
		service.setService(remoteSignatureService());
		return service;
	}

	@Bean
	public RestMultipleDocumentSignatureService restMultipleDocumentsSignatureService() throws Exception {
		RestMultipleDocumentSignatureServiceImpl service = new RestMultipleDocumentSignatureServiceImpl();
		service.setService(remoteMultipleDocumentsSignatureService());
		return service;
	}

	@Bean
	public RestDocumentValidationService restValidationService() throws Exception {
		RestDocumentValidationServiceImpl service = new RestDocumentValidationServiceImpl();
		service.setValidationService(remoteValidationService());
		return service;
	}

	@Bean
	public RestSignatureTokenConnection restServerSigningService() throws IOException {
		RestSignatureTokenConnectionImpl signatureToken = new RestSignatureTokenConnectionImpl();
		signatureToken.setToken(serverToken());
		return signatureToken;
	}

	@Bean
	public Server createServerValidationRestService() throws Exception {
		JAXRSServerFactoryBean sfb = new JAXRSServerFactoryBean();
		sfb.setServiceBean(restValidationService());
		sfb.setAddress("/rest/validation");
		sfb.setProvider(jacksonJsonProvider());
		addLoggers(sfb);
		return sfb.create();
	}

	@Bean
	public Server createServerSigningRestService() throws Exception {
		JAXRSServerFactoryBean sfb = new JAXRSServerFactoryBean();
		sfb.setServiceBean(restServerSigningService());
		sfb.setAddress("/rest/server-signing");
		sfb.setProvider(jacksonJsonProvider());
		addLoggers(sfb);
		return sfb.create();
	}
	
	@Bean
	public Server createOneDocumentSignatureRestService() throws Exception {
		JAXRSServerFactoryBean sfb = new JAXRSServerFactoryBean();
		sfb.setServiceBean(restSignatureService());
		sfb.setAddress("/rest/signature/one-document");
		sfb.setProvider(jacksonJsonProvider());
		addLoggers(sfb);
		return sfb.create();
	}
	
	@Bean
	public Server createMultipleDocumentRestService() throws Exception {
		JAXRSServerFactoryBean sfb = new JAXRSServerFactoryBean();
		sfb.setServiceBean(restMultipleDocumentsSignatureService());
		sfb.setAddress("/rest/signature/multiple-documents");
		sfb.setProvider(jacksonJsonProvider());
		addLoggers(sfb);
		return sfb.create();
	}
	
}
