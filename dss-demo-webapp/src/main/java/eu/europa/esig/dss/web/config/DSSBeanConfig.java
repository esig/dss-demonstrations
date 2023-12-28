package eu.europa.esig.dss.web.config;

import eu.europa.esig.dss.alert.ExceptionOnStatusAlert;
import eu.europa.esig.dss.asic.cades.signature.ASiCWithCAdESService;
import eu.europa.esig.dss.asic.xades.signature.ASiCWithXAdESService;
import eu.europa.esig.dss.cades.signature.CAdESService;
import eu.europa.esig.dss.jades.signature.JAdESService;
import eu.europa.esig.dss.model.DSSException;
import eu.europa.esig.dss.pades.signature.ExternalCMSService;
import eu.europa.esig.dss.pades.signature.PAdESService;
import eu.europa.esig.dss.pades.signature.PAdESWithExternalCMSService;
import eu.europa.esig.dss.service.crl.JdbcCacheCRLSource;
import eu.europa.esig.dss.service.crl.OnlineCRLSource;
import eu.europa.esig.dss.service.http.commons.CommonsDataLoader;
import eu.europa.esig.dss.service.http.commons.FileCacheDataLoader;
import eu.europa.esig.dss.service.http.commons.OCSPDataLoader;
import eu.europa.esig.dss.service.http.commons.SSLCertificateLoader;
import eu.europa.esig.dss.service.http.proxy.ProxyConfig;
import eu.europa.esig.dss.service.ocsp.JdbcCacheOCSPSource;
import eu.europa.esig.dss.service.ocsp.OnlineOCSPSource;
import eu.europa.esig.dss.service.x509.aia.JdbcCacheAIASource;
import eu.europa.esig.dss.spi.client.http.DSSFileLoader;
import eu.europa.esig.dss.spi.client.http.IgnoreDataLoader;
import eu.europa.esig.dss.spi.tsl.TrustedListsCertificateSource;
import eu.europa.esig.dss.spi.x509.CommonTrustedCertificateSource;
import eu.europa.esig.dss.spi.x509.KeyStoreCertificateSource;
import eu.europa.esig.dss.spi.x509.aia.AIASource;
import eu.europa.esig.dss.spi.x509.aia.DefaultAIASource;
import eu.europa.esig.dss.spi.x509.revocation.crl.CRLSource;
import eu.europa.esig.dss.spi.x509.revocation.ocsp.OCSPSource;
import eu.europa.esig.dss.spi.x509.tsp.TSPSource;
import eu.europa.esig.dss.token.KeyStoreSignatureTokenConnection;
import eu.europa.esig.dss.tsl.function.OfficialJournalSchemeInformationURI;
import eu.europa.esig.dss.tsl.function.TypeOtherTSLPointer;
import eu.europa.esig.dss.tsl.function.XMLOtherTSLPointer;
import eu.europa.esig.dss.tsl.job.TLValidationJob;
import eu.europa.esig.dss.tsl.source.LOTLSource;
import eu.europa.esig.dss.utils.Utils;
import eu.europa.esig.dss.validation.CertificateVerifier;
import eu.europa.esig.dss.validation.CommonCertificateVerifier;
import eu.europa.esig.dss.validation.SignaturePolicyProvider;
import eu.europa.esig.dss.ws.cert.validation.common.RemoteCertificateValidationService;
import eu.europa.esig.dss.ws.server.signing.common.RemoteSignatureTokenConnection;
import eu.europa.esig.dss.ws.server.signing.common.RemoteSignatureTokenConnectionImpl;
import eu.europa.esig.dss.ws.signature.common.RemoteDocumentSignatureServiceImpl;
import eu.europa.esig.dss.ws.signature.common.RemoteExternalCMSService;
import eu.europa.esig.dss.ws.signature.common.RemoteExternalCMSServiceImpl;
import eu.europa.esig.dss.ws.signature.common.RemoteMultipleDocumentsSignatureServiceImpl;
import eu.europa.esig.dss.ws.signature.common.RemotePAdESWithExternalCMSService;
import eu.europa.esig.dss.ws.signature.common.RemotePAdESWithExternalCMSServiceImpl;
import eu.europa.esig.dss.ws.signature.common.RemoteTrustedListSignatureServiceImpl;
import eu.europa.esig.dss.ws.timestamp.remote.RemoteTimestampService;
import eu.europa.esig.dss.ws.validation.common.RemoteDocumentValidationService;
import eu.europa.esig.dss.xades.signature.XAdESService;
import org.apache.hc.client5.http.ssl.TrustAllStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportResource;
import org.springframework.core.io.ClassPathResource;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore.PasswordProtection;
import java.util.ArrayList;
import java.util.List;

