package eu.europa.esig.dss.web.config;

import eu.europa.esig.dss.service.crl.JdbcCacheCRLSource;
import eu.europa.esig.dss.service.ocsp.JdbcCacheOCSPSource;
import eu.europa.esig.dss.service.x509.aia.JdbcCacheAIASource;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.sql.SQLException;

/**
 * This class is used to construct/destroy JDBC cache sources
 *
 */
@Component
public class JdbcInitializer {

    @Autowired(required = false)
    private JdbcCacheAIASource jdbcCacheAIASource;

    @Autowired(required = false)
    private JdbcCacheCRLSource jdbcCacheCRLSource;

    @Autowired(required = false)
    private JdbcCacheOCSPSource jdbcCacheOCSPSource;

    @PostConstruct
    public void cachedAIASourceInitialization() throws SQLException {
        if (jdbcCacheAIASource != null) {
            jdbcCacheAIASource.initTable();
        }
    }

    @PostConstruct
    public void cachedCRLSourceInitialization() throws SQLException {
        if (jdbcCacheCRLSource != null) {
            jdbcCacheCRLSource.initTable();
        }
    }

    @PostConstruct
    public void cacheOCSPSourceInitialization() throws SQLException {
        if (jdbcCacheOCSPSource != null) {
            jdbcCacheOCSPSource.initTable();
        }
    }

    @PreDestroy
    public void cachedAIASourceClean() throws SQLException {
        if (jdbcCacheAIASource != null) {
            jdbcCacheAIASource.destroyTable();
        }
    }

    @PreDestroy
    public void cachedCRLSourceClean() throws SQLException {
        if (jdbcCacheCRLSource != null) {
            jdbcCacheCRLSource.destroyTable();
        }
    }

    @PreDestroy
    public void cachedOCSPSourceClean() throws SQLException {
        if (jdbcCacheOCSPSource != null) {
            jdbcCacheOCSPSource.destroyTable();
        }
    }

}
