package eu.europa.esig.dss.standalone.source;

import eu.europa.esig.dss.service.http.commons.CommonsDataLoader;
import eu.europa.esig.dss.service.http.commons.FileCacheDataLoader;
import eu.europa.esig.dss.spi.client.http.DSSFileLoader;
import eu.europa.esig.dss.spi.client.http.IgnoreDataLoader;
import eu.europa.esig.dss.spi.tsl.TrustedListsCertificateSource;
import eu.europa.esig.dss.spi.x509.CertificateSource;
import eu.europa.esig.dss.spi.x509.KeyStoreCertificateSource;
import eu.europa.esig.dss.tsl.cache.CacheCleaner;
import eu.europa.esig.dss.tsl.function.OfficialJournalSchemeInformationURI;
import eu.europa.esig.dss.tsl.job.TLValidationJob;
import eu.europa.esig.dss.tsl.source.LOTLSource;
import eu.europa.esig.dss.tsl.sync.AcceptAllStrategy;
import eu.europa.esig.dss.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class TLValidationJobExecutor {

	private static final Logger LOG = LoggerFactory.getLogger(TLValidationJobExecutor.class);

	private static TLValidationJobExecutor instance;

	private TrustedListsCertificateSource tslCertificateSource;
	private File cacheDirectory;

	private TLValidationJobExecutor() {
		tslCertificateSource = new TrustedListsCertificateSource();
		cacheDirectory = tlCacheDirectory();
	}

	public static TLValidationJobExecutor getInstance() {
		if (instance == null) {
			instance = new TLValidationJobExecutor();
		}
		return instance;
	}

	public TLValidationJob job() {
		TLValidationJob job = new TLValidationJob();
		job.setOnlineDataLoader(onlineLoader());
		job.setOfflineDataLoader(offlineLoader());
		job.setTrustedListCertificateSource(tslCertificateSource);
		job.setSynchronizationStrategy(new AcceptAllStrategy());
		job.setCacheCleaner(cacheCleaner());

		LOTLSource europeanLOTL = europeanLOTL();
		job.setListOfTrustedListSources(europeanLOTL);

		return job;
	}

	private DSSFileLoader onlineLoader() {
		FileCacheDataLoader onlineFileLoader = new FileCacheDataLoader();
		onlineFileLoader.setCacheExpirationTime(0);
		onlineFileLoader.setDataLoader(dataLoader());
		onlineFileLoader.setFileCacheDirectory(cacheDirectory);
		return onlineFileLoader;
	}

	private DSSFileLoader offlineLoader() {
		FileCacheDataLoader offlineFileLoader = new FileCacheDataLoader();
		offlineFileLoader.setCacheExpirationTime(-1);
		offlineFileLoader.setDataLoader(new IgnoreDataLoader()); // do not download from Internet
		offlineFileLoader.setFileCacheDirectory(cacheDirectory);
		return offlineFileLoader;
	}

	private CommonsDataLoader dataLoader() {
		return new CommonsDataLoader();
	}

	private LOTLSource europeanLOTL() {
		LOTLSource lotlSource = new LOTLSource();
		lotlSource.setUrl(PropertyReader.getProperty("lotl.url"));
		lotlSource.setCertificateSource(officialJournalContentKeyStore());
		lotlSource.setSigningCertificatesAnnouncementPredicate(new OfficialJournalSchemeInformationURI(PropertyReader.getProperty("oj.url")));
		lotlSource.setPivotSupport(true);
		return lotlSource;
	}

	private CacheCleaner cacheCleaner() {
		CacheCleaner cacheCleaner = new CacheCleaner();
		cacheCleaner.setCleanMemory(true);
		cacheCleaner.setCleanFileSystem(true);
		cacheCleaner.setDSSFileLoader(getDSSFileLoader());
		return cacheCleaner;
	}

	private CertificateSource officialJournalContentKeyStore() {
		String keyStorePath = PropertyReader.getProperty("keystore.path");
		if (Utils.isStringNotEmpty(keyStorePath)) {
			return new KeyStoreCertificateSource(TLValidationJobExecutor.class.getResourceAsStream(keyStorePath),
					PropertyReader.getProperty("keystore.type"), PropertyReader.getProperty("keystore.password"));
		}
		LOG.warn("Path to the keystore is not defined!");
		return null;
	}

	private FileCacheDataLoader getDSSFileLoader() {
		FileCacheDataLoader fileLoader = new FileCacheDataLoader();
		fileLoader.setCacheExpirationTime(0);
		fileLoader.setFileCacheDirectory(cacheDirectory);
		return fileLoader;
	}

	private File tlCacheDirectory() {
		File rootFolder = new File(System.getProperty("java.io.tmpdir"));
		File tslCache = new File(rootFolder, "dss-tsl-loader");
		LOG.info("TL Cache folder : {}", tslCache.getAbsolutePath());
		return tslCache;
	}

	public TrustedListsCertificateSource getCertificateSources() {
		return tslCertificateSource;
	}

}