@Configuration
@ComponentScan(basePackages = { "eu.europa.esig.dss.web.job", "eu.europa.esig.dss.web.service" })
@Import({ PropertiesConfig.class, JdbcConfig.class, ProxyConfiguration.class, WebSecurityConfiguration.class,
		SchedulingConfig.class })
@ImportResource({ "${tsp-source}" })
public class DSSBeanConfig {

	private static final Logger LOG = LoggerFactory.getLogger(DSSBeanConfig.class);

	@Value("${default.validation.policy}")
	private String defaultValidationPolicy;

	@Value("${default.certificate.validation.policy}")
	private String defaultCertificateValidationPolicy;

	@Value("${current.lotl.url}")
	private String lotlUrl;

	@Value("${lotl.country.code}")
	private String lotlCountryCode;

	@Value("${current.oj.url}")
	private String currentOjUrl;

	@Value("${oj.content.keystore.type}")
	private String ksType;

	@Value("${oj.content.keystore.filename}")
	private String ksFilename;

	@Value("${oj.content.keystore.password}")
	private String ksPassword;

	@Value("${tl.loader.ades.enabled}")
	private boolean adesLotlEnabled;

	@Value("${tl.loader.ades.lotlUrl}")
	private String adesLotlUrl;

	@Value("${tl.loader.ades.keystore.type}")
	private String adesKeyStoreType;

	@Value("${tl.loader.ades.keystore.filename}")
	private String adesKeyStoreFilename;

	@Value("${tl.loader.ades.keystore.password}")
	private String adesKeyStorePassword;

	@Value("${tl.loader.ades.tsl.type}")
	private String adesTSLType;

	@Value("${dss.server.signing.keystore.type}")
	private String serverSigningKeystoreType;

	@Value("${dss.server.signing.keystore.filename}")
	private String serverSigningKeystoreFilename;

	@Value("${dss.server.signing.keystore.password}")
	private String serverSigningKeystorePassword;

	@Autowired
	private TSPSource tspSource;

	@Autowired(required = false)
	private JdbcCacheAIASource jdbcCacheAIASource;

	@Autowired(required = false)
	private JdbcCacheCRLSource jdbcCacheCRLSource;

	@Autowired(required = false)
	private JdbcCacheOCSPSource jdbcCacheOCSPSource;

	@Value("${cache.expiration:0}")
	private long cacheExpiration;

	@Value("${cache.crl.default.next.update:0}")
	private long crlDefaultNextUpdate;

	@Value("${cache.crl.max.next.update:0}")
	private long crlMaxNextUpdate;

	@Value("${cache.ocsp.default.next.update:0}")
	private long ocspDefaultNextUpdate;

	@Value("${cache.ocsp.max.next.update:0}")
	private long ocspMaxNextUpdate;

	@Value("${dataloader.connection.timeout}")
	private int connectionTimeout;

	@Value("${dataloader.connection.request.timeout}")
	private int connectionRequestTimeout;

	@Value("${dataloader.redirect.enabled}")
	private boolean redirectEnabled;

	@Value("${trusted.source.keystore.type:}")
	private String trustSourceKsType;

	@Value("${trusted.source.keystore.filename:}")
	private String trustSourceKsFilename;

	@Value("${trusted.source.keystore.password:}")
	private String trustSourceKsPassword;


	// can be null
	@Autowired(required = false)
	private ProxyConfig proxyConfig;

