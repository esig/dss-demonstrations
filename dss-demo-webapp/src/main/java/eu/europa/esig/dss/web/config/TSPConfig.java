package eu.europa.esig.dss.web.config;

import java.math.BigInteger;
import java.security.KeyStore.PasswordProtection;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaCertStore;
import org.bouncycastle.cms.DefaultCMSSignatureAlgorithmNameGenerator;
import org.bouncycastle.cms.SignerInfoGenerator;
import org.bouncycastle.cms.SignerInfoGeneratorBuilder;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.DigestCalculator;
import org.bouncycastle.operator.bc.BcDigestCalculatorProvider;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.operator.jcajce.JcaDigestCalculatorProviderBuilder;
import org.bouncycastle.tsp.TSPAlgorithms;
import org.bouncycastle.tsp.TimeStampRequest;
import org.bouncycastle.tsp.TimeStampRequestGenerator;
import org.bouncycastle.tsp.TimeStampResponse;
import org.bouncycastle.tsp.TimeStampResponseGenerator;
import org.bouncycastle.tsp.TimeStampToken;
import org.bouncycastle.tsp.TimeStampTokenGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import eu.europa.esig.dss.DSSException;
import eu.europa.esig.dss.DigestAlgorithm;
import eu.europa.esig.dss.token.KSPrivateKeyEntry;
import eu.europa.esig.dss.token.KeyStoreSignatureTokenConnection;
import eu.europa.esig.dss.x509.CertificateToken;
import eu.europa.esig.dss.x509.tsp.TSPSource;

@Configuration
public class TSPConfig {

	private static SecureRandom random = new SecureRandom();

	@Value("${dss.tsa.keystore.type}")
	private String tsaKeystoreType;

	@Value("${dss.tsa.keystore.filename}")
	private String tsaKeystoreFilename;

	@Value("${dss.tsa.keystore.alias}")
	private String tsaKeystoreAlias;

	@Value("${dss.tsa.keystore.password}")
	private String tsaKeystorePassword;

	@Bean
	public TSPSource tspSource() {
		return new MockTSPSource();
	}

	private class MockTSPSource implements TSPSource {

		private static final long serialVersionUID = 1L;

		private final Logger LOG = LoggerFactory.getLogger(MockTSPSource.class);

		@Override
		public TimeStampToken getTimeStampResponse(DigestAlgorithm digestAlgorithm, byte[] digest) {

			try (KeyStoreSignatureTokenConnection ksstc = new KeyStoreSignatureTokenConnection(new ClassPathResource(tsaKeystoreFilename).getFile(),
					tsaKeystoreType, new PasswordProtection(tsaKeystorePassword.toCharArray()))) {

				TimeStampRequestGenerator requestGenerator = new TimeStampRequestGenerator();
				requestGenerator.setCertReq(true);
				TimeStampRequest request = requestGenerator.generate(new ASN1ObjectIdentifier(digestAlgorithm.getOid()), digest);

				KSPrivateKeyEntry ksPK = (KSPrivateKeyEntry) ksstc.getKey(tsaKeystoreAlias);

				LOG.info("Timestamping with {}", ksPK.getCertificate());

				X509CertificateHolder certificate = new X509CertificateHolder(ksPK.getCertificate().getEncoded());
				List<X509Certificate> chain = new ArrayList<X509Certificate>();
				CertificateToken[] certificateChain = ksPK.getCertificateChain();
				for (CertificateToken token : certificateChain) {
					chain.add(token.getCertificate());
				}

				Set<ASN1ObjectIdentifier> accepted = new HashSet<ASN1ObjectIdentifier>();
				accepted.add(TSPAlgorithms.SHA1);
				accepted.add(TSPAlgorithms.SHA256);
				accepted.add(TSPAlgorithms.SHA512);

				AlgorithmIdentifier digestAlgorithmIdentifier = new AlgorithmIdentifier(new ASN1ObjectIdentifier(digestAlgorithm.getOid()));
				AlgorithmIdentifier encryptionAlg = new AlgorithmIdentifier(PKCSObjectIdentifiers.rsaEncryption);

				DefaultCMSSignatureAlgorithmNameGenerator sigAlgoGenerator = new DefaultCMSSignatureAlgorithmNameGenerator();
				String sigAlgoName = sigAlgoGenerator.getSignatureName(digestAlgorithmIdentifier, encryptionAlg);

				ContentSigner signer = new JcaContentSignerBuilder(sigAlgoName).build(ksPK.getPrivateKey());

				SignerInfoGenerator infoGenerator = new SignerInfoGeneratorBuilder(new BcDigestCalculatorProvider()).build(signer, certificate);
				DigestCalculator digestCalculator = new JcaDigestCalculatorProviderBuilder().build().get(digestAlgorithmIdentifier);

				TimeStampTokenGenerator tokenGenerator = new TimeStampTokenGenerator(infoGenerator, digestCalculator, new ASN1ObjectIdentifier("1.2.3.4"));
				tokenGenerator.addCertificates(new JcaCertStore(chain));

				TimeStampResponseGenerator responseGenerator = new TimeStampResponseGenerator(tokenGenerator, accepted);
				TimeStampResponse response = responseGenerator.generate(request, new BigInteger(128, random), new Date());
				return response.getTimeStampToken();

			} catch (Exception e) {
				throw new DSSException("Unable to generate MOCK TSP", e);
			}
		}

	}

}
