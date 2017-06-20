package eu.europa.esig.dss.web.config;

import java.io.IOException;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.io.ClassPathResource;

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
import eu.europa.esig.dss.token.Pkcs12SignatureToken;
import eu.europa.esig.dss.token.RemoteSignatureTokenConnection;
import eu.europa.esig.dss.token.RemoteSignatureTokenConnectionImpl;
import eu.europa.esig.dss.tsl.TrustedListsCertificateSource;
import eu.europa.esig.dss.tsl.service.TSLRepository;
import eu.europa.esig.dss.tsl.service.TSLValidationJob;
import eu.europa.esig.dss.validation.CertificateVerifier;
import eu.europa.esig.dss.validation.CommonCertificateVerifier;
import eu.europa.esig.dss.validation.RemoteDocumentValidationService;
import eu.europa.esig.dss.web.business.ProxyConfigDbManager;
import eu.europa.esig.dss.x509.KeyStoreCertificateSource;
import eu.europa.esig.dss.x509.tsp.TSPSource;
import eu.europa.esig.dss.xades.signature.XAdESService;

@Configuration
@PropertySource("classpath:dss.properties")
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
	
	@Bean
	public ProxyConfigManager proxyConfigManager() {
//		return new ProxyConfigFileManager();
		return new ProxyConfigDbManager();
	}
	
	@Bean
	public ProxyManager proxyPreferenceManager(ProxyConfigManager proxyConfigManager) {
		ProxyManager proxyManager = new ProxyManager();
		proxyManager.setProxyConfigManager(proxyConfigManager);
		return proxyManager;
	}
	
	@Bean
	public CommonsDataLoader dataLoader(ProxyManager proxyPreferenceManager) {
		CommonsDataLoader dataLoader = new CommonsDataLoader();
		dataLoader.setProxyPreferenceManager(proxyPreferenceManager);
		return dataLoader;
	}
	
	@Bean
	public TimestampDataLoader timestampDataLoader(ProxyManager proxyPreferenceManager) {
		TimestampDataLoader timestampDataLoader = new TimestampDataLoader();
		timestampDataLoader.setProxyPreferenceManager(proxyPreferenceManager);
		return timestampDataLoader;
	}
	
	@Bean
	public OCSPDataLoader ocspDataLoader(ProxyManager proxyPreferenceManager) {
		OCSPDataLoader ocspDataLoader = new OCSPDataLoader();
		ocspDataLoader.setProxyPreferenceManager(proxyPreferenceManager);
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
	public OnlineCRLSource cacheCrlSource(DataLoader dataLoader) {
		OnlineCRLSource onlineCRLSource = new OnlineCRLSource();
		onlineCRLSource.setDataLoader(dataLoader);
		return onlineCRLSource;
	}
	
	@Bean 
	public JdbcCacheCRLSource crlSource(DataSource dataSource, OnlineCRLSource cacheCrlSource) throws Exception {
		JdbcCacheCRLSource jdbcCacheCRLSource = new JdbcCacheCRLSource();
		jdbcCacheCRLSource.setDataSource(dataSource);
		jdbcCacheCRLSource.setCachedSource(cacheCrlSource);
		return jdbcCacheCRLSource;
	}
	
	@Bean
	public OnlineOCSPSource ocspSource(DataLoader dataLoader) {
		OnlineOCSPSource onlineOCSPSource = new OnlineOCSPSource();
		onlineOCSPSource.setDataLoader(dataLoader);
		return onlineOCSPSource;
	}
	
	@Bean
	public TrustedListsCertificateSource trustedListSource() {
		return new TrustedListsCertificateSource();
	}
	
	@Bean
	public TSPSource tspSource(TimestampDataLoader dataLoader) {
		OnlineTSPSource onlineTSPSource = new OnlineTSPSource();
		onlineTSPSource.setDataLoader(dataLoader);
		onlineTSPSource.setTspServer(TSA_BELGIUM_URL);
		return onlineTSPSource;
	}
	
	@Bean
	public CertificateVerifier certificateVerifier(TrustedListsCertificateSource trustedListSource, OnlineOCSPSource ocspSource, JdbcCacheCRLSource crlSource, DataLoader dataLoader) {
		CommonCertificateVerifier certificateVerifier = new CommonCertificateVerifier();
		certificateVerifier.setTrustedCertSource(trustedListSource);
		certificateVerifier.setCrlSource(crlSource);
		certificateVerifier.setOcspSource(ocspSource);
		certificateVerifier.setDataLoader(dataLoader);
		return certificateVerifier;
	}
	
	@Bean
	public ClassPathResource defaultPolicy() {
		return new ClassPathResource(defaultValidationPolicy);
	}
	
	@Bean
	public CAdESService cadesService(CertificateVerifier certificateVerifier, TSPSource tspSource) {
		CAdESService service = new CAdESService(certificateVerifier);
		service.setTspSource(tspSource);
		return service;
	}
	
	@Bean
	public XAdESService xadesService(CertificateVerifier certificateVerifier, TSPSource tspSource) {
		XAdESService service = new XAdESService(certificateVerifier);
		service.setTspSource(tspSource);
		return service;
	}
	
	@Bean
	public PAdESService padesService(CertificateVerifier certificateVerifier, TSPSource tspSource) {
		PAdESService service = new PAdESService(certificateVerifier);
		service.setTspSource(tspSource);
		return service;
	}
	
	@Bean
	public ASiCWithCAdESService asicWithCadesService(CertificateVerifier certificateVerifier, TSPSource tspSource) {
		ASiCWithCAdESService service = new ASiCWithCAdESService(certificateVerifier);
		service.setTspSource(tspSource);
		return service;
	}
	
	@Bean
	public ASiCWithXAdESService asicWithXadesService(CertificateVerifier certificateVerifier, TSPSource tspSource) {
		ASiCWithXAdESService service = new ASiCWithXAdESService(certificateVerifier);
		service.setTspSource(tspSource);
		return service;
	}
	
	@Bean
	public RemoteDocumentSignatureServiceImpl remoteSignatureService(CAdESService cadesService, XAdESService xadesService, 
			PAdESService padesService, ASiCWithCAdESService asicWithCadesService, ASiCWithXAdESService asicWithXadesService) {
		RemoteDocumentSignatureServiceImpl service = new RemoteDocumentSignatureServiceImpl();
		service.setAsicWithCAdESService(asicWithCadesService);
		service.setAsicWithXAdESService(asicWithXadesService);
		service.setCadesService(cadesService);
		service.setXadesService(xadesService);
		service.setPadesService(padesService);
		return service;
	}
	
	@Bean
	public RemoteMultipleDocumentsSignatureServiceImpl remoteMultipleDocumentsSignatureService(XAdESService xadesService, 
			ASiCWithCAdESService asicWithCadesService, ASiCWithXAdESService asicWithXadesService) {
		RemoteMultipleDocumentsSignatureServiceImpl service = new RemoteMultipleDocumentsSignatureServiceImpl();
		service.setAsicWithCAdESService(asicWithCadesService);
		service.setAsicWithXAdESService(asicWithXadesService);
		service.setXadesService(xadesService);
		return service;
	}
	
	@Bean
	public RemoteDocumentValidationService remoteValidationService(CertificateVerifier certificateVerifier) {
		RemoteDocumentValidationService service = new RemoteDocumentValidationService();
		service.setVerifier(certificateVerifier);
		return service;
	}
	
	@Bean
	public Pkcs12SignatureToken serverSidePKCS12() throws IOException {
		return new Pkcs12SignatureToken(getClass().getResourceAsStream("classpath:user_a_rsa.p12"), "password");
	}
	
	@Bean
	public RemoteSignatureTokenConnection serverToken(Pkcs12SignatureToken serverSidePKCS12) {
		RemoteSignatureTokenConnectionImpl remoteSignatureTokenConnectionImpl = new RemoteSignatureTokenConnectionImpl();
		remoteSignatureTokenConnectionImpl.setToken(serverSidePKCS12);
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
	public TSLValidationJob tslValidationJob(DataLoader dataLoader, TSLRepository tslRepository, KeyStoreCertificateSource ojContentKeyStore) {
		TSLValidationJob validationJob = new TSLValidationJob();
		validationJob.setDataLoader(dataLoader);
		validationJob.setRepository(tslRepository);
		validationJob.setLotlUrl("https://ec.europa.eu/information_society/policy/esignature/trusted-list/tl-mp.xml");
		validationJob.setLotlRootSchemeInfoUri("https://ec.europa.eu/information_society/policy/esignature/trusted-list/tl.html");
		validationJob.setLotlCode("EU");
		validationJob.setOjUrl(ojUrl);
		validationJob.setOjContentKeyStore(ojContentKeyStore);
		validationJob.setCheckLOTLSignature(true);
		validationJob.setCheckTSLSignatures(true);
		return validationJob;
	}
}
