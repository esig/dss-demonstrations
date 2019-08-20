package eu.europa.esig.dss.web.config;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import com.zaxxer.hikari.HikariDataSource;

@Configuration
@EnableTransactionManagement
public class PersistenceConfig {

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

}
