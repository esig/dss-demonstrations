package eu.europa.esig.dss.web.config;

import javax.sql.DataSource;

import org.apache.commons.dbcp.BasicDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;

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
	public DataSource datasource() {
		BasicDataSource ds = new BasicDataSource();
		ds.setPassword(password);
		ds.setUsername(username);
		ds.setUrl(dataSourceUrl);
		ds.setDriverClassName(dataSourceDriverClassName);
		return ds;
	}

}
