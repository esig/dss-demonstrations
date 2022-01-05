package eu.europa.esig.dss.web.config;

import eu.europa.esig.dss.service.crl.JdbcCacheCRLSource;
import eu.europa.esig.dss.service.x509.aia.JdbcCacheAIASource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.sql.SQLException;

/**
 * This class is used to construct/destroy JDBC cache sources
 *
 */
@Component
public class JdbcInitializer {

    @Autowired
    private JdbcCacheAIASource cachedAIASource;

    @Autowired
    private JdbcCacheCRLSource cachedCRLSource;

    @PostConstruct
    public void cachedAIASourceInitialization() throws SQLException {
        cachedAIASource.initTable();
    }

    @PostConstruct
    public void cachedCRLSourceInitialization() throws SQLException {
        cachedCRLSource.initTable();
    }

    @PreDestroy
    public void cachedAIASourceClean() throws SQLException {
        cachedAIASource.destroyTable();
    }

    @PreDestroy
    public void cachedCRLSourceClean() throws SQLException {
        cachedCRLSource.destroyTable();
    }

    // OCSP cache is not used

}