	@Bean
	public CommonsDataLoader dataLoader() {
		return configureCommonsDataLoader(new CommonsDataLoader());
	}
	
	@Bean
    public CommonsDataLoader trustAllDataLoader() {
		CommonsDataLoader trustAllDataLoader = configureCommonsDataLoader(new CommonsDataLoader());
		trustAllDataLoader.setTrustStrategy(TrustAllStrategy.INSTANCE);
		return trustAllDataLoader;
    }

	@Bean
	public OCSPDataLoader ocspDataLoader() {
		return configureCommonsDataLoader(new OCSPDataLoader());
	}

	@Bean
	public FileCacheDataLoader fileCacheDataLoader() {
		FileCacheDataLoader fileCacheDataLoader = initFileCacheDataLoader();
		fileCacheDataLoader.setCacheExpirationTime(cacheExpiration * 1000); // to millis
		return fileCacheDataLoader;
	}

	private FileCacheDataLoader initFileCacheDataLoader() {
		FileCacheDataLoader fileCacheDataLoader = new FileCacheDataLoader();
		fileCacheDataLoader.setDataLoader(dataLoader());
		// Per default uses "java.io.tmpdir" property
		// fileCacheDataLoader.setFileCacheDirectory(new File("/tmp"));
		return fileCacheDataLoader;
	}

	@Bean
	public DefaultAIASource onlineAIASource() {
		return new DefaultAIASource(dataLoader());
	}

	@Bean
	public AIASource cachedAIASource() {
		if (jdbcCacheAIASource != null) {
			jdbcCacheAIASource.setProxySource(onlineAIASource());
			return jdbcCacheAIASource;
		}
		FileCacheDataLoader fileCacheDataLoader = fileCacheDataLoader();
		return new DefaultAIASource(fileCacheDataLoader);
	}

	@Bean
	public OnlineCRLSource onlineCRLSource() {
		OnlineCRLSource onlineCRLSource = new OnlineCRLSource();
		onlineCRLSource.setDataLoader(dataLoader());
		return onlineCRLSource;
	}

	@Bean
	public CRLSource cachedCRLSource() {
		if (jdbcCacheCRLSource != null) {
			jdbcCacheCRLSource.setProxySource(onlineCRLSource());
			jdbcCacheCRLSource.setDefaultNextUpdateDelay(crlDefaultNextUpdate);
			jdbcCacheCRLSource.setMaxNextUpdateDelay(crlMaxNextUpdate);
			return jdbcCacheCRLSource;
		}
		OnlineCRLSource onlineCRLSource = onlineCRLSource();
		FileCacheDataLoader fileCacheDataLoader = initFileCacheDataLoader();
		fileCacheDataLoader.setCacheExpirationTime(crlMaxNextUpdate * 1000); // to millis
		onlineCRLSource.setDataLoader(fileCacheDataLoader);
		return onlineCRLSource;
	}

	@Bean
	public OnlineOCSPSource onlineOCSPSource() {
		OnlineOCSPSource onlineOCSPSource = new OnlineOCSPSource();
		onlineOCSPSource.setDataLoader(ocspDataLoader());
		return onlineOCSPSource;
	}

	@Bean
	public OCSPSource cachedOCSPSource() {
		if (jdbcCacheOCSPSource != null) {
			jdbcCacheOCSPSource.setProxySource(onlineOCSPSource());
			jdbcCacheOCSPSource.setDefaultNextUpdateDelay(ocspDefaultNextUpdate);
			jdbcCacheOCSPSource.setMaxNextUpdateDelay(ocspMaxNextUpdate);
			return jdbcCacheOCSPSource;
		}
		OnlineOCSPSource onlineOCSPSource = onlineOCSPSource();
		FileCacheDataLoader fileCacheDataLoader = initFileCacheDataLoader();
		fileCacheDataLoader.setDataLoader(ocspDataLoader());
		fileCacheDataLoader.setCacheExpirationTime(ocspMaxNextUpdate * 1000); // to millis
		onlineOCSPSource.setDataLoader(fileCacheDataLoader);
		return onlineOCSPSource;
	}

