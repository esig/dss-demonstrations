package eu.europa.esig.dss.standalone;

import eu.europa.esig.dss.alert.ExceptionOnStatusAlert;
import eu.europa.esig.dss.asic.cades.signature.ASiCWithCAdESService;
import eu.europa.esig.dss.asic.xades.signature.ASiCWithXAdESService;
import eu.europa.esig.dss.cades.signature.CAdESService;
import eu.europa.esig.dss.jades.signature.JAdESService;
import eu.europa.esig.dss.pades.signature.PAdESService;
import eu.europa.esig.dss.service.crl.OnlineCRLSource;
import eu.europa.esig.dss.service.http.commons.CommonsDataLoader;
import eu.europa.esig.dss.service.http.commons.OCSPDataLoader;
import eu.europa.esig.dss.service.http.proxy.ProxyConfig;
import eu.europa.esig.dss.service.ocsp.OnlineOCSPSource;
import eu.europa.esig.dss.spi.tsl.TrustedListsCertificateSource;
import eu.europa.esig.dss.spi.x509.aia.DefaultAIASource;
import eu.europa.esig.dss.spi.x509.aia.OnlineAIASource;
import eu.europa.esig.dss.spi.x509.tsp.TSPSource;
import eu.europa.esig.dss.token.KeyStoreSignatureTokenConnection;
import eu.europa.esig.dss.validation.CertificateVerifier;
import eu.europa.esig.dss.validation.CommonCertificateVerifier;
import eu.europa.esig.dss.ws.signature.common.RemoteDocumentSignatureService;
import eu.europa.esig.dss.ws.signature.common.RemoteDocumentSignatureServiceImpl;
import eu.europa.esig.dss.x509.tsp.MockTSPSource;
import eu.europa.esig.dss.xades.signature.XAdESService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore.PasswordProtection;

public class RemoteDocumentSignatureServiceBuilder {

	private static final Logger LOG = LoggerFactory.getLogger(RemoteDocumentSignatureServiceBuilder.class);
	
	private TrustedListsCertificateSource tslCertificateSource = new TrustedListsCertificateSource();
	
	public void setTslCertificateSource(TrustedListsCertificateSource tslCertificateSource) {
		this.tslCertificateSource = tslCertificateSource;
	}

	public RemoteDocumentSignatureService build() {
		RemoteDocumentSignatureServiceImpl service = new RemoteDocumentSignatureServiceImpl();
		service.setAsicWithCAdESService(asicWithCadesService());
		service.setAsicWithXAdESService(asicWithXadesService());
		service.setCadesService(cadesService());
		service.setXadesService(xadesService());
		service.setPadesService(padesService());
		service.setJadesService(jadesService());
		return service;
	}

	private CommonsDataLoader dataLoader() {
		CommonsDataLoader dataLoader = new CommonsDataLoader();
		dataLoader.setProxyConfig(proxyConfig());
		return dataLoader;
	}

	private OnlineAIASource onlineAIASource() {
		OnlineAIASource onlineAIASource = new DefaultAIASource();
		onlineAIASource.setDataLoader(dataLoader());
		return onlineAIASource;
	}

	private OnlineCRLSource onlineCRLSource() {
		OnlineCRLSource onlineCRLSource = new OnlineCRLSource();
		onlineCRLSource.setDataLoader(dataLoader());
		return onlineCRLSource;
	}

	private OCSPDataLoader ocspDataLoader() {
		OCSPDataLoader ocspDataLoader = new OCSPDataLoader();
		ocspDataLoader.setProxyConfig(proxyConfig());
		return ocspDataLoader;
	}

	private OnlineOCSPSource onlineOCSPSource() {
		OnlineOCSPSource onlineOCSPSource = new OnlineOCSPSource();
		onlineOCSPSource.setDataLoader(ocspDataLoader());
		return onlineOCSPSource;
	}
	
	private ProxyConfig proxyConfig() {
		// not defined by default
		return null;
	}

	private CertificateVerifier certificateVerifier() {
		CommonCertificateVerifier certificateVerifier = new CommonCertificateVerifier();
		certificateVerifier.setCrlSource(onlineCRLSource());
		certificateVerifier.setOcspSource(onlineOCSPSource());
		certificateVerifier.setAIASource(onlineAIASource());
		certificateVerifier.setTrustedCertSources(tslCertificateSource);

		// Default configs
		certificateVerifier.setAlertOnMissingRevocationData(new ExceptionOnStatusAlert());
		certificateVerifier.setCheckRevocationForUntrustedChains(false);

		return certificateVerifier;
	}
	
	private TSPSource tspSource() {
		MockTSPSource tspSource = new MockTSPSource();
		try (InputStream is = RemoteDocumentSignatureServiceBuilder.class.getResourceAsStream("/self-signed-tsa.p12")) {
			tspSource.setToken(new KeyStoreSignatureTokenConnection(is, "PKCS12", new PasswordProtection("ks-password".toCharArray())));
		} catch (IOException e) {
			LOG.warn("Cannot load the KeyStore");
		}
		tspSource.setAlias("self-signed-tsa");
		return tspSource;
	}
	
	private ASiCWithCAdESService asicWithCadesService() {
		ASiCWithCAdESService service = new ASiCWithCAdESService(certificateVerifier());
		service.setTspSource(tspSource());
		return service;
	}

	private ASiCWithXAdESService asicWithXadesService() {
		ASiCWithXAdESService service = new ASiCWithXAdESService(certificateVerifier());
		service.setTspSource(tspSource());
		return service;
	}

	private CAdESService cadesService() {
		CAdESService service = new CAdESService(certificateVerifier());
		service.setTspSource(tspSource());
		return service;
	}

	private XAdESService xadesService() {
		XAdESService service = new XAdESService(certificateVerifier());
		service.setTspSource(tspSource());
		return service;
	}

	private PAdESService padesService() {
		PAdESService service = new PAdESService(certificateVerifier());
		service.setTspSource(tspSource());
		return service;
	}

	private JAdESService jadesService() {
		JAdESService service = new JAdESService(certificateVerifier());
		service.setTspSource(tspSource());
		return service;
	}
	
}
