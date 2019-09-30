package eu.europa.esig.dss.standalone;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore.PasswordProtection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.europa.esig.dss.asic.cades.signature.ASiCWithCAdESService;
import eu.europa.esig.dss.asic.xades.signature.ASiCWithXAdESService;
import eu.europa.esig.dss.cades.signature.CAdESService;
import eu.europa.esig.dss.pades.signature.PAdESService;
import eu.europa.esig.dss.service.crl.OnlineCRLSource;
import eu.europa.esig.dss.service.http.commons.CommonsDataLoader;
import eu.europa.esig.dss.service.http.commons.OCSPDataLoader;
import eu.europa.esig.dss.service.http.proxy.ProxyConfig;
import eu.europa.esig.dss.service.ocsp.OnlineOCSPSource;
import eu.europa.esig.dss.spi.x509.tsp.TSPSource;
import eu.europa.esig.dss.token.KeyStoreSignatureTokenConnection;
import eu.europa.esig.dss.validation.CertificateVerifier;
import eu.europa.esig.dss.validation.CommonCertificateVerifier;
import eu.europa.esig.dss.ws.signature.common.RemoteDocumentSignatureService;
import eu.europa.esig.dss.ws.signature.common.RemoteDocumentSignatureServiceImpl;
import eu.europa.esig.dss.x509.tsp.MockTSPSource;
import eu.europa.esig.dss.xades.signature.XAdESService;

public class RemoteDocumentSignatureServiceBuilder {

	private static final Logger LOG = LoggerFactory.getLogger(RemoteDocumentSignatureServiceBuilder.class);
	
	public static RemoteDocumentSignatureService build() {
		RemoteDocumentSignatureServiceImpl service = new RemoteDocumentSignatureServiceImpl();
		service.setAsicWithCAdESService(asicWithCadesService());
		service.setAsicWithXAdESService(asicWithXadesService());
		service.setCadesService(cadesService());
		service.setXadesService(xadesService());
		service.setPadesService(padesService());
		return service;
	}

	private static CommonsDataLoader crlDataLoader() {
		CommonsDataLoader dataLoader = new CommonsDataLoader();
		dataLoader.setProxyConfig(proxyConfig());
		return dataLoader;
	}

	private static OnlineCRLSource onlineCRLSource() {
		OnlineCRLSource onlineCRLSource = new OnlineCRLSource();
		onlineCRLSource.setDataLoader(crlDataLoader());
		return onlineCRLSource;
	}

	private static OCSPDataLoader ocspDataLoader() {
		OCSPDataLoader ocspDataLoader = new OCSPDataLoader();
		ocspDataLoader.setProxyConfig(proxyConfig());
		return ocspDataLoader;
	}

	private static OnlineOCSPSource onlineOcspSource() {
		OnlineOCSPSource onlineOCSPSource = new OnlineOCSPSource();
		onlineOCSPSource.setDataLoader(ocspDataLoader());
		return onlineOCSPSource;
	}
	
	private static ProxyConfig proxyConfig() {
		// not defined by default
		return null;
	}

	private static CertificateVerifier certificateVerifier() {
		CommonCertificateVerifier certificateVerifier = new CommonCertificateVerifier();
		certificateVerifier.setCrlSource(onlineCRLSource());
		certificateVerifier.setOcspSource(onlineOcspSource());
		certificateVerifier.setDataLoader(crlDataLoader());

		// Default configs
		certificateVerifier.setExceptionOnMissingRevocationData(true);
		certificateVerifier.setCheckRevocationForUntrustedChains(false);

		return certificateVerifier;
	}
	
	private static TSPSource tspSource() {
		MockTSPSource tspSource = new MockTSPSource();
		try (InputStream is = RemoteDocumentSignatureServiceBuilder.class.getResourceAsStream("/self-signed-tsa.p12")) {
			tspSource.setToken(new KeyStoreSignatureTokenConnection(is, "PKCS12", new PasswordProtection("ks-password".toCharArray())));
		} catch (IOException e) {
			LOG.warn("Cannot load the KeyStore");
		}
		tspSource.setAlias("self-signed-tsa");
		return tspSource;
	}
	
	private static ASiCWithCAdESService asicWithCadesService() {
		ASiCWithCAdESService service = new ASiCWithCAdESService(certificateVerifier());
		service.setTspSource(tspSource());
		return service;
	}

	private static ASiCWithXAdESService asicWithXadesService() {
		ASiCWithXAdESService service = new ASiCWithXAdESService(certificateVerifier());
		service.setTspSource(tspSource());
		return service;
	}

	private static CAdESService cadesService() {
		CAdESService service = new CAdESService(certificateVerifier());
		service.setTspSource(tspSource());
		return service;
	}

	private static XAdESService xadesService() {
		XAdESService service = new XAdESService(certificateVerifier());
		service.setTspSource(tspSource());
		return service;
	}

	private static PAdESService padesService() {
		PAdESService service = new PAdESService(certificateVerifier());
		service.setTspSource(tspSource());
		return service;
	}

}