	@Bean
	public SignaturePolicyProvider signaturePolicyProvider() {
		SignaturePolicyProvider signaturePolicyProvider = new SignaturePolicyProvider();
		signaturePolicyProvider.setDataLoader(fileCacheDataLoader());
		return signaturePolicyProvider;
	}

	@Bean(name = "european-trusted-list-certificate-source")
	public TrustedListsCertificateSource trustedListSource() {
		return new TrustedListsCertificateSource();
	}

	@Bean
	public CommonTrustedCertificateSource trustedCertificateSource() {
		CommonTrustedCertificateSource trustedCertificateSource = new CommonTrustedCertificateSource();
		if (Utils.isStringNotEmpty(trustSourceKsFilename)) {
			try {
				KeyStoreCertificateSource keyStore = new KeyStoreCertificateSource(
						new ClassPathResource(trustSourceKsFilename).getFile(), trustSourceKsType, trustSourceKsPassword.toCharArray());
				trustedCertificateSource.importAsTrusted(keyStore);
			} catch (IOException e) {
				throw new DSSException("Unable to load the file " + adesKeyStoreFilename, e);
			}
		}
		return trustedCertificateSource;
	}

	@Bean
	public CertificateVerifier certificateVerifier() {
		CommonCertificateVerifier certificateVerifier = new CommonCertificateVerifier();
		certificateVerifier.setCrlSource(cachedCRLSource());
		certificateVerifier.setOcspSource(cachedOCSPSource());
		certificateVerifier.setAIASource(cachedAIASource());
		certificateVerifier.setTrustedCertSources(trustedListSource(), trustedCertificateSource());

		// Default configs
		certificateVerifier.setAlertOnMissingRevocationData(new ExceptionOnStatusAlert());
		certificateVerifier.setCheckRevocationForUntrustedChains(false);

		return certificateVerifier;
	}

	@Bean
	public ClassPathResource defaultPolicy() {
		return new ClassPathResource(defaultValidationPolicy);
	}

	@Bean
	public ClassPathResource defaultCertificateValidationPolicy() {
		return new ClassPathResource(defaultCertificateValidationPolicy);
	}

	@Bean
	public CAdESService cadesService() {
		CAdESService service = new CAdESService(certificateVerifier());
		service.setTspSource(tspSource);
		return service;
	}

	@Bean
	public XAdESService xadesService() {
		XAdESService service = new XAdESService(certificateVerifier());
		service.setTspSource(tspSource);
		return service;
	}

	@Bean
	public PAdESService padesService() {
		PAdESService service = new PAdESService(certificateVerifier());
		service.setTspSource(tspSource);
		return service;
	}

	@Bean
	public JAdESService jadesService() {
		JAdESService service = new JAdESService(certificateVerifier());
		service.setTspSource(tspSource);
		return service;
	}

	@Bean
	public ASiCWithCAdESService asicWithCadesService() {
		ASiCWithCAdESService service = new ASiCWithCAdESService(certificateVerifier());
		service.setTspSource(tspSource);
		return service;
	}

	@Bean
	public ASiCWithXAdESService asicWithXadesService() {
		ASiCWithXAdESService service = new ASiCWithXAdESService(certificateVerifier());
		service.setTspSource(tspSource);
		return service;
	}

	@Bean
	public RemoteDocumentSignatureServiceImpl remoteSignatureService() {
		RemoteDocumentSignatureServiceImpl service = new RemoteDocumentSignatureServiceImpl();
		service.setAsicWithCAdESService(asicWithCadesService());
		service.setAsicWithXAdESService(asicWithXadesService());
		service.setCadesService(cadesService());
		service.setXadesService(xadesService());
		service.setPadesService(padesService());
		service.setJadesService(jadesService());
		return service;
	}

	@Bean
	public RemoteMultipleDocumentsSignatureServiceImpl remoteMultipleDocumentsSignatureService() {
		RemoteMultipleDocumentsSignatureServiceImpl service = new RemoteMultipleDocumentsSignatureServiceImpl();
		service.setAsicWithCAdESService(asicWithCadesService());
		service.setAsicWithXAdESService(asicWithXadesService());
		service.setXadesService(xadesService());
		service.setJadesService(jadesService());
		return service;
	}

