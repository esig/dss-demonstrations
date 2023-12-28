package eu.europa.esig.dss.web.config;

import com.zaxxer.hikari.HikariDataSource;
import eu.europa.esig.dss.service.crl.JdbcCacheCRLSource;
import eu.europa.esig.dss.service.ocsp.JdbcCacheOCSPSource;
import eu.europa.esig.dss.service.x509.aia.JdbcCacheAIASource;
import eu.europa.esig.dss.spi.client.jdbc.JdbcCacheConnector;
import eu.europa.esig.dss.utils.Utils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;

@Configuration
@EnableTransactionManagement
public class JdbcConfig {

	@Value("${datasource.jdbc.enabled:false}")
	private Boolean jdbcEnabled;

	@Value("${datasource.username}")
	private String username;

	@Value("${datasource.password}")
	private String password;

	@Value("${datasource.url}")
	private String dataSourceUrl;

	@Value("${datasource.driver.class}")
	private String dataSourceDriverClassName;

	@Bean
	public DataSource dataSource() {
		HikariDataSource ds = new HikariDataSource();
		ds.setPoolName("DSS-Hikari-Pool");
		ds.setJdbcUrl(dataSourceUrl);
		ds.setDriverClassName(dataSourceDriverClassName);
		ds.setUsername(username);
		ds.setPassword(password);
		ds.setAutoCommit(false);
		return ds;
	}

	@Bean
	public JdbcCacheConnector jdbcCacheConnector() {
		if (Utils.isTrue(jdbcEnabled)) {
			return new JdbcCacheConnector(dataSource());
		}
		return null;
	}

	@Bean
	public JdbcCacheAIASource jdbcCacheAIASource() {
		if (Utils.isTrue(jdbcEnabled)) {
			JdbcCacheAIASource jdbcCacheAIASource = new JdbcCacheAIASource();
			jdbcCacheAIASource.setJdbcCacheConnector(jdbcCacheConnector());
			return jdbcCacheAIASource;
		}
		return null;
	}

	@Bean
	public JdbcCacheCRLSource jdbcCacheCRLSource() {
		if (Utils.isTrue(jdbcEnabled)) {
			JdbcCacheCRLSource jdbcCacheCRLSource = new JdbcCacheCRLSource();
			jdbcCacheCRLSource.setJdbcCacheConnector(jdbcCacheConnector());
			return jdbcCacheCRLSource;
		}
		return null;
	}

	@Bean
	public JdbcCacheOCSPSource jdbcCacheOCSPSource() {
		if (Utils.isTrue(jdbcEnabled)) {
			JdbcCacheOCSPSource jdbcCacheOCSPSource = new JdbcCacheOCSPSource();
			jdbcCacheOCSPSource.setJdbcCacheConnector(jdbcCacheConnector());
			return jdbcCacheOCSPSource;
		}
		return null;
	}

}