	@Bean
	public RemoteTrustedListSignatureServiceImpl remoteTrustedListSignatureService() {
		RemoteTrustedListSignatureServiceImpl service = new RemoteTrustedListSignatureServiceImpl();
		service.setXadesService(xadesService());
		return service;
	}

	@Bean
	public RemotePAdESWithExternalCMSService remotePadesWithExternalCmsService() {
		RemotePAdESWithExternalCMSServiceImpl service = new RemotePAdESWithExternalCMSServiceImpl();
		service.setService(padesWithExternalCmsService());
		return service;
	}

	@Bean
	public PAdESWithExternalCMSService padesWithExternalCmsService() {
		PAdESWithExternalCMSService padesWithExternalCMSService = new PAdESWithExternalCMSService();
		padesWithExternalCMSService.setCertificateVerifier(certificateVerifier());
		padesWithExternalCMSService.setTspSource(tspSource);
		return padesWithExternalCMSService;
	}

	@Bean
	public RemoteExternalCMSService remoteExternalCmsService() {
		RemoteExternalCMSServiceImpl service = new RemoteExternalCMSServiceImpl();
		service.setService(externalCmsService());
		return service;
	}

	@Bean
	public ExternalCMSService externalCmsService() {
		ExternalCMSService externalCMSService = new ExternalCMSService(certificateVerifier());
		externalCMSService.setTspSource(tspSource);
		return externalCMSService;
	}

	@Bean
	public RemoteDocumentValidationService remoteValidationService() {
		RemoteDocumentValidationService service = new RemoteDocumentValidationService();
		service.setVerifier(certificateVerifier());
		if (defaultPolicy() != null) {
			try (InputStream is = defaultPolicy().getInputStream()) {
				service.setDefaultValidationPolicy(is);
			} catch (IOException e) {
				LOG.error(String.format("Unable to parse policy: %s", e.getMessage()), e);
			}
		}
		return service;
	}
	
	@Bean
	public RemoteCertificateValidationService remoteCertificateValidationService() {
		RemoteCertificateValidationService service = new RemoteCertificateValidationService();
		service.setVerifier(certificateVerifier());
		if (defaultCertificateValidationPolicy() != null) {
			try (InputStream is = defaultCertificateValidationPolicy().getInputStream()) {
				service.setDefaultValidationPolicy(is);
			} catch (IOException e) {
				LOG.error(String.format("Unable to parse policy: %s", e.getMessage()), e);
			}
		}
		return service;
	}

	@Bean
	public KeyStoreSignatureTokenConnection remoteToken() throws IOException {
		return new KeyStoreSignatureTokenConnection(new ClassPathResource(serverSigningKeystoreFilename).getFile(), serverSigningKeystoreType,
				new PasswordProtection(serverSigningKeystorePassword.toCharArray()));
	}

	@Bean
	public RemoteSignatureTokenConnection serverToken() throws IOException {
		RemoteSignatureTokenConnectionImpl remoteSignatureTokenConnectionImpl = new RemoteSignatureTokenConnectionImpl();
		remoteSignatureTokenConnectionImpl.setToken(remoteToken());
		return remoteSignatureTokenConnectionImpl;
	}
	
	@Bean
	public RemoteTimestampService timestampService() throws IOException {
		RemoteTimestampService timestampService = new RemoteTimestampService();
		timestampService.setTSPSource(tspSource);
		return timestampService;
	}

	@Bean
	public KeyStoreCertificateSource ojContentKeyStore() {
		try {
			return new KeyStoreCertificateSource(new ClassPathResource(ksFilename).getFile(), ksType, ksPassword.toCharArray());
		} catch (IOException e) {
			throw new DSSException("Unable to load the file " + ksFilename, e);
		}
	}

	@Bean
	public KeyStoreCertificateSource adesLotlKeyStore() {
		try {
			return new KeyStoreCertificateSource(new ClassPathResource(adesKeyStoreFilename).getFile(), adesKeyStoreType, adesKeyStorePassword.toCharArray());
		} catch (IOException e) {
			throw new DSSException("Unable to load the file " + adesKeyStoreFilename, e);
		}
	}
	
	@Bean 
	public TLValidationJob job() {
		TLValidationJob job = new TLValidationJob();
		job.setTrustedListCertificateSource(trustedListSource());
		job.setListOfTrustedListSources(listOfTrustedListSources());
		job.setOfflineDataLoader(offlineLoader());
		job.setOnlineDataLoader(onlineLoader());
		return job;
	}

	@Bean
	public DSSFileLoader onlineLoader() {
		FileCacheDataLoader onlineFileLoader = new FileCacheDataLoader();
		onlineFileLoader.setCacheExpirationTime(0);
		onlineFileLoader.setDataLoader(dataLoader());
		onlineFileLoader.setFileCacheDirectory(tlCacheDirectory());
		return onlineFileLoader;
	}

	private LOTLSource[] listOfTrustedListSources() {
		List<LOTLSource> lotlSourceList = new ArrayList<>();
		lotlSourceList.add(europeanLOTL());
		if (adesLotlEnabled) {
			lotlSourceList.add(adesLOTL());
		}
		return lotlSourceList.toArray(new LOTLSource[0]);
	}

	@Bean(name = "european-lotl-source")
	public LOTLSource europeanLOTL() {
		LOTLSource lotlSource = new LOTLSource();
		lotlSource.setUrl(lotlUrl);
		lotlSource.setCertificateSource(ojContentKeyStore());
		lotlSource.setSigningCertificatesAnnouncementPredicate(new OfficialJournalSchemeInformationURI(currentOjUrl));
		lotlSource.setPivotSupport(true);
		return lotlSource;
	}

	@Bean(name = "ades-source")
	public LOTLSource adesLOTL() {
		LOTLSource adesLOTL = new LOTLSource();
		adesLOTL.setUrl(adesLotlUrl);
		adesLOTL.setCertificateSource(adesLotlKeyStore());
		adesLOTL.setMraSupport(true);
		adesLOTL.setPivotSupport(false);

		adesLOTL.setLotlPredicate(new XMLOtherTSLPointer().and(new TypeOtherTSLPointer(adesTSLType)));
		adesLOTL.setTlPredicate(new XMLOtherTSLPointer().and(new TypeOtherTSLPointer(adesTSLType)).negate()); // allow all TSL Types

		return adesLOTL;
	}

	@Bean
	public DSSFileLoader offlineLoader() {
		FileCacheDataLoader offlineFileLoader = new FileCacheDataLoader();
		offlineFileLoader.setCacheExpirationTime(-1);
		offlineFileLoader.setDataLoader(new IgnoreDataLoader());
		offlineFileLoader.setFileCacheDirectory(tlCacheDirectory());
		return offlineFileLoader;
	}

	@Bean
	public File tlCacheDirectory() {
		File rootFolder = new File(System.getProperty("java.io.tmpdir"));
		File tslCache = new File(rootFolder, "dss-tsl-loader");
		if (tslCache.mkdirs()) {
			LOG.info("TL Cache folder : {}", tslCache.getAbsolutePath());
		}
		return tslCache;
	}
	
    /* QWAC Validation */

    @Bean
    public SSLCertificateLoader sslCertificateLoader() {
        SSLCertificateLoader sslCertificateLoader = new SSLCertificateLoader();
        sslCertificateLoader.setCommonsDataLoader(trustAllDataLoader());
        return sslCertificateLoader;
    }

	private <C extends CommonsDataLoader> C configureCommonsDataLoader(C dataLoader) {
		dataLoader.setTimeoutConnection(connectionTimeout);
		dataLoader.setTimeoutConnectionRequest(connectionRequestTimeout);
		dataLoader.setRedirectsEnabled(redirectEnabled);
		dataLoader.setProxyConfig(proxyConfig);
		return dataLoader;
	}

}